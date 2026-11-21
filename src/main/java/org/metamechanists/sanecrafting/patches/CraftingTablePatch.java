package org.metamechanists.sanecrafting.patches;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.VanillaItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.multiblocks.EnhancedCraftingTable;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.sanecrafting.SaneCrafting;
import org.metamechanists.sanecrafting.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static org.metamechanists.sanecrafting.Util.generateRecipeId;


@UtilityClass
public class CraftingTablePatch {
    @Nullable
    private List<ItemStack[]> getRecipes() {
        EnhancedCraftingTable enhancedCraftingTable = Util.findMultiblock(EnhancedCraftingTable.class);
        if (enhancedCraftingTable == null) {
            return null;
        }
        return enhancedCraftingTable.getRecipes();
    }

    private void convertRecipe(List<ItemStack> input, ItemStack output) {
        convertRecipe(generateRecipeId(output), input, output);
    }

    private void convertRecipe(String recipeId, List<ItemStack> input, ItemStack output) {
        // Remove recipe if already registered
        NamespacedKey key = new NamespacedKey(SaneCrafting.getInstance(), recipeId);
        if (Bukkit.getServer().getRecipe(key) != null) {
            Bukkit.getServer().removeRecipe(key);
        }

        // Convert to shape
        String itemCharacters = "abcdefghi";
        List<String> shape = new ArrayList<>(List.of("abc", "def", "ghi"));
        Map<Character, ItemStack> ingredients = new HashMap<>();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int i = y*3 + x;
                char character = itemCharacters.charAt(i);
                ItemStack itemStack = input.get(i);
                if (itemStack == null) {
                    shape.set(y, shape.get(y).replace(character, ' '));
                } else {
                    ingredients.put(character, itemStack);
                }
            }
        }

        // Trim empty outer rows/columns only — never strip interior empty rows/columns
        // (e.g. boots ["d f", "g i"] must preserve the center gap, not collapse to ["df", "gi"]).
        trimShape(shape);

        // Skip if no recipe (just in case)
        if (shape.isEmpty() || shape.get(0).isEmpty()) {
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(key, output);
        recipe.shape(shape.toArray(new String[]{}));
        for (Entry<Character, ItemStack> entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
        Bukkit.getServer().addRecipe(recipe);
    }

    static void trimShape(List<String> shape) {
        // Trim leading and trailing empty rows only
        while (!shape.isEmpty() && isRowEmpty(shape.get(0))) {
            shape.remove(0);
        }
        while (!shape.isEmpty() && isRowEmpty(shape.get(shape.size() - 1))) {
            shape.remove(shape.size() - 1);
        }

        if (shape.isEmpty()) {
            return;
        }

        // Trim leading empty columns only
        while (!shape.get(0).isEmpty() && isColumnEmpty(shape, 0)) {
            for (int y = 0; y < shape.size(); y++) {
                shape.set(y, shape.get(y).substring(1));
            }
        }

        // Trim trailing empty columns only
        while (!shape.get(0).isEmpty() && isColumnEmpty(shape, shape.get(0).length() - 1)) {
            int lastCol = shape.get(0).length() - 1;
            for (int y = 0; y < shape.size(); y++) {
                shape.set(y, shape.get(y).substring(0, lastCol));
            }
        }
    }

    private boolean isRowEmpty(String row) {
        for (int i = 0; i < row.length(); i++) {
            if (row.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    private boolean isColumnEmpty(List<String> shape, int col) {
        for (String row : shape) {
            if (row.charAt(col) != ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * Registers Enhanced Crafting Table recipes one per server tick.
     *
     * <p>On current Paper/Purpur, every {@link org.bukkit.Server#addRecipe}
     * rebuilds the recipe display cache. Registering a large Slimefun catalog
     * in one scheduler task therefore blocks the primary thread long enough
     * for the watchdog to fire. Keeping every registration on the primary
     * thread preserves Bukkit's threading contract while bounding each tick.</p>
     */
    public void applyBatched(Runnable completion) {
        List<ItemStack[]> recipes = getRecipes();
        if (recipes == null) {
            completion.run();
            return;
        }

        List<Runnable> conversions = new ArrayList<>();
        int oversized = 0;
        for (int j = 0; j < recipes.size(); j += 2) {
            ItemStack[] input = recipes.get(j);
            ItemStack output = recipes.get(j + 1)[0];
            if (SlimefunItem.getByItem(output) == null) {
                // Skip vanilla slimefunitem recipes for now
                continue;
            }
            if (exceedsMaxStackSize(output)) {
                oversized++;
                continue;
            }

            conversions.add(() -> convertRecipeSafely(Arrays.asList(input), output));
        }

        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            if (item instanceof VanillaItem vanillaItem && item.getRecipeType() == RecipeType.ENHANCED_CRAFTING_TABLE) {
                if (exceedsMaxStackSize(vanillaItem.getRecipeOutput())) {
                    oversized++;
                    continue;
                }
                conversions.add(() -> convertVanillaRecipeSafely(vanillaItem));
            }
        }

        SaneCrafting.getInstance().getLogger().info("Enhanced Crafting Table: " + recipes.size() / 2
                + " recipes found, " + conversions.size() + " to convert, " + oversized
                + " kept only in the Enhanced Crafting Table (output above max stack size)");

        if (conversions.isEmpty()) {
            completion.run();
            return;
        }

        new BukkitRunnable() {
            private int nextRecipe;

            @Override
            public void run() {
                conversions.get(nextRecipe++).run();
                if (nextRecipe == conversions.size()) {
                    cancel();
                    SaneCrafting.getInstance().getLogger().info("Applied CraftingTable patch and converted "
                            + nextRecipe + " Enhanced Crafting Table recipes to regular Crafing Table recipes");
                    completion.run();
                }
            }
        }.runTaskTimer(SaneCrafting.getInstance(), 1L, 1L);
    }

    /**
     * A shaped recipe whose result exceeds the item's max stack size (e.g. 4 potions)
     * cannot be materialised by the server: it logs "Can't create item stack" and
     * breaks consumers that iterate recipes, such as EquivalencyTech's EMC pass.
     * Such recipes stay craftable in the Enhanced Crafting Table instead of being
     * clamped, so players never get a smaller yield.
     */
    static boolean exceedsMaxStackSize(ItemStack output) {
        return output.getAmount() > output.getMaxStackSize();
    }

    private void convertRecipeSafely(List<ItemStack> input, ItemStack output) {
        try {
            convertRecipe(input, output);
        } catch (RuntimeException e) {
            String name = PlainTextComponentSerializer.plainText().serialize(output.displayName());
            SaneCrafting.getInstance().getLogger().severe("Failed to convert Enhanced Crafting Table recipe for " + name);
            e.printStackTrace();
        }
    }

    private void convertVanillaRecipeSafely(VanillaItem vanillaItem) {
        try {
            convertRecipe(generateRecipeId(vanillaItem), Arrays.asList(vanillaItem.getRecipe()), vanillaItem.getRecipeOutput());
        } catch (RuntimeException e) {
            String name = PlainTextComponentSerializer.plainText().serialize(vanillaItem.getItem().displayName());
            SaneCrafting.getInstance().getLogger().severe("Failed to convert Enhanced Crafting Table recipe for " + name);
            e.printStackTrace();
        }
    }
}

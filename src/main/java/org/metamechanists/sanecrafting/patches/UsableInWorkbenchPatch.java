package org.metamechanists.sanecrafting.patches;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.sanecrafting.SaneCrafting;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public final class UsableInWorkbenchPatch implements Listener {
    static final String SANECRAFTING_NAMESPACE = "sanecrafting";

    private static final Set<String> usableInWorkbench = new HashSet<>();
    private static final Set<String> processedItems = new HashSet<>();
    private static final AtomicBoolean registered = new AtomicBoolean(false);

    private UsableInWorkbenchPatch() {}

    public static void apply() {
        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            // Capture the original isUseableInWorkbench flag only once per item ID so
            // subsequent reloads do not treat SaneCrafting's own override as vanilla-usable.
            if (processedItems.add(item.getId()) && item.isUseableInWorkbench()) {
                usableInWorkbench.add(item.getId());
            }
            item.setUseableInWorkbench(true);
        }

        if (registered.compareAndSet(false, true)) {
            Bukkit.getServer().getPluginManager().registerEvents(new UsableInWorkbenchPatch(), SaneCrafting.getInstance());
        }
        SaneCrafting.getInstance().getLogger().info("Applied UsableInWorkbench patch");
    }

    static boolean isSaneCraftingRecipe(@Nullable Recipe recipe) {
        if (recipe instanceof Keyed keyed) {
            return isSaneCraftingKey(keyed.getKey());
        }
        return false;
    }

    static boolean isSaneCraftingKey(@Nullable NamespacedKey key) {
        if (key == null) {
            return false;
        }
        String pluginNamespace = SaneCrafting.getInstance() != null
                ? SaneCrafting.getInstance().getName().toLowerCase(Locale.ROOT)
                : SANECRAFTING_NAMESPACE;
        return key.getNamespace().equals(pluginNamespace);
    }

    private static boolean containsDisallowedSlimefunIngredient(@Nullable ItemStack[] contents) {
        if (contents == null) {
            return false;
        }
        for (ItemStack item : contents) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            if (sfItem != null && !usableInWorkbench.contains(sfItem.getId())) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public static void onCraft(@NotNull CraftItemEvent e) {
        // Allow recipes registered by SaneCrafting (including VanillaItem outputs) and Slimefun outputs
        if (isSaneCraftingRecipe(e.getRecipe()) || SlimefunItem.getByItem(e.getInventory().getResult()) != null) {
            return;
        }

        // Otherwise make sure we're not trying to craft a vanilla item with non-usable SF ingredients
        if (containsDisallowedSlimefunIngredient(e.getInventory().getMatrix())) {
            e.setResult(Result.DENY);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public static void onPrepareCraft(@NotNull PrepareItemCraftEvent e) {
        ItemStack result = e.getInventory().getResult();
        if (result == null) {
            return;
        }

        // Allow recipes registered by SaneCrafting (including VanillaItem outputs) and Slimefun outputs
        if (isSaneCraftingRecipe(e.getRecipe()) || SlimefunItem.getByItem(result) != null) {
            return;
        }

        // Otherwise make sure we're not trying to craft a vanilla item with non-usable SF ingredients
        if (containsDisallowedSlimefunIngredient(e.getInventory().getMatrix())) {
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public static void onCrafterCraft(@NotNull CrafterCraftEvent e) {
        SlimefunItem sfResult = SlimefunItem.getByItem(e.getResult());
        if (isSaneCraftingRecipe(e.getRecipe()) || sfResult != null) {
            // A Crafter block has no player to verify research ownership; never bypass Slimefun researches.
            if (sfResult != null && sfResult.hasResearch() && Slimefun.getRegistry().isResearchingEnabled()) {
                e.setCancelled(true);
            }
            return;
        }

        // Vanilla recipe in a Crafter: block if any slot holds a non-workbench-usable Slimefun item.
        BlockState state = e.getBlock().getState();
        if (state instanceof Container container
                && containsDisallowedSlimefunIngredient(container.getInventory().getContents())) {
            e.setCancelled(true);
        }
    }
}

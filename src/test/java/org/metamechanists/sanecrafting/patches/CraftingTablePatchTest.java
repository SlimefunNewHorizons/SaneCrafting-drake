package org.metamechanists.sanecrafting.patches;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingTablePatchTest {

    @Test
    void trimShapePreservesCenterColumnForBootsRecipe() {
        // Obsidian Boots pattern: [null, null, null, alloy, null, alloy, alloy, null, alloy]
        List<String> shape = new ArrayList<>(List.of("   ", "d f", "g i"));
        CraftingTablePatch.trimShape(shape);
        assertEquals(List.of("d f", "g i"), shape);
    }

    @Test
    void trimShapePreservesCenterRowWhenInteriorRowIsEmpty() {
        List<String> shape = new ArrayList<>(List.of("a c", "   ", "g i"));
        CraftingTablePatch.trimShape(shape);
        assertEquals(List.of("a c", "   ", "g i"), shape);
    }

    @Test
    void trimShapeRemovesOuterEmptyRowsAndColumns() {
        List<String> shape = new ArrayList<>(List.of("   ", "   ", "  i"));
        CraftingTablePatch.trimShape(shape);
        assertEquals(List.of("i"), shape);
    }

    @Test
    void trimShapeHandlesHelmetChestplateAndLeggings() {
        List<String> helmet = new ArrayList<>(List.of("abc", "d f", "   "));
        CraftingTablePatch.trimShape(helmet);
        assertEquals(List.of("abc", "d f"), helmet);

        List<String> chestplate = new ArrayList<>(List.of("a c", "def", "ghi"));
        CraftingTablePatch.trimShape(chestplate);
        assertEquals(List.of("a c", "def", "ghi"), chestplate);

        List<String> leggings = new ArrayList<>(List.of("abc", "d f", "g i"));
        CraftingTablePatch.trimShape(leggings);
        assertEquals(List.of("abc", "d f", "g i"), leggings);
    }

    @Test
    void trimShapeReturnsEmptyForBlankGrid() {
        List<String> empty = new ArrayList<>(List.of("   ", "   ", "   "));
        CraftingTablePatch.trimShape(empty);
        assertTrue(empty.isEmpty());
    }

    @Test
    void uniqueRecipeIdSuffixesDuplicatesDeterministically() {
        Map<String, Integer> seen = new HashMap<>();
        assertEquals("sanecrafting_obsidian_boots", CraftingTablePatch.uniqueRecipeId("sanecrafting_obsidian_boots", seen));
        assertEquals("sanecrafting_obsidian_boots_2", CraftingTablePatch.uniqueRecipeId("sanecrafting_obsidian_boots", seen));
        assertEquals("sanecrafting_obsidian_boots_3", CraftingTablePatch.uniqueRecipeId("sanecrafting_obsidian_boots", seen));
        assertEquals("sanecrafting_other_item", CraftingTablePatch.uniqueRecipeId("sanecrafting_other_item", seen));
    }

    @Test
    void hasStackedIngredientHandlesNullAndEmptyLists() {
        assertFalse(CraftingTablePatch.hasStackedIngredient(null));
        assertFalse(CraftingTablePatch.hasStackedIngredient(List.of()));
        List<org.bukkit.inventory.ItemStack> nulls = new ArrayList<>();
        nulls.add(null);
        assertFalse(CraftingTablePatch.hasStackedIngredient(nulls));
    }

    @Test
    void isSaneCraftingKeyMatchesOnlySaneCraftingNamespace() {
        assertTrue(UsableInWorkbenchPatch.isSaneCraftingKey(new NamespacedKey("sanecrafting", "sanecrafting_elytra")));
        assertFalse(UsableInWorkbenchPatch.isSaneCraftingKey(NamespacedKey.minecraft("iron_block")));
        assertFalse(UsableInWorkbenchPatch.isSaneCraftingKey(new NamespacedKey("slimefun", "something")));
        assertFalse(UsableInWorkbenchPatch.isSaneCraftingKey(null));
    }
}

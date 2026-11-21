package org.metamechanists.sanecrafting.patches;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}

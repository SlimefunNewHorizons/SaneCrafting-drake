package org.metamechanists.sanecrafting.patches;

import com.github.drakescraft_labs.slimefun4.api.events.ResearchUnlockEvent;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.sanecrafting.SaneCrafting;
import org.metamechanists.sanecrafting.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public final class RecipeBookResearchPatch implements Listener {
    private static boolean registered;

    private RecipeBookResearchPatch() {}

    public static void apply() {
        if (!registered) {
            registered = true;
            Bukkit.getServer().getPluginManager().registerEvents(new RecipeBookResearchPatch(), SaneCrafting.getInstance());
        }
        // Quien entro mientras las recetas se registraban por ticks perdio las aun no registradas
        // ("Tried to load unrecognized recipe ... removed now"): se las devolvemos ahora.
        for (Player player : Bukkit.getOnlinePlayers()) {
            rediscover(player);
        }
        SaneCrafting.getInstance().getLogger().info("Applied RecipeBookResearch patch");
    }

    private static void rediscover(@NotNull Player player) {
        boolean researching = Slimefun.getRegistry().isResearchingEnabled();
        var profile = Slimefun.getRegistry().getPlayerProfiles().get(player.getUniqueId());
        if (researching && profile == null) {
            return;
        }
        List<NamespacedKey> keys = new ArrayList<>();
        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            if (researching && item.hasResearch() && !profile.hasUnlocked(item.getResearch())) {
                continue;
            }
            String recipeId = Util.generateRecipeId(item.getItem());
            if (recipeId != null) {
                NamespacedKey key = new NamespacedKey(SaneCrafting.getInstance(), recipeId);
                if (Bukkit.getServer().getRecipe(key) != null) {
                    keys.add(key);
                }
            }
        }
        player.discoverRecipes(keys);
    }

    @EventHandler
    public static void onJoin(@NotNull PlayerJoinEvent e) {
        if (Slimefun.getRegistry().isResearchingEnabled()) {
            return;
        }

        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            String recipeId = Util.generateRecipeId(item.getItem());
            if (recipeId != null) {
                NamespacedKey key = new NamespacedKey(SaneCrafting.getInstance(), recipeId);
                if (Bukkit.getServer().getRecipe(key) != null) {
                    e.getPlayer().discoverRecipe(key);
                } else {
                    e.getPlayer().undiscoverRecipe(key);
                }
            }
        }
    }

    @EventHandler
    public static void onResearch(@NotNull ResearchUnlockEvent e) {
        for (SlimefunItem item : e.getResearch().getAffectedItems()) {
            if (item == null) {
                continue;
            }
            String recipeId = Util.generateRecipeId(item.getItem());
            if (recipeId != null) {
                NamespacedKey key = new NamespacedKey(SaneCrafting.getInstance(), recipeId);
                if (Bukkit.getServer().getRecipe(key) != null) {
                    e.getPlayer().discoverRecipe(key);
                } else {
                    e.getPlayer().undiscoverRecipe(key);
                }
            }
        }
    }

    @EventHandler
    public static void onCraft(@NotNull CraftItemEvent e) {
        SlimefunItem result = SlimefunItem.getByItem(e.getInventory().getResult());
        if (result == null) {
            return;
        }

        if (!result.hasResearch()) {
            return;
        }

        UUID uuid = e.getWhoClicked().getUniqueId();
        if (Slimefun.getRegistry().getPlayerProfiles().get(uuid).hasUnlocked(result.getResearch())) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public static void onPrepareCraft(@NotNull PrepareItemCraftEvent e) {
        SlimefunItem result = SlimefunItem.getByItem(e.getInventory().getResult());
        if (result == null) {
            return;
        }

        if (!result.hasResearch()) {
            return;
        }

        UUID uuid = e.getView().getPlayer().getUniqueId();
        if (Slimefun.getRegistry().getPlayerProfiles().get(uuid).hasUnlocked(result.getResearch())) {
            return;
        }

        e.getInventory().setResult(null);
    }
}

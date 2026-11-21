package org.metamechanists.sanecrafting.patches;

import io.github.thebusybiscuit.slimefun4.api.events.ResearchUnlockEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.sanecrafting.SaneCrafting;
import org.metamechanists.sanecrafting.Util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public final class RecipeBookResearchPatch implements Listener {
    private static final AtomicBoolean registered = new AtomicBoolean(false);

    private RecipeBookResearchPatch() {}

    public static void apply() {
        if (registered.compareAndSet(false, true)) {
            Bukkit.getServer().getPluginManager().registerEvents(new RecipeBookResearchPatch(), SaneCrafting.getInstance());
        }
        SaneCrafting.getInstance().getLogger().info("Applied RecipeBookResearch patch");
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

    private static boolean hasUnlockedResearch(@NotNull HumanEntity entity, @NotNull SlimefunItem result) {
        if (!Slimefun.getRegistry().isResearchingEnabled() || !result.hasResearch()) {
            return true;
        }

        UUID uuid = entity.getUniqueId();
        PlayerProfile profile = Slimefun.getRegistry().getPlayerProfiles().get(uuid);
        if (profile == null) {
            if (entity instanceof Player player) {
                PlayerProfile.request(player);
            }
            return false;
        }

        return profile.hasUnlocked(result.getResearch());
    }

    @EventHandler
    public static void onCraft(@NotNull CraftItemEvent e) {
        SlimefunItem result = SlimefunItem.getByItem(e.getInventory().getResult());
        if (result == null || hasUnlockedResearch(e.getWhoClicked(), result)) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public static void onPrepareCraft(@NotNull PrepareItemCraftEvent e) {
        SlimefunItem result = SlimefunItem.getByItem(e.getInventory().getResult());
        if (result == null || hasUnlockedResearch(e.getView().getPlayer(), result)) {
            return;
        }

        e.getInventory().setResult(null);
    }
}

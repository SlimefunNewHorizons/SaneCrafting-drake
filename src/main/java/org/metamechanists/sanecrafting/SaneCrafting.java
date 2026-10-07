package org.metamechanists.sanecrafting;


import com.github.drakescraft_labs.labupdate.DrakesLabsReleaseUpdate;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.event.server.ServerLoadEvent;
import lombok.Getter;
import lombok.NonNull;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.sanecrafting.patches.CraftingTablePatch;
import org.metamechanists.sanecrafting.patches.RecipeBookResearchPatch;
import org.metamechanists.sanecrafting.patches.RecipeLorePatch;
import org.metamechanists.sanecrafting.patches.UsableInWorkbenchPatch;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;


public final class SaneCrafting extends JavaPlugin implements SlimefunAddon {
    private static final int BSTATS_ID = 22737;
    @Getter
    private static SaneCrafting instance;

    @Override
    public void onEnable() {
        DrakesLabsReleaseUpdate.schedule(this, "SaneCrafting-drake");

        instance = this;

        if (getConfig().getBoolean("auto-update") && !getPluginVersion().contains("MODIFIED")) {
            new GitHubBuildsUpdater(this, getFile(), "metamechanists/SaneCrafting/master").start();
        }

        new Metrics(this, BSTATS_ID);

        // El registro se mantiene en el hilo principal, pero cada alta se reparte
        // entre ticks para no bloquear el watchdog cuando el catálogo es grande.
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onServerLoad(ServerLoadEvent e) {
                if (e.getType() != ServerLoadEvent.LoadType.STARTUP
                        && e.getType() != ServerLoadEvent.LoadType.RELOAD) {
                    return;
                }
                UsableInWorkbenchPatch.apply();
                CraftingTablePatch.applyBatched(() -> {
                    RecipeBookResearchPatch.apply();
                    RecipeLorePatch.apply();
                    if (e.getType() == ServerLoadEvent.LoadType.STARTUP) {
                        // Esperar el primer lote evita que dos pasadas registren recetas en paralelo.
                        Bukkit.getScheduler().runTaskLater(SaneCrafting.this, () -> {
                            CraftingTablePatch.applyBatched(() -> {
                                RecipeBookResearchPatch.apply();
                                RecipeLorePatch.apply();
                            });
                        }, 20L);
                    }
                });
            }
        }, this);

    }

    @Override
    public void onDisable() {

    }

    @NonNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return null;
    }
}

package com.extrahardmode.features.monsters;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.BiomeHelper;
import com.extrahardmode.module.EntityHelper;
import com.extrahardmode.service.ListenerModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Parched
 * <p/>
 * Skeletons which spawn in desserts and badlands are replaced by a Parched, the desert variant of the skeleton
 */
public class Parched extends ListenerModule
{
    private RootConfig CFG;


    public Parched(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Constructor for the unit tests, {@link #starting()} is not called
     *
     * @param plugin - the plugin
     * @param CFG    - the config to use
     */
    Parched(ExtraHardMode plugin, RootConfig CFG)
    {
        super(plugin);
        this.CFG = CFG;
    }


    @Override
    public void starting()
    {
        super.starting();
        CFG = plugin.getModuleForClass(RootConfig.class);
    }


    /**
     * When an Entity spawns: Spawn a Parched sometimes instead of a Skeleton in deserts
     *
     * @param event which occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        if (!shouldReplace(event))
            return;

        final int parchedSpawnPercent = CFG.getInt(RootNode.BONUS_PARCHED_SPAWN_PERCENT, event.getLocation().getWorld().getName());
        if (plugin.random(parchedSpawnPercent))
        {
            event.setCancelled(true);
            EntityHelper.spawn(event.getLocation(), EntityType.PARCHED);
        }
    }


    /**
     * Whether this spawn is a natural skeleton spawn in a desert or badlands
     *
     * @param event - the spawn event to check
     *
     * @return true if a Parched may be spawned instead
     */
    boolean shouldReplace(CreatureSpawnEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (EntityHelper.isMarkedAsOurs(entity))
            return false;
        if (entity.getType() != EntityType.SKELETON || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return false;
        Location location = event.getLocation();
        World world = location.getWorld();
        return world != null && world.getEnvironment() == World.Environment.NORMAL && BiomeHelper.isDesert(world.getBiome(location));
    }
}

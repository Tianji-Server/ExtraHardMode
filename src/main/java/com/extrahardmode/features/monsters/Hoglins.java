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
 * Hoglins
 * <p/>
 * Zombified piglins which spawn in crimson forests are replaced by a Hoglin, which shoves players around and knocks
 * them into lava
 */
public class Hoglins extends ListenerModule
{
    private RootConfig CFG;


    public Hoglins(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Constructor for the unit tests, {@link #starting()} is not called
     *
     * @param plugin - the plugin
     * @param CFG    - the config to use
     */
    Hoglins(ExtraHardMode plugin, RootConfig CFG)
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
     * When an Entity spawns: Spawn a Hoglin sometimes instead of a Zombified Piglin in crimson forests
     *
     * @param event which occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        if (!shouldReplace(event))
            return;

        final int hoglinSpawnPercent = CFG.getInt(RootNode.BONUS_HOGLIN_SPAWN_PERCENT, event.getLocation().getWorld().getName());
        if (plugin.random(hoglinSpawnPercent))
        {
            event.setCancelled(true);
            EntityHelper.spawn(event.getLocation(), EntityType.HOGLIN);
        }
    }


    /**
     * Whether this spawn is a natural zombified piglin spawn in a crimson forest
     *
     * @param event - the spawn event to check
     *
     * @return true if a Hoglin may be spawned instead
     */
    boolean shouldReplace(CreatureSpawnEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (EntityHelper.isMarkedAsOurs(entity))
            return false;
        if (entity.getType() != EntityType.ZOMBIFIED_PIGLIN || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return false;
        Location location = event.getLocation();
        World world = location.getWorld();
        return world != null && world.getEnvironment() == World.Environment.NETHER && BiomeHelper.isCrimsonForest(world.getBiome(location));
    }
}

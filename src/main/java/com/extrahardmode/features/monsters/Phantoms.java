package com.extrahardmode.features.monsters;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
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
 * Phantoms
 * <p/>
 * Bats which spawn on the surface at night are replaced by a Phantom, which dives at players that did not sleep for
 * a while
 * <p/>
 * Runs at {@link EventPriority#LOWEST} so a bat which is about to become a Phantom is not stolen by the
 * {@link Vex} feature.
 */
public class Phantoms extends ListenerModule
{
    private RootConfig CFG;


    public Phantoms(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Constructor for the unit tests, {@link #starting()} is not called
     *
     * @param plugin - the plugin
     * @param CFG    - the config to use
     */
    Phantoms(ExtraHardMode plugin, RootConfig CFG)
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
     * When an Entity spawns: Spawn a Phantom sometimes instead of a Bat at night
     *
     * @param event which occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        if (!shouldReplace(event))
            return;

        final int phantomSpawnPercent = CFG.getInt(RootNode.BONUS_PHANTOM_SPAWN_PERCENT, event.getLocation().getWorld().getName());
        if (plugin.random(phantomSpawnPercent))
        {
            event.setCancelled(true);
            EntityHelper.spawn(event.getLocation(), EntityType.PHANTOM);
        }
    }


    /**
     * Whether this spawn is a natural bat spawn on the surface during the night
     *
     * @param event - the spawn event to check
     *
     * @return true if a Phantom may be spawned instead
     */
    boolean shouldReplace(CreatureSpawnEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (EntityHelper.isMarkedAsOurs(entity))
            return false;
        if (entity.getType() != EntityType.BAT || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return false;
        Location location = event.getLocation();
        World world = location.getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL)
            return false;
        return Creaking.isNight(world) && location.getBlockY() >= world.getSeaLevel();
    }
}

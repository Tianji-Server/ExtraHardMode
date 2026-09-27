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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Creaking
 * <p/>
 * Zombies which spawn on the surface at night are replaced by a Creaking (1.21.4). A Creaking only moves while nobody
 * is looking at it and takes no damage, the only way to kill it is to find and destroy its heart.
 * <p/>
 * Experimental and disabled by default: spawned Creaking are activated for the closest player, so they at least
 * behave like a mob instead of standing around.
 */
public class Creaking extends ListenerModule
{
    /** Distance in blocks in which a player activates a spawned Creaking */
    private static final double ACTIVATION_RANGE = 32.0;

    private RootConfig CFG;


    public Creaking(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Constructor for the unit tests, {@link #starting()} is not called
     *
     * @param plugin - the plugin
     * @param CFG    - the config to use
     */
    Creaking(ExtraHardMode plugin, RootConfig CFG)
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
     * When an Entity spawns: Spawn a Creaking sometimes instead of a Zombie on the surface
     *
     * @param event which occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        final int creakingSpawnPercent = CFG.getInt(RootNode.BONUS_CREAKING_SPAWN_PERCENT, event.getLocation().getWorld().getName());
        if (creakingSpawnPercent == 0)
            return;

        if (!shouldReplace(event))
            return;

        if (plugin.random(creakingSpawnPercent))
        {
            event.setCancelled(true);
            LivingEntity spawned = EntityHelper.spawn(event.getLocation(), EntityType.CREAKING);
            if (spawned instanceof org.bukkit.entity.Creaking)
            {
                Player target = closestPlayer(event.getLocation());
                if (target != null)
                    ((org.bukkit.entity.Creaking) spawned).activate(target);
            }
        }
    }


    /**
     * Whether this spawn is a natural zombie spawn on the surface during the night
     *
     * @param event - the spawn event to check
     *
     * @return true if a Creaking may be spawned instead
     */
    boolean shouldReplace(CreatureSpawnEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (EntityHelper.isMarkedAsOurs(entity))
            return false;
        if (entity.getType() != EntityType.ZOMBIE || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return false;
        Location location = event.getLocation();
        World world = location.getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL)
            return false;
        //Creaking are a night mob and should stay on the surface
        return isNight(world) && location.getBlockY() >= world.getSeaLevel() && location.getBlock().getLightFromBlocks() == 0;
    }


    /**
     * @param world - world to check the time of
     *
     * @return true if it is night in the given world
     */
    static boolean isNight(World world)
    {
        long time = world.getTime();
        return time >= 13000 && time < 23000;
    }


    /**
     * @param location - location to search around
     *
     * @return the closest player in {@link #ACTIVATION_RANGE} blocks
     */
    private static Player closestPlayer(Location location)
    {
        Player closest = null;
        double closestDistance = ACTIVATION_RANGE * ACTIVATION_RANGE;
        for (Player player : location.getWorld().getPlayers())
        {
            if (!player.getWorld().equals(location.getWorld()))
                continue;
            double distance = player.getLocation().distanceSquared(location);
            if (distance < closestDistance)
            {
                closestDistance = distance;
                closest = player;
            }
        }
        return closest;
    }
}

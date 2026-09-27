package com.extrahardmode.features.monsters;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.EntityHelper;
import com.extrahardmode.service.ListenerModule;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Warden
 * <p/>
 * Zombies which spawn in the deepest caves are replaced by a Warden (1.19).
 * <p/>
 * This is disabled by default since the Warden is stronger than everything else in the game and there is no way of
 * fighting it. Set "Warden.Bonus Spawn Percent" to enable it.
 */
public class Warden extends ListenerModule
{
    private RootConfig CFG;


    public Warden(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Constructor for the unit tests, {@link #starting()} is not called
     *
     * @param plugin - the plugin
     * @param CFG    - the config to use
     */
    Warden(ExtraHardMode plugin, RootConfig CFG)
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
     * When an Entity spawns: Spawn a Warden sometimes instead of a Zombie in the deepest caves
     *
     * @param event which occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        final int maxY = CFG.getInt(RootNode.WARDEN_MAX_Y, event.getLocation().getWorld().getName());
        if (maxY == 0) //disabled
            return;

        final int wardenSpawnPercent = CFG.getInt(RootNode.BONUS_WARDEN_SPAWN_PERCENT, event.getLocation().getWorld().getName());
        if (wardenSpawnPercent == 0) //no need to check the rest
            return;

        if (!shouldReplace(event, maxY))
            return;

        if (plugin.random(wardenSpawnPercent))
        {
            event.setCancelled(true);
            LivingEntity warden = EntityHelper.spawn(event.getLocation(), EntityType.WARDEN);
            if (warden != null)
                //don't let it despawn while the players are still digging towards it
                warden.setRemoveWhenFarAway(false);
        }
    }


    /**
     * Whether this spawn is a natural zombie spawn below the given Y level
     *
     * @param event - the spawn event to check
     * @param maxY  - Y level below which a Warden may spawn, 0 disables the replacement
     *
     * @return true if a Warden may be spawned instead
     */
    boolean shouldReplace(CreatureSpawnEvent event, int maxY)
    {
        LivingEntity entity = event.getEntity();
        if (EntityHelper.isMarkedAsOurs(entity))
            return false;
        if (entity.getType() != EntityType.ZOMBIE || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return false;
        World world = event.getLocation().getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL)
            return false;
        return maxY != 0 && event.getLocation().getBlockY() < maxY;
    }
}

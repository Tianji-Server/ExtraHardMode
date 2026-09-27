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
 * Breeze
 * <p/>
 * Skeletons which spawn deep underground are replaced by a Breeze (1.21), which shoots wind charges at players
 */
public class Breeze extends ListenerModule
{
    private RootConfig CFG;


    public Breeze(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Constructor for the unit tests, {@link #starting()} is not called
     *
     * @param plugin - the plugin
     * @param CFG    - the config to use
     */
    Breeze(ExtraHardMode plugin, RootConfig CFG)
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
     * When an Entity spawns: Spawn a Breeze sometimes instead of a Skeleton deep underground
     *
     * @param event which occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        final int maxY = CFG.getInt(RootNode.BREEZE_MAX_Y, event.getLocation().getWorld().getName());
        if (maxY == 0) //disabled
            return;

        if (!shouldReplace(event, maxY))
            return;

        final int breezeSpawnPercent = CFG.getInt(RootNode.BONUS_BREEZE_SPAWN_PERCENT, event.getLocation().getWorld().getName());
        if (plugin.random(breezeSpawnPercent))
        {
            event.setCancelled(true);
            EntityHelper.spawn(event.getLocation(), EntityType.BREEZE);
        }
    }


    /**
     * Whether this spawn is a natural skeleton spawn below the given Y level
     *
     * @param event - the spawn event to check
     * @param maxY  - Y level below which a Breeze may spawn, 0 disables the replacement
     *
     * @return true if a Breeze may be spawned instead
     */
    boolean shouldReplace(CreatureSpawnEvent event, int maxY)
    {
        LivingEntity entity = event.getEntity();
        if (EntityHelper.isMarkedAsOurs(entity))
            return false;
        if (entity.getType() != EntityType.SKELETON || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return false;
        World world = event.getLocation().getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL)
            return false;
        return maxY != 0 && event.getLocation().getBlockY() < maxY;
    }
}

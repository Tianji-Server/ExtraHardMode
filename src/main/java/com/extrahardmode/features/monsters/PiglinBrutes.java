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
 * PiglinBrutes
 * <p/>
 * Zombified piglins which spawn in the nether wastes are replaced by a Piglin Brute.
 * <p/>
 * Unlike a zombified piglin a brute can not be calmed down by wearing gold armor and hits hard enough to kill a
 * player in diamond armor within a few hits
 */
public class PiglinBrutes extends ListenerModule
{
    private RootConfig CFG;


    public PiglinBrutes(ExtraHardMode plugin)
    {
        super(plugin);
    }


    /**
     * Constructor for the unit tests, {@link #starting()} is not called
     *
     * @param plugin - the plugin
     * @param CFG    - the config to use
     */
    PiglinBrutes(ExtraHardMode plugin, RootConfig CFG)
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
     * When an Entity spawns: Spawn a Piglin Brute sometimes instead of a Zombified Piglin in the nether wastes
     *
     * @param event which occurred
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        if (!shouldReplace(event))
            return;

        final int bruteSpawnPercent = CFG.getInt(RootNode.BONUS_PIGLIN_BRUTE_SPAWN_PERCENT, event.getLocation().getWorld().getName());
        if (plugin.random(bruteSpawnPercent))
        {
            event.setCancelled(true);
            EntityHelper.spawn(event.getLocation(), EntityType.PIGLIN_BRUTE);
        }
    }


    /**
     * Whether this spawn is a natural zombified piglin spawn in the nether wastes
     *
     * @param event - the spawn event to check
     *
     * @return true if a Piglin Brute may be spawned instead
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
        return world != null && world.getEnvironment() == World.Environment.NETHER && BiomeHelper.isNetherWastes(world.getBiome(location));
    }
}

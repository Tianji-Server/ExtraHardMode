package com.extrahardmode.mocks;


import com.destroystokyo.paper.SkinParts;
import io.papermc.paper.InternalAPIBridge;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.entity.poi.PoiType;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.world.damagesource.CombatEntry;
import io.papermc.paper.world.damagesource.FallLocationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attributable;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pose;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Minimal {@link InternalAPIBridge} for the unit tests.
 * <p/>
 * paper-api resolves a couple of classes - most notably {@link Biome} - through this interface, which only has an
 * implementation inside a real server. Without it every {@code Biome} constant (and therefore the whole interface)
 * fails to initialize as soon as a test touches one.
 * <p/>
 * Only the biome factory is answered, everything else throws since no test needs it.
 */
public final class TestInternalAPIBridge implements InternalAPIBridge
{
    private static final String UNSUPPORTED = "Not supported in the unit tests";


    @Deprecated
    @Override
    public Biome constructLegacyCustomBiome()
    {
        //The Biome.CUSTOM constant: the tests never use it, a null keeps this stub free of further dependencies
        return null;
    }


    @Override
    public CombatEntry createCombatEntry(LivingEntity entity, DamageSource source, float damage)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public CombatEntry createCombatEntry(DamageSource source, float damage, FallLocationType fallLocation, float fallDistance)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public Predicate<CommandSourceStack> restricted(Predicate<CommandSourceStack> predicate)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public ResolvableProfile defaultMannequinProfile()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public SkinParts.Mutable allSkinParts()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public Component defaultMannequinDescription()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public <MODERN, LEGACY> GameRule<LEGACY> legacyGameRuleBridge(GameRule<MODERN> modern, Function<LEGACY, MODERN> toModern,
                                                                 Function<MODERN, LEGACY> toLegacy, Class<LEGACY> legacyClass)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public Set<Pose> validMannequinPoses()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public PoiType.Occupancy createOccupancy(String name)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public DamageSource.Builder createDamageSourceBuilder(DamageType damageType)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public DamageEffect getDamageEffect(String key)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public String getTranslationKey(EntityType entityType)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public SpawnCategory getSpawnCategory(EntityType entityType)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public ItemStack deserializeItem(byte[] data)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public boolean hasDefaultEntityAttributes(NamespacedKey key)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public Attributable getDefaultEntityAttributes(NamespacedKey key)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public String getStatisticCriteriaKey(Statistic statistic)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public LifecycleEventManager<Plugin> createPluginLifecycleEventManager(JavaPlugin plugin, BooleanSupplier registrationCheck)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public ItemStack createEmptyStack()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public Component resolveWithContext(Component component, CommandSender context, Entity source, boolean renderInsideComponent) throws IOException
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }


    @Override
    public ComponentFlattener componentFlattener()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }
}

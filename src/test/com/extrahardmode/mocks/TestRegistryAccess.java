package com.extrahardmode.mocks;


import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;

/**
 * Minimal {@link RegistryAccess} for the unit tests.
 * <p/>
 * paper-api resolves every {@link Registry} field through this interface and only has an implementation inside a
 * real server, so the tests provide their own through {@code META-INF/services/io.papermc.paper.registry.RegistryAccess}.
 * <p/>
 * The item/block/effect registries are backed by generated stubs (paper-api asks them to decide whether a
 * {@code Material} is an item or a block), everything else gets an empty registry.
 */
public final class TestRegistryAccess implements RegistryAccess
{
    /** Registry key of the potion effect registry */
    private static final String MOB_EFFECT = "mob_effect";

    /** Registry key of the item registry */
    private static final String ITEM = "item";

    /** Registry key of the block registry */
    private static final String BLOCK = "block";

    /** Registry key of the biome registry, {@code RegistryKey.BIOME} is named "worldgen/biome" */
    private static final String BIOME = "worldgen/biome";


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Registry<T> getRegistry(Class<T> registryClass)
    {
        if (PotionEffectType.class.equals(registryClass))
            return (Registry<T>) BukkitTestBootstrap.createPotionRegistry();
        if (ItemType.class.equals(registryClass))
            return (Registry<T>) BukkitTestBootstrap.createTypeRegistry(ItemType.class);
        if (BlockType.class.equals(registryClass))
            return (Registry<T>) BukkitTestBootstrap.createTypeRegistry(BlockType.class);
        if (Biome.class.equals(registryClass))
            return (Registry<T>) BukkitTestBootstrap.createBiomeRegistry();
        return (Registry<T>) BukkitTestBootstrap.createFallbackRegistry();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Registry<T> getRegistry(RegistryKey<T> registryKey)
    {
        String key = registryKey != null ? registryKey.key().value() : null;
        if (MOB_EFFECT.equals(key))
            return (Registry<T>) BukkitTestBootstrap.createPotionRegistry();
        if (ITEM.equals(key))
            return (Registry<T>) BukkitTestBootstrap.createTypeRegistry(ItemType.class);
        if (BLOCK.equals(key))
            return (Registry<T>) BukkitTestBootstrap.createTypeRegistry(BlockType.class);
        if (BIOME.equals(key))
            return (Registry<T>) BukkitTestBootstrap.createBiomeRegistry();
        return (Registry<T>) BukkitTestBootstrap.createFallbackRegistry();
    }
}

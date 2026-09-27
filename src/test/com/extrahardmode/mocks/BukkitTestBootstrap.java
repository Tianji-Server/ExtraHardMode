package com.extrahardmode.mocks;


import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Minimal Bukkit bootstrap for tests that now touch registry-backed API.
 */
public final class BukkitTestBootstrap
{
    private BukkitTestBootstrap()
    {
    }


    public static void install()
    {
        clearServer();

        Server server = mock(Server.class);
        Logger logger = Logger.getLogger(BukkitTestBootstrap.class.getName());
        ItemFactory itemFactory = mock(ItemFactory.class);
        Map<Class<?>, Registry<?>> bootstrapRegistries = new ConcurrentHashMap<Class<?>, Registry<?>>();

        when(server.getLogger()).thenReturn(logger);
        when(server.getName()).thenReturn("TestBukkit");
        when(server.getVersion()).thenReturn("test");
        when(server.getBukkitVersion()).thenReturn("test");
        when(server.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.asMetaFor(any(ItemMeta.class), any(Material.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(itemFactory.getItemMeta(any(Material.class))).thenReturn(null);

        when(server.getRegistry(any())).thenAnswer(invocation ->
        {
            Class<?> registryClass = invocation.getArgument(0);
            return resolveBootstrapRegistry(bootstrapRegistries, registryClass);
        });

        Bukkit.setServer(server);

        Registry<?> materialRegistry = Registry.MATERIAL;
        Registry<?> entityRegistry = Registry.ENTITY_TYPE;
        Registry<?> effectRegistry = Registry.EFFECT;

        when(server.getRegistry(any())).thenAnswer(invocation ->
        {
            Class<?> registryClass = invocation.getArgument(0);
            if (Material.class.equals(registryClass))
                return materialRegistry;
            if (EntityType.class.equals(registryClass))
                return entityRegistry;
            if (PotionEffectType.class.equals(registryClass))
                return effectRegistry;
            return resolveBootstrapRegistry(bootstrapRegistries, registryClass);
        });
    }


    public static void reset()
    {
        clearServer();
    }


    @SuppressWarnings("unchecked")
    static <T extends org.bukkit.Keyed> Registry<T> createFallbackRegistry()
    {
        return (Registry<T>) Proxy.newProxyInstance(
                BukkitTestBootstrap.class.getClassLoader(),
                new Class<?>[]{Registry.class},
                new EmptyRegistryHandler());
    }


    @SuppressWarnings("unchecked")
    static Registry<PotionEffectType> createPotionRegistry()
    {
        return (Registry<PotionEffectType>) Proxy.newProxyInstance(
                BukkitTestBootstrap.class.getClassLoader(),
                new Class<?>[]{Registry.class},
                new PotionRegistryHandler());
    }


    private static Registry<?> resolveBootstrapRegistry(Map<Class<?>, Registry<?>> bootstrapRegistries, Class<?> registryClass)
    {
        if (registryClass == null)
            return createFallbackRegistry();

        Registry<?> registry = bootstrapRegistries.get(registryClass);
        if (registry != null)
            return registry;

        Registry<?> createdRegistry = PotionEffectType.class.equals(registryClass) ? createPotionRegistry() : createFallbackRegistry();
        Registry<?> existingRegistry = bootstrapRegistries.putIfAbsent(registryClass, createdRegistry);
        return existingRegistry != null ? existingRegistry : createdRegistry;
    }


    private static PotionEffectType getOrCreateEffect(Map<String, PotionEffectType> effects, NamespacedKey key)
    {
        return effects.computeIfAbsent(key.toString(), ignored ->
        {
            int id = effects.size() + 1;
            return new TestPotionEffectType(id, key);
        });
    }


    private static void clearServer()
    {
        try
        {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, null);
        } catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException("Unable to reset Bukkit server singleton for tests", e);
        }
    }


    private static final class EmptyRegistryHandler implements InvocationHandler
    {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            String methodName = method.getName();
            if ("get".equals(methodName) || "match".equals(methodName))
                return null;
            if ("getOrThrow".equals(methodName))
                throw new IllegalArgumentException("Missing test registry entry");
            if ("stream".equals(methodName))
                return Stream.empty();
            if ("iterator".equals(methodName))
                return Stream.empty().iterator();
            if ("toString".equals(methodName))
                return "EmptyRegistryProxy";
            if ("hashCode".equals(methodName))
                return System.identityHashCode(proxy);
            if ("equals".equals(methodName))
                return proxy == args[0];
            throw new UnsupportedOperationException("Unsupported registry method: " + methodName);
        }
    }


    private static final class PotionRegistryHandler implements InvocationHandler
    {
        private final Map<String, PotionEffectType> effects = new ConcurrentHashMap<String, PotionEffectType>();


        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            String methodName = method.getName();
            if ("get".equals(methodName) || "getOrThrow".equals(methodName))
                return getOrCreateEffect(effects, toNamespacedKey(args[0]));
            if ("match".equals(methodName))
            {
                String input = (String) args[0];
                if (input == null)
                    return null;

                NamespacedKey key = NamespacedKey.fromString(input.toLowerCase(Locale.ROOT));
                if (key == null)
                    key = NamespacedKey.minecraft(input.toLowerCase(Locale.ROOT));
                return getOrCreateEffect(effects, key);
            }
            if ("stream".equals(methodName))
                return effects.values().stream();
            if ("iterator".equals(methodName))
                return effects.values().iterator();
            if ("toString".equals(methodName))
                return "PotionRegistryProxy";
            if ("hashCode".equals(methodName))
                return System.identityHashCode(proxy);
            if ("equals".equals(methodName))
                return proxy == args[0];
            throw new UnsupportedOperationException("Unsupported registry method: " + methodName);
        }
    }


    /** paper-api looks registries up with either a NamespacedKey or with any adventure Key */
    private static NamespacedKey toNamespacedKey(Object key)
    {
        if (key instanceof NamespacedKey)
            return (NamespacedKey) key;
        if (key instanceof Key)
            return NamespacedKey.fromString(((Key) key).asString());
        return NamespacedKey.fromString(String.valueOf(key));
    }


    /**
     * Registry that hands out stubs of the given type. paper-api resolves Material#asItemType()/asBlockType()
     * through the item/block registry, so an empty registry would make every Material "not an item".
     */
    @SuppressWarnings("unchecked")
    static <T extends org.bukkit.Keyed> Registry<T> createTypeRegistry(final Class<?> typeClass)
    {
        return (Registry<T>) Proxy.newProxyInstance(
                BukkitTestBootstrap.class.getClassLoader(),
                new Class<?>[]{Registry.class},
                new TypeRegistryHandler(typeClass));
    }


    private static final class TypeRegistryHandler implements InvocationHandler
    {
        private final Class<?> typeClass;
        private final Map<String, Object> types = new ConcurrentHashMap<String, Object>();


        private TypeRegistryHandler(Class<?> typeClass)
        {
            this.typeClass = typeClass;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            String methodName = method.getName();
            if ("get".equals(methodName) || "getOrThrow".equals(methodName))
                return getOrCreateType(toNamespacedKey(args[0]));
            if ("match".equals(methodName))
            {
                String input = args[0] == null ? null : String.valueOf(args[0]).toLowerCase(Locale.ROOT);
                if (input == null)
                    return null;

                NamespacedKey key = NamespacedKey.fromString(input);
                return getOrCreateType(key != null ? key : NamespacedKey.minecraft(input));
            }
            if ("stream".equals(methodName))
                return types.values().stream();
            if ("iterator".equals(methodName))
                return types.values().iterator();
            if ("toString".equals(methodName))
                return typeClass.getSimpleName() + "RegistryProxy";
            if ("hashCode".equals(methodName))
                return System.identityHashCode(proxy);
            if ("equals".equals(methodName))
                return proxy == args[0];
            throw new UnsupportedOperationException("Unsupported registry method: " + methodName);
        }


        private Object getOrCreateType(NamespacedKey key)
        {
            if (key == null)
                return null;

            return types.computeIfAbsent(key.toString(), ignored -> Proxy.newProxyInstance(
                    BukkitTestBootstrap.class.getClassLoader(),
                    new Class<?>[]{typeClass},
                    new TypeHandler(key)));
        }
    }


    /** Stub for ItemType/BlockType, answers the few queries the plugin asks and stays neutral for the rest */
    private static final class TypeHandler implements InvocationHandler
    {
        private final NamespacedKey key;


        private TypeHandler(NamespacedKey key)
        {
            this.key = key;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            String methodName = method.getName();
            if ("getMaxStackSize".equals(methodName))
                return 64;
            if ("getMaxDurability".equals(methodName))
                return (short) 1561;
            if ("createItemStack".equals(methodName))
                return new TestItemStack(Material.matchMaterial(key.getKey()), args.length > 0 ? (Integer) args[0] : 1);
            if ("key".equals(methodName) || "getKey".equals(methodName))
                return key;
            if ("getTranslationKey".equals(methodName) || "translationKey".equals(methodName) || "getName".equals(methodName))
                return key.getKey();
            if ("asMaterial".equals(methodName))
                return Material.matchMaterial(key.getKey());
            if ("toString".equals(methodName))
                return "TestType(" + key + ")";
            if ("hashCode".equals(methodName))
                return System.identityHashCode(proxy);
            if ("equals".equals(methodName))
                return proxy == args[0];
            return defaultValue(method.getReturnType());
        }


        private static Object defaultValue(Class<?> returnType)
        {
            if (!returnType.isPrimitive() || returnType == void.class)
                return null;
            if (returnType == boolean.class)
                return Boolean.FALSE;
            if (returnType == char.class)
                return (char) 0;
            if (returnType == float.class)
                return 0.0F;
            if (returnType == double.class)
                return 0.0D;
            if (returnType == long.class)
                return 0L;
            if (returnType == byte.class)
                return (byte) 0;
            if (returnType == short.class)
                return (short) 0;
            return 0;
        }
    }


    /**
     * Registry for the biome registry.
     * <p/>
     * The {@link Biome} constants are resolved through this registry while the interface is initialized, so without a
     * registry that answers the lookup the interface - and with it every biome constant - fails to initialize in the
     * unit tests.
     */
    @SuppressWarnings("unchecked")
    static Registry<Biome> createBiomeRegistry()
    {
        return (Registry<Biome>) Proxy.newProxyInstance(
                BukkitTestBootstrap.class.getClassLoader(),
                new Class<?>[]{Registry.class},
                new BiomeRegistryHandler());
    }


    /** Registry which hands out biome stubs, cached by the key the caller asked for */
    private static final class BiomeRegistryHandler implements InvocationHandler
    {
        private final Map<String, Object> biomes = new ConcurrentHashMap<String, Object>();


        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            String methodName = method.getName();
            if ("get".equals(methodName) || "getOrThrow".equals(methodName) || "getOrNull".equals(methodName))
                return getOrCreateBiome(args != null && args.length > 0 ? args[0] : null);
            if ("stream".equals(methodName))
                return biomes.values().stream();
            if ("iterator".equals(methodName))
                return biomes.values().iterator();
            if ("toString".equals(methodName))
                return "BiomeRegistryProxy";
            if ("hashCode".equals(methodName))
                return System.identityHashCode(proxy);
            if ("equals".equals(methodName))
                return proxy == args[0];
            throw new UnsupportedOperationException("Unsupported registry method: " + methodName);
        }


        private Object getOrCreateBiome(Object key)
        {
            if (key == null)
                return null;

            return biomes.computeIfAbsent(String.valueOf(key), ignored -> Proxy.newProxyInstance(
                    BukkitTestBootstrap.class.getClassLoader(),
                    new Class<?>[]{Biome.class},
                    new BiomeHandler(toBiomeKey(key))));
        }


        /** paper-api asks with an adventure key, a biome stub only ever needs namespace and value */
        private static NamespacedKey toBiomeKey(Object key)
        {
            if (key instanceof NamespacedKey)
                return (NamespacedKey) key;
            if (key instanceof Key)
                return new NamespacedKey(((Key) key).namespace(), ((Key) key).value());
            return new NamespacedKey(NamespacedKey.MINECRAFT, String.valueOf(key));
        }
    }


    /** Stub for a Biome, only its key is of interest to the plugin */
    private static final class BiomeHandler implements InvocationHandler
    {
        private final NamespacedKey key;


        private BiomeHandler(NamespacedKey key)
        {
            this.key = key;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            String methodName = method.getName();
            if ("key".equals(methodName) || "getKey".equals(methodName))
                return key;
            if ("namespace".equals(methodName))
                return key.getNamespace();
            if ("value".equals(methodName))
                return key.getKey();
            if ("getTranslationKey".equals(methodName) || "translationKey".equals(methodName) || "getName".equals(methodName))
                return key.getKey();
            if ("toString".equals(methodName))
                return "TestBiome(" + key + ")";
            if ("hashCode".equals(methodName))
                return System.identityHashCode(proxy);
            if ("equals".equals(methodName))
                return proxy == args[0];
            return defaultValueFor(method.getReturnType());
        }
    }


    /** @return the default value for a primitive return type, null for everything else */
    static Object defaultValueFor(Class<?> returnType)
    {
        if (!returnType.isPrimitive() || returnType == void.class)
            return null;
        if (returnType == boolean.class)
            return Boolean.FALSE;
        if (returnType == char.class)
            return (char) 0;
        if (returnType == float.class)
            return 0.0F;
        if (returnType == double.class)
            return 0.0D;
        if (returnType == long.class)
            return 0L;
        if (returnType == byte.class)
            return (byte) 0;
        if (returnType == short.class)
            return (short) 0;
        return 0;
    }


    /**
     * Stub item stack for the tests. paper-api's ItemStack only holds a delegate that is created by the ItemType,
     * which needs a running server, so the stubs answer the queries EHM asks themselves.
     */
    private static final class TestItemStack extends ItemStack
    {
        private final Material type;
        private int amount;
        private short durability;


        private TestItemStack(Material type, int amount)
        {
            this.type = type;
            this.amount = amount;
        }


        @Override
        public Material getType()
        {
            return type;
        }


        @Override
        public int getAmount()
        {
            return amount;
        }


        @Override
        public void setAmount(int amount)
        {
            this.amount = amount;
        }


        @Override
        public int getMaxStackSize()
        {
            return 64;
        }


        @Override
        public short getDurability()
        {
            return durability;
        }


        @Override
        public void setDurability(short durability)
        {
            this.durability = durability;
        }


        @Override
        public boolean hasItemMeta()
        {
            return false;
        }


        @Override
        public String toString()
        {
            return amount + " x " + type;
        }
    }


    private static final class TestPotionEffectType extends PotionEffectType
    {
        private final int id;
        private final NamespacedKey key;
        private final String name;


        private TestPotionEffectType(int id, NamespacedKey key)
        {
            this.id = id;
            this.key = key;
            this.name = key.getKey().toUpperCase(Locale.ROOT);
        }


        @Override
        public PotionEffect createEffect(int duration, int amplifier)
        {
            return new PotionEffect(this, duration, amplifier);
        }


        @Override
        public boolean isInstant()
        {
            return false;
        }


        @Override
        public PotionEffectTypeCategory getCategory()
        {
            return PotionEffectTypeCategory.NEUTRAL;
        }


        @Override
        public Color getColor()
        {
            return Color.WHITE;
        }


        @Override
        public NamespacedKey getKey()
        {
            return key;
        }


        @Override
        public double getDurationModifier()
        {
            return 1.0;
        }


        @Override
        public int getId()
        {
            return id;
        }


        @Override
        public String getName()
        {
            return name;
        }


        @Override
        public String getTranslationKey()
        {
            return "effect.minecraft." + key.getKey();
        }


        @Override
        public String translationKey()
        {
            return "effect.minecraft." + key.getKey();
        }


        @Override
        public PotionEffectType.Category getEffectCategory()
        {
            return PotionEffectType.Category.NEUTRAL;
        }


        @Override
        public Map<Attribute, AttributeModifier> getEffectAttributes()
        {
            return Map.of();
        }


        @Override
        public double getAttributeModifierAmount(Attribute attribute, int amplifier)
        {
            return 0.0;
        }
    }
}

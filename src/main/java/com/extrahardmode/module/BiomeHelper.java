package com.extrahardmode.module;


import net.kyori.adventure.key.Key;
import org.bukkit.block.Biome;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Bucket checks for biomes.
 * <p/>
 * Biomes are matched by their key instead of the {@link Biome} constants because those constants are registry backed
 * (which makes them unusable outside of a running server) and because a key based match also works for biomes added
 * by datapacks.
 */
public final class BiomeHelper
{
    /** Swamps and other humid biomes, home of the Bogged */
    private static final Set<String> SWAMPY = keys("swamp", "mangrove_swamp");

    /** Hot and dry biomes, home of the Parched */
    private static final Set<String> DESERT = keys("desert", "badlands", "eroded_badlands", "wooded_badlands");

    /** Crimson forests, where a Hoglin is at home */
    private static final Set<String> CRIMSON_FOREST = keys("crimson_forest");

    /** Nether biomes in which the zombified piglins are not already busy fighting hoglins */
    private static final Set<String> NETHER_WASTES = keys("nether_wastes", "basalt_deltas", "soul_sand_valley");


    private BiomeHelper()
    {
    }


    /**
     * @param biome - biome to check, may be null
     *
     * @return true if the biome is a swamp or a mangrove swamp
     */
    public static boolean isSwampy(Biome biome)
    {
        return matches(biome, SWAMPY);
    }


    /**
     * @param biome - biome to check, may be null
     *
     * @return true if the biome is a desert or a badlands variant
     */
    public static boolean isDesert(Biome biome)
    {
        return matches(biome, DESERT);
    }


    /**
     * @param biome - biome to check, may be null
     *
     * @return true if the biome is a crimson forest
     */
    public static boolean isCrimsonForest(Biome biome)
    {
        return matches(biome, CRIMSON_FOREST);
    }


    /**
     * @param biome - biome to check, may be null
     *
     * @return true if the biome is a "neutral" nether biome
     */
    public static boolean isNetherWastes(Biome biome)
    {
        return matches(biome, NETHER_WASTES);
    }


    /**
     * @param biome - biome to compare, may be null
     * @param names - lowercase biome names, the part after the namespace
     *
     * @return true if the biome is one of the given biomes
     */
    private static boolean matches(Biome biome, Set<String> names)
    {
        if (biome == null)
            return false;
        Key key = biome.key();
        return key != null && names.contains(key.value());
    }


    private static Set<String> keys(String... names)
    {
        return new HashSet<String>(Arrays.asList(names));
    }
}

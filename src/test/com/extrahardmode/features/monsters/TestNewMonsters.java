/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.features.monsters;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.mocks.BukkitTestBootstrap;
import com.extrahardmode.mocks.MockBlock;
import com.extrahardmode.mocks.MockExtraHardMode;
import com.extrahardmode.mocks.MockWorld;
import com.extrahardmode.mocks.events.MockCreatureSpawnEvent;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Tests for the monster variants of newer Minecraft versions, e.g Bogged, Breeze, Warden or Phantoms
 */
public class TestNewMonsters
{
    @BeforeClass
    public static void beforeClass()
    {
        BukkitTestBootstrap.install();
    }


    @AfterClass
    public static void afterClass()
    {
        BukkitTestBootstrap.reset();
    }


    private final ExtraHardMode plugin = new MockExtraHardMode().get();

    private final RootConfig CFG = new RootConfig(plugin);


    @Before
    public void prepare()
    {
        //every percentage check succeeds, the percentages themselves are the business of the config
        when(plugin.random(anyInt())).thenReturn(true);
    }


    @Test
    public void boggedReplacesSwampSkeletons()
    {
        MockCreatureSpawnEvent event = spawn(EntityType.SKELETON, Biome.SWAMP);

        new Bogged(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("A skeleton in a swamp should become a Bogged", event.isCancelled());
    }


    @Test
    public void boggedIgnoresDesertSkeletons()
    {
        MockCreatureSpawnEvent event = spawn(EntityType.SKELETON, Biome.DESERT);

        new Bogged(plugin, CFG).onEntitySpawn(event.get());

        assertFalse("A Parched is the desert skeleton, not a Bogged", event.isCancelled());
    }


    @Test
    public void boggedIgnoresNonSkeletons()
    {
        MockCreatureSpawnEvent event = spawn(EntityType.ZOMBIE, Biome.SWAMP);

        new Bogged(plugin, CFG).onEntitySpawn(event.get());

        assertFalse("Only skeletons know how to hold a bow", event.isCancelled());
    }


    @Test
    public void parchedReplacesDesertSkeletons()
    {
        MockCreatureSpawnEvent event = spawn(EntityType.SKELETON, Biome.BADLANDS);

        new Parched(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("A skeleton in the badlands should become a Parched", event.isCancelled());
    }


    @Test
    public void breezeReplacesDeepSkeletons()
    {
        CFG.set("world", RootNode.BREEZE_MAX_Y, -16);
        MockCreatureSpawnEvent event = spawn(EntityType.SKELETON, Biome.PLAINS);
        event.getLocation().setBlockY(-30);

        new Breeze(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("A skeleton deep below the configured Y level should become a Breeze", event.isCancelled());
    }


    @Test
    public void breezeIgnoresShallowSkeletons()
    {
        CFG.set("world", RootNode.BREEZE_MAX_Y, -16);
        MockCreatureSpawnEvent event = spawn(EntityType.SKELETON, Biome.PLAINS);
        event.getLocation().setBlockY(0);

        new Breeze(plugin, CFG).onEntitySpawn(event.get());

        assertFalse("A Breeze in a normal cave would be ridiculous", event.isCancelled());
    }


    @Test
    public void breezeIsDisabledWithZeroMaxY()
    {
        CFG.set("world", RootNode.BREEZE_MAX_Y, 0);
        MockCreatureSpawnEvent event = spawn(EntityType.SKELETON, Biome.PLAINS);
        event.getLocation().setBlockY(-60);

        new Breeze(plugin, CFG).onEntitySpawn(event.get());

        assertFalse("A max Y of 0 disables the Breeze replacement", event.isCancelled());
    }


    @Test
    public void wardenIsDisabledByDefault()
    {
        CFG.set("world", RootNode.WARDEN_MAX_Y, -48);
        MockCreatureSpawnEvent event = spawn(EntityType.ZOMBIE, Biome.PLAINS);
        event.getLocation().setBlockY(-60);

        new Warden(plugin, CFG).onEntitySpawn(event.get());

        assertFalse("The Warden spawn percentage is 0 by default", event.isCancelled());
    }


    @Test
    public void wardenReplacesDeepZombies()
    {
        CFG.set("world", RootNode.WARDEN_MAX_Y, -48);
        CFG.set("world", RootNode.BONUS_WARDEN_SPAWN_PERCENT, 5);
        MockCreatureSpawnEvent event = spawn(EntityType.ZOMBIE, Biome.PLAINS);
        event.getLocation().setBlockY(-60);

        new Warden(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("A zombie in the deep dark should become a Warden", event.isCancelled());
    }


    @Test
    public void creakingReplacesNightZombies()
    {
        CFG.set("world", RootNode.BONUS_CREAKING_SPAWN_PERCENT, 5);
        MockCreatureSpawnEvent event = surfaceSpawn(EntityType.ZOMBIE, 18000);

        new Creaking(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("A zombie on the surface at night should become a Creaking", event.isCancelled());
    }


    @Test
    public void creakingIgnoresDaytimeZombies()
    {
        CFG.set("world", RootNode.BONUS_CREAKING_SPAWN_PERCENT, 5);
        MockCreatureSpawnEvent event = surfaceSpawn(EntityType.ZOMBIE, 6000);

        new Creaking(plugin, CFG).onEntitySpawn(event.get());

        assertFalse("A Creaking in broad daylight would just burn", event.isCancelled());
    }


    @Test
    public void phantomsReplaceNightBats()
    {
        CFG.set("world", RootNode.BONUS_PHANTOM_SPAWN_PERCENT, 5);
        MockCreatureSpawnEvent event = surfaceSpawn(EntityType.BAT, 18000);

        new Phantoms(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("A bat at night should become a Phantom", event.isCancelled());
    }


    @Test
    public void hoglinsReplaceCrimsonForestPigmen()
    {
        MockCreatureSpawnEvent event = netherSpawn(EntityType.ZOMBIFIED_PIGLIN, Biome.CRIMSON_FOREST);

        new Hoglins(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("The crimson forest should be hoglin territory", event.isCancelled());
    }


    @Test
    public void zoglinsReplaceNetherPigmen()
    {
        MockCreatureSpawnEvent event = netherSpawn(EntityType.ZOMBIFIED_PIGLIN, Biome.NETHER_WASTES);

        new Zoglins(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("Zoglins attack everything, they fit everywhere in the nether", event.isCancelled());
    }


    @Test
    public void piglinBrutesReplaceNetherWastePigmen()
    {
        MockCreatureSpawnEvent event = netherSpawn(EntityType.ZOMBIFIED_PIGLIN, Biome.NETHER_WASTES);

        new PiglinBrutes(plugin, CFG).onEntitySpawn(event.get());

        assertTrue("A Piglin Brute belongs into the nether wastes", event.isCancelled());
    }


    @Test
    public void piglinBrutesIgnoreCrimsonForests()
    {
        MockCreatureSpawnEvent event = netherSpawn(EntityType.ZOMBIFIED_PIGLIN, Biome.CRIMSON_FOREST);

        new PiglinBrutes(plugin, CFG).onEntitySpawn(event.get());

        assertFalse("The crimson forest is the business of the Hoglins", event.isCancelled());
    }


    @Test
    public void excludedTypesGetNoPackSpawns()
    {
        CFG.set("world", RootNode.MORE_MONSTERS_EXCLUDED_TYPES, Arrays.asList("WARDEN", "Breeze", "creaking"));
        MockWorld world = new MockWorld("world");
        MonsterRules rules = new MonsterRules(plugin, CFG);

        assertTrue("A naturally spawned Warden must not pull a pack of monsters", rules.isExcludedFromPackSpawns(world.get(), EntityType.WARDEN));
        assertTrue("Names in the list are matched case insensitively", rules.isExcludedFromPackSpawns(world.get(), EntityType.BREEZE));
        assertTrue("Names in the list are matched case insensitively", rules.isExcludedFromPackSpawns(world.get(), EntityType.CREAKING));
        assertFalse("An ordinary monster is the reason the pack spawns exist", rules.isExcludedFromPackSpawns(world.get(), EntityType.ZOMBIE));
    }


    @Test
    public void exclusionListHasSaneDefaults()
    {
        Object defaultExcluded = RootNode.MORE_MONSTERS_EXCLUDED_TYPES.getDefaultValue();

        assertTrue("The default exclusion list should contain the Warden", ((List) defaultExcluded).contains("WARDEN"));
        assertTrue("The default exclusion list should contain the Breeze", ((List) defaultExcluded).contains("BREEZE"));
        assertFalse("Zombies are supposed to spawn in packs", ((List) defaultExcluded).contains("ZOMBIE"));
    }


    @Test
    public void newNodesHaveExpectedDefaults()
    {
        assertEquals("Bogged should be enabled by default", Integer.valueOf(20), RootNode.BONUS_BOGGED_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Parched should be enabled by default", Integer.valueOf(20), RootNode.BONUS_PARCHED_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Breeze should be enabled by default", Integer.valueOf(5), RootNode.BONUS_BREEZE_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Warden is opt in", Integer.valueOf(0), RootNode.BONUS_WARDEN_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Creaking is opt in", Integer.valueOf(0), RootNode.BONUS_CREAKING_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Hoglins are enabled by default", Integer.valueOf(10), RootNode.BONUS_HOGLIN_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Zoglins are enabled by default", Integer.valueOf(5), RootNode.BONUS_ZOGLIN_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Piglin Brutes are enabled by default", Integer.valueOf(5), RootNode.BONUS_PIGLIN_BRUTE_SPAWN_PERCENT.getDefaultValue());
        assertEquals("Phantoms are enabled by default", Integer.valueOf(5), RootNode.BONUS_PHANTOM_SPAWN_PERCENT.getDefaultValue());
    }


    /**
     * A natural spawn of the given type in a world with the given biome, the world is an overworld by default
     */
    private static MockCreatureSpawnEvent spawn(EntityType type, Biome biome)
    {
        MockCreatureSpawnEvent event = new MockCreatureSpawnEvent(type, "world", CreatureSpawnEvent.SpawnReason.NATURAL);
        event.getWorld().setEnvironment(World.Environment.NORMAL);
        event.getWorld().setBiome(biome);
        return event;
    }


    /**
     * A natural spawn on the surface of the overworld, y 70, sea level 63
     */
    private static MockCreatureSpawnEvent surfaceSpawn(EntityType type, long time)
    {
        MockCreatureSpawnEvent event = spawn(type, Biome.PLAINS);
        MockWorld world = event.getWorld();
        world.setEnvironment(World.Environment.NORMAL);
        world.setTime(time);
        world.setSeaLevel(63);
        event.getLocation().setBlockY(70);
        event.getLocation().setBlock(new MockBlock().setWorld(world.get()));
        return event;
    }


    /**
     * A natural spawn in the nether with the given biome
     */
    private static MockCreatureSpawnEvent netherSpawn(EntityType type, Biome biome)
    {
        MockCreatureSpawnEvent event = spawn(type, biome);
        event.getWorld().setEnvironment(World.Environment.NETHER);
        return event;
    }
}

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

package com.extrahardmode.mocks;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks commonly used methods like
 * <pre>
 *     getName()
 * </pre>
 */
public class MockWorld
{
    /**
     * Our mocked World Object
     */
    private final World world = mock(World.class);

    /**
     * Environment of this world NORMAL/NETHER/END
     */
    private World.Environment environment;


    /**
     * Construct a new mocked World with a given name
     */
    public MockWorld(String name)
    {
        when(world.getName()).thenReturn(name);
    }


    /**
     * Set the Environment of this World
     */
    public void setEnvironment(World.Environment environment)
    {
        this.environment = environment;
        when(this.get().getEnvironment()).thenReturn(environment);
    }


    /**
     * Get the Environment of this World
     */
    public World.Environment getEnvironment()
    {
        return environment;
    }


    /**
     * Get the mocked Object
     */
    public World get()
    {
        return world;
    }


    /**
     * Set the biome {@link World#getBiome(Location)} returns
     *
     * @param biome - biome to return
     */
    public void setBiome(Biome biome)
    {
        when(world.getBiome(any(Location.class))).thenReturn(biome);
    }


    /**
     * Set the time of day of this world
     *
     * @param time - time in ticks, 13000 - 23000 is night
     */
    public void setTime(long time)
    {
        when(world.getTime()).thenReturn(time);
    }


    /**
     * Set the sea level of this world
     *
     * @param seaLevel - Y level of the sea
     */
    public void setSeaLevel(int seaLevel)
    {
        when(world.getSeaLevel()).thenReturn(seaLevel);
    }
}

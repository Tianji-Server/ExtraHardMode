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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic Location
 * <pre>
 *     mock getBlock(X/Y/Z)
 *     mock get(X/Y/Z)
 *     override mock getBlock()
 * </pre>
 */
public class MockLocation
{
    /**
     * Location Object
     */
    private final Location loc = mock(Location.class);

    /**
     * Block that resides at this Location
     */
    private MockBlock block;


    /**
     * A basic Constructor
     */
    public MockLocation(World world)
    {
        when(loc.getWorld()).thenReturn(world);
    }


    /**
     * Block at this Location
     */
    public MockBlock getBlock()
    {
        return block;
    }


    /**
     * Set the Block that is at this Location
     */
    public void setBlock(MockBlock block)
    {
        this.block = block;
        when(loc.getBlock()).thenReturn(block.get());
    }


    /**
     * Set the Y coordinate of this Location
     *
     * @param y - block Y coordinate
     *
     * @return this MockLocation for chaining
     */
    public MockLocation setBlockY(int y)
    {
        when(loc.getBlockY()).thenReturn(y);
        return this;
    }


    public Location get()
    {
        return loc;
    }
}

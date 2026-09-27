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

package com.extrahardmode.mocks.events;


import com.extrahardmode.mocks.MockBlock;
import com.extrahardmode.mocks.MockLivingEntity;
import com.extrahardmode.mocks.MockLocation;
import com.extrahardmode.mocks.MockWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocks frequently used  methods that are called on this event, by default
 * <pre>
 *     getWorld()
 *     getEntity()
 *     getSpawnReason()
 * </pre>
 * Generates a World Object, which can be retrieved via getWorld()
 */
public class MockCreatureSpawnEvent
{
    /**
     * Our mocked Event Object
     */
    private final CreatureSpawnEvent cse = mock(CreatureSpawnEvent.class);

    /**
     * World where this event occurred
     */
    private MockWorld world;

    /**
     * Entity of this Event
     */
    private MockLivingEntity entity;

    /**
     * Block where this Event occured
     */
    private MockBlock block;

    /**
     * Location where the Event occurred
     */
    private MockLocation location;

    /**
     * Whether the event got cancelled by the code under test
     */
    private boolean cancelled;


    /**
     * Constructor param entity Pass in an Entity with a mocked getWorld()-method
     *
     * @param type      of the spawned Entity
     * @param worldName name of the world where this Event occured
     * @param reason    what caused the event
     */
    public MockCreatureSpawnEvent(EntityType type, String worldName, CreatureSpawnEvent.SpawnReason reason)
    {
        world = new MockWorld(worldName);
        entity = new MockLivingEntity(world.get(), type);
        location = new MockLocation(world.get());
        setLocation(location);

        when(cse.getEntity()).thenReturn(entity.get());
        when(cse.getSpawnReason()).thenReturn(reason);
        when(cse.isCancelled()).thenAnswer(invocation -> cancelled);
        doAnswer(invocation ->
        {
            cancelled = invocation.getArgument(0);
            return null;
        }).when(cse).setCancelled(anyBoolean());
    }


    /**
     * Whether the code under test cancelled this event
     *
     * @return true if the spawn got cancelled
     */
    public boolean isCancelled()
    {
        return cse.isCancelled();
    }


    /**
     * Set the Entity we mocked
     */
    public void setEntity(MockLivingEntity entity)
    {
        this.entity = entity;
        when(this.get().getEntity()).thenReturn(entity.get());
    }


    /**
     * Get the MockEntity we set
     */
    public MockLivingEntity getEntity()
    {
        return entity;
    }


    /**
     * Set the MockWorld Object for this Event
     *
     * @param world MockWorld where this Event occured
     */
    public void setWorld(MockWorld world)
    {
        this.world = world;
    }


    /**
     * Get the mocked World Object where this Event took place
     */
    public MockWorld getWorld()
    {
        return world;
    }


    /**
     * Set the Location where the Event ocurred
     */
    public void setLocation(MockLocation location)
    {
        this.location = location;
        when(cse.getLocation()).thenReturn(location.get());
    }


    /**
     * Get the Location of the Event
     */
    public MockLocation getLocation()
    {
        return location;
    }


    /**
     * Get the mocked Event
     */
    public CreatureSpawnEvent get()
    {
        return cse;
    }
}

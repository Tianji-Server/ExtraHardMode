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

package com.extrahardmode.service.config;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.EHMModule;
import com.extrahardmode.service.config.customtypes.BlockRelationsList;
import com.extrahardmode.service.config.customtypes.PotionEffectHolder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modular configuration class that utilizes a ConfigNode enumeration as easy access and storage of configuration option values.
 *
 * @author Mitsugaru (original author)
 * @author Diemex (modifies to allow multiworld)
 */
public abstract class MultiWorldConfig extends EHMModule
{

    /**
     * For mods like MystCraft which allow Players to create their own dimensions, so the admin doesn't have to add worlds manually
     */
    protected boolean enabledForAll = false;

    /**
     * String that will enable the plugin in all worlds
     */
    public static final String ALL_WORLDS = "@all";

    private Table<String/*world*/, ConfigNode, Object> OPTIONS;

    /**
     * Already parsed material lists. Material names are expensive to parse (see {@link #materialFromName(String)}) and
     * {@link #getStringListAsMaterialList(ConfigNode, String)} is called from hot event handlers, so the parsed results
     * are kept until the config is set again.
     */
    private Table<String/*world*/, ConfigNode, List<Material>> parsedMaterialLists = HashBasedTable.create();


    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public MultiWorldConfig(ExtraHardMode plugin)
    {
        super(plugin);
        init();
    }


    /**
     * Inits Objects and deletes old ones at the same time
     */
    protected void init()
    {
        OPTIONS = HashBasedTable.create();
        parsedMaterialLists = HashBasedTable.create();
    }


    /**
     * Set a value for the given node and world
     *
     * @param world - World for the value
     * @param node  - ConfigNode for the given value
     * @param value - the Object to save
     */
    public void set(final String world, final ConfigNode node, Object value)
    {
        Validate.notNull(node, "Supplied ConfigNode was null - world: " + world + " value: " + value);
        Validate.notNull(world, "Supplied World was null - node: " + node + " value: " + value);
        //Any change invalidates the lazily parsed material lists
        parsedMaterialLists.clear();
        switch (node.getVarType())
        {
            case LIST:
            {
                if (value instanceof List)
                {
                    List list = (List) value;
                    OPTIONS.put(world, node, list);
                    break;
                }
            }
            case DOUBLE:
            {
                if (value instanceof Double)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case STRING:
            {
                if (value instanceof String)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case INTEGER:
            {
                if (value instanceof Integer || value instanceof Double)
                {
                    //fix error when double is provided which can be casted
                    if (value instanceof Double)
                        value = ((Double) value).intValue();
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case BOOLEAN:
            {
                if (value instanceof Boolean)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case POTION_EFFECT:
            {
                if (value instanceof PotionEffectHolder)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case MATERIAL:
            {
                if (value instanceof Material)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            case BLOCK_RELATION_LIST:
            {
                if (value instanceof BlockRelationsList)
                {
                    OPTIONS.put(world, node, value);
                    break;
                }
            }
            default:
            {
                OPTIONS.put(world, node, node.getDefaultValue());
                String inputClassName = value != null ? value.getClass().getName() : "null";
                throw new IllegalArgumentException(node.getPath() + " expects " + node.getVarType() + " but got " + inputClassName);
            }
        }
    }


//     __            _     _          _   _               _ _   __    __           _     _
//    /__\ __   __ _| |__ | | ___  __| | (_)_ __     __ _| | | / / /\ \ \___  _ __| | __| |___
//   /_\| '_ \ / _` | '_ \| |/ _ \/ _` | | | '_ \   / _` | | | \ \/  \/ / _ \| '__| |/ _` / __|
//  //__| | | | (_| | |_) | |  __/ (_| | | | | | | | (_| | | |  \  /\  / (_) | |  | | (_| \__ \
//  \__/|_| |_|\__,_|_.__/|_|\___|\__,_| |_|_| |_|  \__,_|_|_|   \/  \/ \___/|_|  |_|\__,_|___/
//


    /**
     * Return all world names were EHM is activated
     *
     * @return world names
     */
    public String[] getEnabledWorlds()
    {
        ArrayList<String> worlds = new ArrayList<String>();
        for (Map.Entry<String, Map<ConfigNode, Object>> entry : OPTIONS.rowMap().entrySet())
            worlds.add(entry.getKey());
        return worlds.toArray(new String[worlds.size()]);
    }


    public boolean isEnabledIn(String world)
    {
        return OPTIONS.containsRow(world);
    }


    /**
     * Does this config apply to all loaded worlds
     *
     * @return if applies to all worlds
     */
    public boolean isEnabledForAll()
    {
        return enabledForAll;
    }


    public String getAllWorldString()
    {
        return ALL_WORLDS;
    }


//     ___     _   _
//    / _ \___| |_| |_ ___ _ __ ___
//   / /_\/ _ \ __| __/ _ \ '__/ __|
//  / /_\\  __/ |_| ||  __/ |  \__ \
//  \____/\___|\__|\__\___|_|  |___/
//


    private static final BiMap<ConfigNode.VarType, Class> varTypeClassMap = HashBiMap.create();


    static
    {
        varTypeClassMap.put(ConfigNode.VarType.INTEGER, Integer.class);
        varTypeClassMap.put(ConfigNode.VarType.BOOLEAN, Boolean.class);
        varTypeClassMap.put(ConfigNode.VarType.MATERIAL, Material.class);
        varTypeClassMap.put(ConfigNode.VarType.BLOCK_RELATION_LIST, BlockRelationsList.class);
        varTypeClassMap.put(ConfigNode.VarType.DOUBLE, Double.class);
        varTypeClassMap.put(ConfigNode.VarType.LIST, List.class);
        varTypeClassMap.put(ConfigNode.VarType.POTION_EFFECT, PotionEffectHolder.class);
        varTypeClassMap.put(ConfigNode.VarType.STRING, String.class);
    }


    /**
     * Generic get() (untested)
     *
     * @param node  node to use
     * @param world world name
     * @param clazz type of node
     * @param <T>
     *
     * @return node value for the given world
     */
    public <T> T get(final ConfigNode node, final String world, Class<T> clazz)
    {
        if (!varTypeClassMap.containsKey(node.getVarType()))
            throw new IllegalArgumentException("Node " + node + " doesn't have a class set");
        Object val = OPTIONS.get(world, node);
        //VarType of node has to match VarType of the expected class
        if (varTypeClassMap.inverse().get(clazz) == node.getVarType())
        {
            //Check cast
            if (varTypeClassMap.get(node.getVarType()).isInstance(val))
                return (T) OPTIONS.get(world, node);
            else return (T) node.getValueToDisable();
        } else throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as " + varTypeClassMap.get(node.getVarType()));
    }


    /**
     * Get the integer value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns -1 if unknown.
     */
    public int getInt(final ConfigNode node, final String world)
    {
        int i = -1;
        switch (node.getVarType())
        {
            case INTEGER:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                i = obj instanceof Integer ? (Integer) obj : (Integer) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as an integer.");
            }
        }
        return i;
    }


    /**
     * Get the double value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns 0 if unknown.
     */
    public double getDouble(final ConfigNode node, final String world)
    {
        double d;
        switch (node.getVarType())
        {
            case DOUBLE:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                d = obj instanceof Number ? ((Number) obj).doubleValue() : (Double) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a double.");
            }
        }
        return d;
    }


    /**
     * Get the boolean value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns false if unknown.
     */
    public boolean getBoolean(final ConfigNode node, final String world)
    {
        boolean bool = false;
        switch (node.getVarType())
        {
            case BOOLEAN:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                bool = obj instanceof Boolean ? (Boolean) obj : (Boolean) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a boolean.");
            }
        }
        return bool;
    }


    /**
     * Get the string value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns and empty string if unknown.
     */
    public String getString(final ConfigNode node, final String world)
    {
        String out = "";
        switch (node.getVarType())
        {
            case STRING:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                out = obj instanceof String ? (String) obj : (String) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a string.");
            }
        }
        return out;
    }


    /**
     * Get the list value of the node.
     *
     * @param node - Node to use.
     *
     * @return Value of the node. Returns an empty list if unknown.
     */
    public List<String> getStringList(final ConfigNode node, final String world)
    {
        List<String> list;
        switch (node.getVarType())
        {
            case LIST:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                list = obj instanceof List ? (List<String>) obj : (List) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a List.");
            }
        }
        return list;
    }


    public PotionEffectHolder getPotionEffect(final ConfigNode node, final String world)
    {
        PotionEffectHolder effect;

        switch (node.getVarType())
        {
            case POTION_EFFECT:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                effect = obj instanceof PotionEffectHolder ? (PotionEffectHolder) obj : (PotionEffectHolder) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a PotionEffectHolder.");
            }
        }
        return effect;
    }


    @Deprecated //Should encourage use of getStringList, since this is performing an unchecked cast?
    public List<Material> getStringListAsMaterialList(final ConfigNode node, final String world)
    {
        //Parsing material names is expensive (see materialFromName) and this method is called on hot paths,
        //so hand out the cached result until the config is changed
        List<Material> cached = parsedMaterialLists.get(world, node);
        if (cached != null)
            return cached;

        List<Material> blockList = new ArrayList<>();

        switch (node.getVarType())
        {
            case LIST:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                if (!(obj instanceof List))
                {
                    parsedMaterialLists.put(world, node, blockList);
                    break;
                }
                for (String materialName : (List<String>) obj)
                {
                    Material material = materialFromName(materialName);
                    if (material == null)
                    {
                        plugin.getLogger().warning(materialName + " is not a valid material. Please fix or remove from config.yml " + node.getPath());
                        continue;
                    }
                    blockList.add(material);
                }

                parsedMaterialLists.put(world, node, blockList);
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " converted to a List<Material>.");
            }
        }
        return blockList;
    }


    /** Cache for {@link #materialFromName(String)}, allows caching of unknown names as well */
    private static final Map<String, Material> MATERIAL_NAMES = Collections.synchronizedMap(new HashMap<String, Material>());


    /**
     * Cached variant of {@link Material#matchMaterial(String)}.
     * <p/>
     * Material#matchMaterial is surprisingly expensive: it uppercases the given name and compiles + runs two regular
     * expressions (\s+ and \W) on every single call. Because EHM parses material names from the config inside event
     * handlers, the results - including unknown names - are cached here.
     * <p/>
     * This method doesn't log anything, warning about unknown names is up to the caller.
     *
     * @param name - material name as written in the config
     *
     * @return the Material or null if the name is unknown
     */
    public static Material materialFromName(String name)
    {
        if (name == null)
            return null;

        if (MATERIAL_NAMES.containsKey(name))
            return MATERIAL_NAMES.get(name);

        Material material = Material.matchMaterial(name);
        MATERIAL_NAMES.put(name, material);
        return material;
    }

    @Deprecated
    public BlockRelationsList getBlockRelationList(final ConfigNode node, final String world)
    {
        BlockRelationsList blockList;

        switch (node.getVarType())
        {
            case BLOCK_RELATION_LIST:
            {
                Object obj = null;
                if (OPTIONS.contains(world, node))
                    obj = OPTIONS.get(world, node);
                else if (enabledForAll)
                    obj = OPTIONS.get(ALL_WORLDS, node);
                blockList = obj instanceof BlockRelationsList ? (BlockRelationsList) obj : (BlockRelationsList) node.getValueToDisable();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Attempted to get " + node.toString() + " of type " + node.getVarType() + " as a BlockRelationsList.");
            }
        }
        return blockList;
    }


    public abstract void load();


    /**
     * Clear all the loaded config options. Primarily for unit testing purposes.
     */
    public void clearCache()
    {
        OPTIONS.clear();
    }
}
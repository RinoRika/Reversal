package net.optifine.shaders;

import net.minecraft.src.Config;
import net.optifine.config.ConnectedParser;
import net.optifine.shaders.config.MacroProcessor;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.StrUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EntityAliases
{
    private static int[] entityAliases = null;
    private static boolean updateOnResourcesReloaded;

    public static int getEntityAliasId(int entityId)
    {
        if (entityAliases == null)
        {
            return -1;
        }
        else if (entityId >= 0 && entityId < entityAliases.length)
        {
            int i = entityAliases[entityId];
            return i;
        }
        else
        {
            return -1;
        }
    }

    public static void resourcesReloaded()
    {
        if (updateOnResourcesReloaded)
        {
            updateOnResourcesReloaded = false;
            update(Shaders.getShaderPack());
        }
    }

    public static void update(IShaderPack shaderPack)
    {
        reset();

        if (shaderPack != null) {
            List<Integer> list = new ArrayList();
            String s = "/shaders/entity.properties";
            InputStream inputstream = shaderPack.getResourceAsStream(s);

            if (inputstream != null) {
                loadEntityAliases(inputstream, s, list);
            }


            if (!list.isEmpty()) {
                entityAliases = toArray(list);
            }
        }
    }

    private static void loadEntityAliases(InputStream in, String path, List<Integer> listEntityAliases)
    {
        if (in != null)
        {
            try
            {
                in = MacroProcessor.process(in, path);
                Properties properties = new PropertiesOrdered();
                properties.load(in);
                in.close();
                Config.dbg("[Shaders] Parsing entity mappings: " + path);
                ConnectedParser connectedparser = new ConnectedParser("Shaders");

                for (Object o : properties.keySet())
                {
                    String s = (String) o;
                    String s1 = properties.getProperty(s);
                    String s2 = "entity.";

                    if (!s.startsWith(s2))
                    {
                        Config.warn("[Shaders] Invalid entity ID: " + s);
                    }
                    else
                    {
                        String s3 = StrUtils.removePrefix(s, s2);
                        int i = Config.parseInt(s3, -1);

                        if (i < 0)
                        {
                            Config.warn("[Shaders] Invalid entity alias ID: " + i);
                        }
                        else
                        {
                            int[] aint = connectedparser.parseEntities(s1);

                            if (aint != null && aint.length >= 1)
                            {
                                for (int j = 0; j < aint.length; ++j)
                                {
                                    int k = aint[j];
                                    addToList(listEntityAliases, k, i);
                                }
                            }
                            else
                            {
                                Config.warn("[Shaders] Invalid entity ID mapping: " + s + "=" + s1);
                            }
                        }
                    }
                }
            }
            catch (IOException var15)
            {
                Config.warn("[Shaders] Error reading: " + path);
            }
        }
    }

    private static void addToList(List<Integer> list, int index, int val)
    {
        while (list.size() <= index)
        {
            list.add(Integer.valueOf(-1));
        }

        list.set(index, Integer.valueOf(val));
    }

    private static int[] toArray(List<Integer> list)
    {
        int[] aint = new int[list.size()];

        for (int i = 0; i < aint.length; ++i)
        {
            aint[i] = ((Integer)list.get(i)).intValue();
        }

        return aint;
    }

    public static void reset()
    {
        entityAliases = null;
    }
}

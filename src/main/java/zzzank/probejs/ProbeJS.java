package zzzank.probejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.forge.EventBuses;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import lombok.val;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zzzank.probejs.plugin.BuiltinProbeJSPlugin;
import zzzank.probejs.utils.JsonUtils;

import java.nio.file.Path;
import java.util.List;

@Mod(ProbeJS.MOD_ID)
public class ProbeJS {
    public static final String MOD_ID = "probejs";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder()
        .serializeSpecialFloatingPointValues()
        .setLenient()
        .disableHtmlEscaping()
        .registerTypeHierarchyAdapter(Path.class, new JsonUtils.PathConverter())
        .create();
    public static final Gson GSON_WRITER = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    public ProbeJS() {
        EventBuses.registerModEventBus(ProbeJS.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        insertPlugin();

        ProbeConfig.refresh();
    }

    /**
     * why not 'kubejs.plugins.txt' you might ask, well, ProbeJS Legacy declared two mods
     * so TWO plugins will be registered. Stupid KubeJS
     */
    private static void insertPlugin() {
        try {
            val field = KubeJSPlugins.class.getDeclaredField("LIST");
            field.setAccessible(true);
            val got = (List<KubeJSPlugin>) field.get(null);
            got.add(new BuiltinProbeJSPlugin());
        } catch (Throwable e) {
            ProbeJS.LOGGER.error("unable to insert KubeJS plugin", e);
        }
    }
}

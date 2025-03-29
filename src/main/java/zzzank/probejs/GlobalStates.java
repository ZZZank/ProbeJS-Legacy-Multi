package zzzank.probejs;

import lombok.val;
import me.shedaniel.architectury.platform.Mod;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import zzzank.probejs.features.bridge.ProbeServer;
import zzzank.probejs.mixins.AccessTextureAtlas;
import zzzank.probejs.mixins.AccessTextureManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GlobalStates {
    public static ProbeServer SERVER;

    public static final Set<String> MIXIN_LANG_KEYS = new HashSet<>();
    public static final Set<String> RECIPE_IDS = new HashSet<>();
    public static final Set<String> LOOT_TABLES = new HashSet<>();
    public static long ERROR_TIMESTAMP = 0;

    public static final Supplier<Set<String>> LANG_KEYS = () -> {
        Set<String> keys;
        synchronized (MIXIN_LANG_KEYS) {
            keys = new HashSet<>(MIXIN_LANG_KEYS);
        }
        val mc = Minecraft.getInstance();
        val manager = mc.getLanguageManager();
        val english = manager.getLanguage("en_us");
        if (english == null) {
            return keys;
        }

        val clientLanguage = ClientLanguage.loadFrom(
            mc.getResourceManager(),
            Collections.singletonList(english)
        );
        keys.addAll(clientLanguage.getLanguageData().keySet());
        return keys;
    };

    public static final Supplier<Set<String>> RAW_TEXTURES = () ->
        ((AccessTextureManager) Minecraft.getInstance().getTextureManager())
            .byPath()
            .keySet()
            .stream()
            .map(ResourceLocation::toString)
            .collect(Collectors.toSet());

    public static final Supplier<Set<String>> TEXTURES = () ->
        ((AccessTextureAtlas) Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS))
            .texturesByName()
            .keySet()
            .stream()
            .map(ResourceLocation::toString)
            .collect(Collectors.toSet());

    public static final Supplier<Set<String>> MODS = () ->
        Platform.getMods()
            .stream()
            .map(Mod::getModId)
            .collect(Collectors.toSet());

    // For probing stuffs
    public static BlockPos LAST_RIGHTCLICKED = null;
    public static Entity LAST_ENTITY = null;
}

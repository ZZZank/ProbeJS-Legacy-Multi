package zzzank.probejs.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import me.shedaniel.architectury.platform.Mod;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@UtilityClass
public class GameUtils {

    @Nullable
    public RegistryAccess registryAccess() {
        val server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.registryAccess();
    }

    public long modHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Mod mod : Platform.getMods()) {
                digest.update((mod.getModId() + mod.getVersion()).getBytes());
            }
            return ByteBuffer.wrap(digest.digest()).getLong();
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
    }

    public long registryHash() {
        try {
            val digest = MessageDigest.getInstance("SHA-256");
            val server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return -1;
            }

            RegistryInfos.values()
                .stream()
                .flatMap(registryInfo -> registryInfo.names.stream())
                .map(ResourceLocation::toString)
                .sorted()
                .forEach(s -> digest.update(s.getBytes()));

            ByteBuffer buffer = ByteBuffer.wrap(digest.digest());
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
    }

    public void logThrowable(Throwable t, int maxStackStraceCount) {
        val traces = t.getStackTrace();
        val limit = maxStackStraceCount < 0
            ? traces.length
            : Math.min(maxStackStraceCount, traces.length);
        val lines = new ArrayList<String>(1 + limit);
        lines.add(t.toString());
        for (int i = 0; i < limit; i++) {
            lines.add("    at " + traces[i].toString());
        }
        ProbeJS.LOGGER.error(String.join("\n", lines));
    }

    public void logThrowable(Throwable t) {
        logThrowable(t, -1);
    }
}

package zzzank.probejs.utils.registry;

import lombok.val;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class RegistryInfo implements Comparable<RegistryInfo> {

    public final Registry<?> raw;
    public final IForgeRegistry<?> forgeRaw;
    public final ResourceKey<? extends Registry<?>> resKey;
    public final Set<ResourceLocation> names;

    public RegistryInfo(Registry<?> registry) {
        this.raw = registry;
        this.forgeRaw = null;
        this.resKey = raw.key();
        this.names = raw.keySet();
    }

    @Override
    public int compareTo(@NotNull RegistryInfo o) {
        return resKey.compareTo(o.resKey);
    }

    public ResourceLocation id() {
        return resKey.location();
    }

    public Stream<? extends TagKey<?>> tagNames() {
        return raw.getTagNames();
    }

    public dev.latvian.mods.kubejs.registry.RegistryInfo<?> kjs() {
        return dev.latvian.mods.kubejs.registry.RegistryInfo.MAP.get(resKey);
    }

    public Class<?> assignmentType() {
        val kjs = kjs();
        if (kjs != null && kjs.autoWrap) {
            return kjs.objectBaseClass;
        }
        return null;
    }

    public Set<? extends Map.Entry<? extends ResourceKey<?>, ?>> entries() {
        return raw.entrySet();
    }
}
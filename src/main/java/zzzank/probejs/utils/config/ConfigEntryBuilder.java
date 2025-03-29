package zzzank.probejs.utils.config;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.NameUtils;
import zzzank.probejs.utils.config.binding.ConfigBinding;
import zzzank.probejs.utils.config.binding.DefaultBinding;
import zzzank.probejs.utils.config.binding.RangedBinding;
import zzzank.probejs.utils.config.binding.ReadOnlyBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.serde.ConfigSerdes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class ConfigEntryBuilder<T> {

    @NotNull
    public final ConfigImpl root;
    @NotNull
    public String name;
    @NotNull
    public String namespace;
    protected Class<T> expectedType;
    public List<String> comments;
    public ConfigBinding<T> binding;
    public ConfigSerde<T> serde;
    private final ConfigProperties properties = new ConfigProperties();

    protected ConfigEntryBuilder(@NotNull ConfigImpl config, @NotNull String namespace, @NotNull String name) {
        this.root = config;
        this.name = name;
        this.namespace = namespace;
    }

    public ConfigEntryBuilder<T> setName(@NotNull String name) {
        this.name = Asser.tNotNull(name, "config entry name");
        return this;
    }

    public ConfigEntryBuilder<T> setNamespace(@NotNull String namespace) {
        this.namespace = Asser.tNotNull(namespace, "config entry namespace");
        return this;
    }

    public <T_> ConfigEntryBuilder<T_> setDefault(Class<T_> type, ConfigBinding<T_> binding) {
        Asser.t(type.isInstance(binding.getDefault()), "config default value must match expected type");
        val casted = Cast.<ConfigEntryBuilder<T_>>to(this);
        casted.expectedType = Asser.tNotNull(type, "config expected type");
        casted.binding = Asser.tNotNull(binding, "config binding");
        return casted;
    }

    public <T_> ConfigEntryBuilder<T_> setDefault(ConfigBinding<T_> binding) {
        return setDefault(binding.clazzFromDefaultValue(), binding);
    }

    public <T_> ConfigEntryBuilder<T_> setDefault(@NotNull T_ defaultValue) {
        return setDefault(new DefaultBinding<>(defaultValue, name));
    }

    public <T_> ConfigEntryBuilder<T_> readOnly(@NotNull T_ defaultValue) {
        return setDefault(new ReadOnlyBinding<>(defaultValue, name));
    }

    public <T_ extends Comparable<T_>> ConfigEntryBuilder<T_> setDefault(
        @NotNull T_ defaultValue,
        @NotNull T_ min,
        @NotNull T_ max
    ) {
        return setDefault(new RangedBinding<>(defaultValue, name, min, max));
    }

    public ConfigEntryBuilder<T> setSerde(ConfigSerde<T> serde) {
        this.serde = serde;
        return this;
    }

    public <T_> ConfigEntryBuilder<T> setProperty(ConfigProperty<T_> property, @NotNull T_ value) {
        properties.put(property, value);
        return this;
    }

    public ConfigEntryBuilder<T> setComments(List<String> comments) {
        return setProperty(ConfigProperty.COMMENTS, comments);
    }

    public ConfigEntryBuilder<T> comment(String... comments) {
        this.properties.merge(
            ConfigProperty.COMMENTS,
            Arrays.stream(comments)
                .map(NameUtils.MATCH_LINE_BREAK::split)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList()),
            (a, b) -> {
                a.addAll(b);
                return a;
            }
        );
        return this;
    }

    public ConfigEntry<T> build() {
        if (comments == null) {
            comments = Collections.emptyList();
        }
        if (serde == null) {
            serde = ConfigSerdes.get(binding.getDefault());
        }
        return this.root.register(
            new ConfigEntry<>(this.root, namespace, name, serde, binding, properties)
        );
    }
}

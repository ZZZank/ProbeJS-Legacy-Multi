package zzzank.probejs.api;

import lombok.AllArgsConstructor;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClazzMemberCollector;
import zzzank.probejs.plugin.ProbeJSPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
@AllArgsConstructor
public class DumpContext {
    public final TSFileWriter writer;
    public final List<ProbeJSPlugin> plugins;
    public final ClassRegistry registry;

    public DumpContext(@Nonnull TSFileWriter writer) {
        this.writer = writer;
        this.registry = new ClassRegistry(new ClazzMemberCollector());
        plugins = new ArrayList<>();
    }
}

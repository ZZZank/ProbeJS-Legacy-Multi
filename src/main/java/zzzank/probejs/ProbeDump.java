package zzzank.probejs;

import com.google.gson.JsonObject;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.features.forge_scan.ClassScanner;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.schema.SchemaDump;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProbeDump {
    public static final Path SNIPPET_PATH = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.code-snippets");
    public static final Path CLASS_CACHE = ProbePaths.PROBE.resolve("classes.txt");
    public static final Path EVENT_CACHE = ProbePaths.PROBE.resolve("kube_event.json");

    final SchemaDump schemaDump = new SchemaDump();
    final SnippetDump snippetDump = new SnippetDump();
    final Collection<ScriptDump> scriptDumps = new ArrayList<>();
    public final Consumer<ProbeText> messageSender;

    public ProbeDump(Consumer<ProbeText> messageSender) {
        this.messageSender = messageSender;
    }

    public void addScript(@NotNull ScriptDump dump) {
        scriptDumps.add(Objects.requireNonNull(dump));
    }

    private void onModChange() throws IOException {
        // Decompile stuffs - here we scan mod classes even if we don't decompile
        // So we have all classes without needing to decompile
        ClassRegistry.REGISTRY.fromClasses(ClassScanner.scanForge());
        ClassRegistry.REGISTRY.fromClasses(ClassScanner.scanMods(ProbeConfig.fullScanMods.get()));

        report(ProbeText.pjs("dump.cleaning"));
        for (ScriptDump scriptDump : scriptDumps) {
            scriptDump.removeClasses();
            report(ProbeText.pjs("removed_script", scriptDump.manager.scriptType.toString()));
        }

//        val downloader = new SchemaDownloader();
//        try (var zipStream = downloader.openSchemaStream()) {
//            downloader.processFile(zipStream);
//        } catch (Throwable err) {
//            ProbeJS.LOGGER.error(err.getMessage());
//        }
    }

    private void onRegistryChange() throws IOException {

    }

    private void report(ProbeText text) {
        messageSender.accept(text);
    }

    public void trigger() throws IOException {
        report(ProbeText.pjs("dump.start").green());

        // Create the snippets
        snippetDump.fromDocs();
        snippetDump.writeTo(SNIPPET_PATH);

        // And schemas
        schemaDump.fromDocs();
        schemaDump.writeTo(ProbePaths.WORKSPACE_SETTINGS);
        writeVSCodeConfig();
        appendGitIgnore();

        report(ProbeText.pjs("dump.snippets_generated"));

//        EventJSInfos.loadFrom(EVENT_CACHE);
//        EventJSInfos.writeTo(EVENT_CACHE);

        if (GameUtils.modHash() != ProbeConfig.modHash.get()) {
            report(ProbeText.pjs("dump.mod_changed").aqua());
            onModChange();
            ProbeConfig.modHash.set(GameUtils.modHash());
        }

        if (GameUtils.registryHash() != ProbeConfig.registryHash.get()) {
            onRegistryChange();
            ProbeConfig.registryHash.set(GameUtils.registryHash());
        }

        // Fetch classes that will be used in the dump
        ClassRegistry.REGISTRY.loadFrom(CLASS_CACHE);
        for (ScriptDump scriptDump : scriptDumps) {
            ClassRegistry.REGISTRY.fromClasses(scriptDump.retrieveClasses());
        }

        ClassRegistry.REGISTRY.walkClass();
        ClassRegistry.REGISTRY.writeTo(CLASS_CACHE);
        report(ProbeText.pjs("dump.class_discovered", ClassRegistry.REGISTRY.foundClasses.size()));

        // Spawn a thread for each dump
        val threads = CollectUtils.mapToList(
            scriptDumps,
            (dump) -> new Thread(
                () -> {
                    dump.acceptClasses(ClassRegistry.REGISTRY.getFoundClasses());
                    try {
                        dump.dump();
                        report(ProbeText.pjs("dump.dump_finished", dump.manager.scriptType).green());
                    } catch (Throwable e) {
                        val text = ProbeText.pjs("dump.dump_error", dump.manager.scriptType).red();
                        report(text);
                        ProbeJS.LOGGER.error(text.unwrap().getString(), e);
                    }
                },
                String.format("ProbeDumpingThread-%s", dump.manager.scriptType.name)
            )
        );
        for (val thread : threads) {
            thread.start();
        }

        Thread reportingThread = new Thread(
            () -> {
                while (true) {
                    try {
                        Thread.sleep(3000);
                        if (threads.stream().noneMatch(Thread::isAlive)) {
                            return;
                        }
                        val dumpProgress = scriptDumps.stream()
                            .filter(sd -> sd.classesWriter.countAcceptedFiles() != 0)
                            .map(sd -> String.format(
                                "%s/%s",
                                sd.classesWriter.countWrittenFiles(),
                                sd.classesWriter.countAcceptedFiles()
                            ))
                            .collect(Collectors.joining(", "));
                        report(ProbeText.pjs("dump.report_progress").append(ProbeText.literal(dumpProgress).blue()));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            },
            "ProbeDumpingThread-report"
        );
        reportingThread.start();
    }

    private void writeVSCodeConfig() throws IOException {
        val config = (JsonObject) JsonUtils.parseObject(CollectUtils.ofMap(
            "json.schemas", CollectUtils.ofList(
                CollectUtils.ofMap(
                    "fileMatch", CollectUtils.ofList("/recipe_schemas/*.json"),
                    "url", "./.vscode/recipe.json"
                )
            )
        ));
        FileUtils.writeMergedConfig(ProbePaths.VSCODE_JSON, config);
    }

    private void appendGitIgnore() throws IOException {
        val toAppends = CollectUtils.ofList(".probe/*", "!.probe/probe-settings.json");
        val toRemoves = CollectUtils.ofList(".probe");

        ArrayList<String> lines;
        try (val reader = Files.newBufferedReader(ProbePaths.GIT_IGNORE)) {
            lines = reader.lines().collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ignored) {
            lines = new ArrayList<>();
        }

        lines.removeIf(toRemoves::contains);
        toAppends.removeIf(lines::contains);
        lines.addAll(toAppends);

        try (val writer = Files.newBufferedWriter(ProbePaths.GIT_IGNORE)) {
            for (val line : lines) {
                writer.write(line);
                writer.write('\n');
            }
        }
    }
}

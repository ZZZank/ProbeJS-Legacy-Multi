package zzzank.probejs.plugin;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.events.*;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformerRegistration;
import zzzank.probejs.lang.transpiler.transformation.impl.*;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.util.Map;

public class BuiltinProbeJSPlugin extends KubeJSPlugin implements ProbeJSPlugin {

    @Override
    public void registerEvents() {
        ProbeEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("require", new Require(event.manager));
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        // lol
        filter.deny("org.jetbrains.java.decompiler");
        filter.deny("com.github.javaparser");
        filter.deny("org.java_websocket");
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        ProbeEvents.ADD_GLOBAL.post(new AddGlobalEventJS(scriptDump));
    }

    @Override
    public void assignType(ScriptDump scriptDump) {
        ProbeEvents.ASSIGN_TYPE.post(new TypeAssignmentEventJS(scriptDump));
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        ProbeEvents.MODIFY_DOC.post(new TypingModificationEventJS(scriptDump, globalClasses));
    }

    @Override
    public void denyTypes(Transpiler transpiler) {
        transpiler.reject(Object.class);
        transpiler.reject(Void.class);

        transpiler.reject(Character.TYPE);
        transpiler.reject(Void.TYPE);
        transpiler.reject(Long.TYPE);
        transpiler.reject(Integer.TYPE);
        transpiler.reject(Short.TYPE);
        transpiler.reject(Byte.TYPE);
        transpiler.reject(Double.TYPE);
        transpiler.reject(Float.TYPE);
        transpiler.reject(Boolean.TYPE);
    }

    @Override
    public void registerClassTransformer(ClassTransformerRegistration registration) {
        if (ProbeConfig.autoParamRename.get()) {
            registration.register(new AutoParamRename());
        }
        if (RhizoState.RETURNS_SELF_ANNOTATION) {
            registration.register(new RhizoReturnsSelf());
        }
        registration.register(
            new KubeJSDenied(registration.scriptDump.manager),
            new InjectAnnotation(),
            new InjectArray(),
            new InjectBeans(ProbeConfig.fieldAsBeaning.get()),
            new InjectSpecialType()
        );
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        ProbeEvents.SNIPPETS.post(new SnippetGenerationEventJS(dump));
    }
}

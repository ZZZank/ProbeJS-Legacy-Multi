package zzzank.probejs.docs;

import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import lombok.val;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Map;

/**
 * @author ZZZank
 */
public class LoadClassFn implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val requireFn = Statements
            .func("require")
            .param("name", Types.STRING)
            .returnType(Types.ANY)
            .build();
        requireFn.addComment(
            "@deprecated please use `java()` directly, ProbeJS adds TS path support for it.",
            "@see java"
        );

        scriptDump.addGlobal("java", requireFn);
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        val file = globalClasses.get(ClassPath.fromJava(JavaWrapper.class));
        if (file == null) {
            return;
        }
        val decl = file.findCode(ClassDecl.class).orElse(null);
        if (decl == null) {
            return;
        }
        for (val method : decl.methods) {
            if ("loadClass".equals(method.name) || "tryLoadClass".equals(method.name)) {
                val t = Types.generic("T", GlobalClasses.CLASS_PATH);
                method.variableTypes.add(t);
                method.params.get(0).type = t;
                method.returnType = GlobalClasses.LOAD_CLASSES.withParams(t);
            }
        }
    }

    @Override
    public void denyBindings(BindingFilter filter) {
        filter.denyFunction("java");
        filter.denyFunction("require");
    }
}

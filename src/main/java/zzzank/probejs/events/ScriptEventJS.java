package zzzank.probejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassWrapper;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.Types;

public class ScriptEventJS extends EventJS {
    public final ScriptDump scriptDump;

    public ScriptEventJS(ScriptDump scriptDump) {
        this.scriptDump = scriptDump;
    }

    public ScriptType getScriptType() {
        return scriptDump.scriptType;
    }

    public ClassWrapper<Types> getTypes() {
        return new ClassWrapper<>(Types.class);
    }

    public ClassWrapper<Statements> getStatements() {
        return new ClassWrapper<>(Statements.class);
    }
}

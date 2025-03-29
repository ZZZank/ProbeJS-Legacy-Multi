package zzzank.probejs.docs.bindings;

import dev.latvian.kubejs.script.BindingsEvent;
import dev.latvian.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.*;

/**
 * @author ZZZank
 */
public class DummyBindingEvent extends BindingsEvent {
    private final BindingReader reader;

    public DummyBindingEvent(ScriptManager manager, Context cx, Scriptable scope, BindingReader reader) {
        super(manager, cx, scope);
        this.reader = reader;
    }

    @Override
    public void add(String name, Object value) {
        if (value instanceof Class<?> c) {
            reader.classes.put(name, c);
        } else if (value instanceof BaseFunction fn) {
            reader.functions.put(name, fn);
        } else {
            reader.constants.put(name, value);
        }
    }
}

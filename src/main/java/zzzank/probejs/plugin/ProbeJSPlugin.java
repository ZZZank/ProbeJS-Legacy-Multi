package zzzank.probejs.plugin;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.schema.SchemaDump;
import zzzank.probejs.lang.schema.SchemaElement;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.transpiler.redirect.TypeRedirect;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformerRegistration;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A plugin for ProbeJS that is able to alter how ProbeJS works.
 * <br>
 * Different method calls might have same parameter/controller,
 * but it is advised to call different methods and their own stage
 * in order to prevent unexpected behavior.
 */
public interface ProbeJSPlugin {
    /**
     * Used to add forcefully-converted types in order to prevent transient types
     * like boolean / string from showing up.
     * @see TypeConverter#addTypeRedirect(TypeRedirect)
     */
    default void addPredefinedTypes(TypeConverter converter) {
    }

    /**
     * Used to prevent some types from showing up in the dump, e.g. primitives.
     * @see Transpiler#reject(Class)
     */
    default void denyTypes(Transpiler transpiler) {
    }

    /**
     * Used to modify the classes that will be dumped to a certain script type.
     * <br>
     * Can add / remove dumps by mutating the globalClasses.
     */
    default void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
    }

    /**
     * Used to add code to global namespace.
     * <br>
     * Globals are available without any imports, so it must be ensured that the
     * added code is either:
     * 1. a type
     * 2. a binding (though it's not very needed for most people)
     * @see ScriptDump#addGlobal(String, Code...)
     * @see ScriptDump#addGlobal(String, Collection, Code...)
     */
    default void addGlobals(ScriptDump scriptDump) {
    }

    /**
     * Adds a convertible type to a classPath.
     * <br>
     * e.g. Item can be assigned with any item name string.
     * @see ScriptDump#assignType(Class, BaseType)
     * @see ScriptDump#assignType(ClassPath, BaseType)
     * @see ScriptDump#assignType(Class, String, BaseType)
     * @see ScriptDump#assignType(ClassPath, String, BaseType)
     */
    default void assignType(ScriptDump scriptDump) {
    }

    /**
     * Provides Java classes for the class registry to discover.
     */
    @HideFromJS
    default Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return Collections.emptySet();
    }

    /**
     * Provides event ids (without sub id) that should be skipped by auto-dump for custom support.
     */
    default Set<String> disableEventDumps(ScriptDump dump) {
        return Collections.emptySet();
    }

    /**
     * @see SnippetDump#snippet(String)
     */
    default void addVSCodeSnippets(SnippetDump dump) {
    }

    /**
     * @see SchemaDump#newSchema(String, SchemaElement)
     */
    default void addJsonSchema(SchemaDump dump) {
    }

    /**
     * @param predefined Note that the value of such map (in {@code JSObjectType}) is recommended to be in the format
     *                   of {@code {"some_id": ((someArg1: SomeType1, someArg2: SomeType2, someArg3: SomeType3)=>SomeRecipeTypeJS)}},
     *                   aka {@code {string: lambda}}
     */
    default void addPredefinedRecipeDoc(ScriptDump scriptDump, Map<ResourceLocation, JSLambdaType> predefined) {
    }

    default void denyBindings(BindingFilter filter) {
    }

    default void registerClassTransformer(ClassTransformerRegistration registration) {
    }
}

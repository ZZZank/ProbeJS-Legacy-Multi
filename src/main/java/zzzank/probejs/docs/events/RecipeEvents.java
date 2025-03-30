package zzzank.probejs.docs.events;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.SpecialRecipeSchema;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;
import zzzank.probejs.features.kesseractjs.TypeDescAdapter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.utils.NameUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecipeEvents implements ProbeJSPlugin {
    public static final Map<String, String> SHORTCUTS = new HashMap<>();
    public static final ClassPath DOCUMENTED_RECIPES =
        ClassPath.fromRaw("moe.wolfgirl.probejs.generated.DocumentedRecipes");

    static {
        SHORTCUTS.put("shaped", "kubejs:shaped");
        SHORTCUTS.put("shapeless", "kubejs:shapeless");
        SHORTCUTS.put("smelting", "minecraft:smelting");
        SHORTCUTS.put("blasting", "minecraft:blasting");
        SHORTCUTS.put("smoking", "minecraft:smoking");
        SHORTCUTS.put("campfireCooking", "minecraft:campfire_cooking");
        SHORTCUTS.put("stonecutting", "minecraft:stonecutting");
        SHORTCUTS.put("smithing", "minecraft:smithing_transform");
        SHORTCUTS.put("smithingTrim", "minecraft:smithing_trim");
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return;
        }
        val converter = scriptDump.transpiler.typeConverter;

        // Generate recipe schema classes
        // Also generate the documented recipe class containing all stuffs from everywhere
        val documentedRecipes = Statements.clazz(DOCUMENTED_RECIPES.getName());

        for (val entry : RecipeNamespace.getAll().entrySet()) {
            val namespaceId = entry.getKey();
            val namespace = entry.getValue();

            val builder = Types.object();

            for (val e : namespace.entrySet()) {
                val schemaId = e.getKey();
                val schemaType = e.getValue();
                if (schemaType instanceof JsonRecipeSchemaType) {
                    continue;
                }
                val schema = schemaType.schema;
                if (schema == SpecialRecipeSchema.SCHEMA) {
                    continue;
                }

                val schemaPath = getSchemaClassPath(namespaceId, schemaId);
                val schemaDecl = generateSchemaClass(schemaId, schema, converter);
                val schemaFile = new TypeScriptFile(schemaPath);
                schemaFile.addCode(schemaDecl);
                globalClasses.put(schemaPath, schemaFile);

                val recipeFunction = generateSchemaFunction(schemaPath, schema, converter);
                builder.member(schemaId, recipeFunction);
            }

            documentedRecipes.field(namespaceId, builder.build());
        }
        val documentFile = new TypeScriptFile(DOCUMENTED_RECIPES);
        documentFile.addCode(documentedRecipes.build());
        globalClasses.put(DOCUMENTED_RECIPES, documentFile);

        // Inject types into the RecipeEventJS
        val recipeEventFile = globalClasses.get(ClassPath.fromJava(RecipesEventJS.class));
        val recipeEvent = recipeEventFile.findCode(ClassDecl.class).orElse(null);
        if (recipeEvent == null) {
            return; // What???
        }
        recipeEvent.methods.stream()
            .filter(m -> m.params.isEmpty() && m.name.equals("getRecipes"))
            .findFirst()
            .ifPresent(methodDecl -> methodDecl.returnType = Types.type(DOCUMENTED_RECIPES));
        for (Code code : recipeEvent.bodyCode) {
            if (code instanceof BeanDecl beanDecl && beanDecl.name.equals("recipes")) {
                beanDecl.type = Types.type(DOCUMENTED_RECIPES);
            }
        }
        recipeEventFile.declaration.addImport(ImportInfo.ofDefault(DOCUMENTED_RECIPES));

        // Make shortcuts valid recipe functions
        for (val field : recipeEvent.fields) {
            if (!SHORTCUTS.containsKey(field.name)) {
                continue;
            }
            val parts = SHORTCUTS.get(field.name).split(":", 2);
            val shortcutSchema = RecipeNamespace.getAll().get(parts[0]).get(parts[1]).schema;
            val returnType = getSchemaClassPath(parts[0], parts[1]);
            field.type = generateSchemaFunction(returnType, shortcutSchema, converter);

            for (val usedClassPath : field.type.getImportInfos(BaseType.FormatType.RETURN)) {
                recipeEventFile.declaration.addImport(usedClassPath);
            }
        }

    }

    private static ClassPath getSchemaClassPath(String namespace, String id) {
        return ClassPath.fromRaw("moe.wolfgirl.probejs.generated.schema.%s.%s".formatted(
            namespace, NameUtils.rlToTitle(id)
        ));
    }

    /**
     * export class RecipeId {
     * foo(foo: FooType): this
     * bar(bar: BarType): this
     * }
     */
    private static ClassDecl generateSchemaClass(String id, RecipeSchema schema, TypeConverter converter) {
        ClassDecl.Builder builder = Statements.clazz(NameUtils.rlToTitle(id))
            .superClass(Types.type(schema.recipeType));
        for (RecipeKey<?> key : schema.keys) {
            if (key.noBuilders) {
                continue;
            }
            builder.method(
                key.preferred, method -> {
                    method.returnType(Types.THIS);
                    val baseType =
                        TypeDescAdapter.convertType(key.component.constructorDescription(TypeDescAdapter.PROBEJS));
                    if (!baseType.equals(Types.BOOLEAN)) {
                        method.param(key.preferred, baseType);
                    }
                }
            );
        }
        return builder.build();
    }

    private static JSLambdaType generateSchemaFunction(
        ClassPath returnType,
        RecipeSchema schema,
        TypeConverter converter
    ) {
        val builder = Types.lambda()
            .returnType(Types.type(returnType));

        for (RecipeKey<?> key : schema.keys) {
            if (key.excluded) {
                continue;
            }
            builder.param(
                key.preferred,
                TypeDescAdapter.convertType(key.component.constructorDescription(TypeDescAdapter.PROBEJS)),
                key.optional(),
                false
            );
        }

        return builder.build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        val classes = new HashSet<Class<?>>();
        for (val namespace : RecipeNamespace.getAll().values()) {
            for (val schemaType : namespace.values()) {
                classes.add(schemaType.schema.recipeType);
            }
        }
        return classes;
    }
}
package zzzank.probejs.mixins;

import com.google.gson.JsonNull;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaClass;
import dev.latvian.mods.rhino.Scriptable;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zzzank.probejs.GlobalStates;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;

@Mixin(value = {ScriptManager.class}, remap = false)
public abstract class MixinScriptManager {

    @Inject(method = "load", at = @At("HEAD"))
    public void pjs$reloadStart(CallbackInfo ci) {
        if (GlobalStates.SERVER != null) {
            GlobalStates.SERVER.broadcast("clear_error", JsonNull.INSTANCE);
        }
    }

    @Inject(method = "loadJavaClass", at = @At("RETURN"))
    public void pjs$captureClass(String name, boolean error, CallbackInfoReturnable<NativeJavaClass> cir) {
        val result = cir.getReturnValue();
        if (result == null) {
            return;
        }
        ClassRegistry.REGISTRY.fromClass(result.getClassObject());
    }
}

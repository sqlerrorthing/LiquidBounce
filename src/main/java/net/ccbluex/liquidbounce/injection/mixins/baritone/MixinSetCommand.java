package net.ccbluex.liquidbounce.injection.mixins.baritone;

import baritone.api.Settings;
import baritone.api.command.argument.IArgConsumer;
import baritone.command.defaults.SetCommand;
import com.llamalad7.mixinextras.sugar.Local;
import net.ccbluex.liquidbounce.features.module.modules.client.ModuleBaritoneKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SetCommand.class)
public class MixinSetCommand {

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lbaritone/command/defaults/SetCommand;logDirect(Ljava/lang/String;)V", ordinal = 9), remap = false)
    private void hookSetSetting(String par1, IArgConsumer par2, CallbackInfo ci, @Local(ordinal = 0) Settings.Setting<?> setting) {
        var onChangeListener = ModuleBaritoneKt.getControlledBaritoneSettings()
                .getOrDefault(setting, null);

        if (onChangeListener != null) {
            onChangeListener.invoke(setting.value);
        }
    }

}

package com.matyrobbrt.cursedkeyboard.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.matyrobbrt.cursedkeyboard.CursedKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyBindsScreen.class)
public class KeyBindsScreenMixin {
    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setToDefault()V"), method = "lambda$addFooter$0")
    private boolean cursedkeyboard$allowResetOnlyIfNotCursed(KeyMapping mapping) {
        return !CursedKeybinds.isCursed(mapping);
    }
}

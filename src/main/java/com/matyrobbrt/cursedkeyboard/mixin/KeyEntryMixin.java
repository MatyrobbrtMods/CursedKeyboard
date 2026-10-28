package com.matyrobbrt.cursedkeyboard.mixin;

import com.matyrobbrt.cursedkeyboard.CursedKeybinds;
import com.matyrobbrt.cursedkeyboard.CursedKeyboard;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.KeyEntry.class)
public class KeyEntryMixin {
    @Final
    @Shadow
    private Button changeButton, resetButton;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void cursedkeyboard$disableIfNecessary(KeyBindsList this$0, KeyMapping key, Component name, CallbackInfo ci) {
        if (CursedKeybinds.isAffected(key)) {
            changeButton.active = false;
            changeButton.setTooltip(CursedKeyboard.CANNOT_CHANGE);
            resetButton.active = false;
            resetButton.setTooltip(CursedKeyboard.CANNOT_CHANGE);
        }
    }
}

package com.matyrobbrt.cursedkeyboard.mixin;

import com.matyrobbrt.cursedkeyboard.CursedKeybinds;
import com.matyrobbrt.cursedkeyboard.CursedKeyboard;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
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
    private KeyMapping key;

    @Final
    @Shadow
    private Button changeButton, resetButton;

    @Inject(at = @At("TAIL"), method = "refreshEntry()V")
    private void cursedkeyboard$disableIfNecessary(CallbackInfo ci) {
        if (CursedKeybinds.isCursed(key)) {
            changeButton.active = false;
            changeButton.setTooltip(CursedKeyboard.CANNOT_CHANGE);
            resetButton.active = false;
            resetButton.setTooltip(CursedKeyboard.CANNOT_CHANGE);
        }
    }
}

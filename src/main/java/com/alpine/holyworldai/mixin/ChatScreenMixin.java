package com.alpine.holyworldai.mixin;

import com.alpine.holyworldai.HolyWorldAIClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void onSendMessage(String message, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (HolyWorldAIClient.chatMonitor != null) {
                HolyWorldAIClient.chatMonitor.onSendMessage(message);
            }
        } catch (Exception e) {
            HolyWorldAIClient.LOGGER.error("Send mixin error", e);
        }
    }
}

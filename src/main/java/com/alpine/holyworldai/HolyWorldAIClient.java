package com.alpine.holyworldai;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HolyWorldAIClient implements ClientModInitializer {

    public static final String MOD_ID = "holyworldai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String MODERATOR_NICK = "Alpine1428";

    public static boolean isLearning = false;
    public static boolean isAutoResponding = false;
    public static boolean isSpying = false;
    public static String checkedPlayerNick = null;

    public static AIEngine aiEngine;
    public static ChatMonitor chatMonitor;

    @Override
    public void onInitializeClient() {
        LOGGER.info("HolyWorld AI Assistant loaded!");
        aiEngine = new AIEngine();
        aiEngine.loadModel();
        chatMonitor = new ChatMonitor();
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("ai")
                .then(ClientCommandManager.literal("startlesson")
                    .executes(context -> {
                        isLearning = true;
                        context.getSource().sendFeedback(
                            Text.literal("\u00a7a[AI] \u00a7fLearning mode ON"));
                        LOGGER.info("Learning mode ENABLED");
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("stoplesson")
                    .executes(context -> {
                        isLearning = false;
                        aiEngine.saveModel();
                        context.getSource().sendFeedback(
                            Text.literal("\u00a7a[AI] \u00a7fLearning OFF. Entries: "
                                + aiEngine.getTrainingDataSize()));
                        LOGGER.info("Learning mode DISABLED");
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("start")
                    .executes(context -> {
                        isAutoResponding = true;
                        context.getSource().sendFeedback(
                            Text.literal("\u00a7a[AI] \u00a7fAuto-reply ON"));
                        LOGGER.info("Auto-respond ENABLED");
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("stop")
                    .executes(context -> {
                        isAutoResponding = false;
                        context.getSource().sendFeedback(
                            Text.literal("\u00a7a[AI] \u00a7fAuto-reply OFF"));
                        LOGGER.info("Auto-respond DISABLED");
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("status")
                    .executes(context -> {
                        String s = "\u00a7a[AI] \u00a7f"
                            + "Learn=" + (isLearning ? "ON" : "OFF")
                            + " Reply=" + (isAutoResponding ? "ON" : "OFF")
                            + " Spy=" + (isSpying ? "ON" : "OFF")
                            + " Target=" + (checkedPlayerNick != null ? checkedPlayerNick : "none")
                            + " Data=" + aiEngine.getTrainingDataSize();
                        context.getSource().sendFeedback(Text.literal(s));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("reset")
                    .executes(context -> {
                        aiEngine.resetModel();
                        context.getSource().sendFeedback(
                            Text.literal("\u00a7a[AI] \u00a7cModel reset!"));
                        return 1;
                    })
                )
            );
        });
    }
}

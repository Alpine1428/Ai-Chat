package com.alpine.holyworldai;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMonitor {

    private final List<String> playerMessages = new ArrayList<>();
    private final List<String> moderatorResponses = new ArrayList<>();

    // Удаление цвет-кодов Minecraft (§a, §7 и т.д.)
    private static final Pattern COLOR_PATTERN = Pattern.compile("§.");

    // Универсальный паттерн:
    // [CHECK] Nick -> message
    private static final Pattern CHECK_PATTERN =
            Pattern.compile("\\[CHECK\\]\\s*(\\S+)\\s*->\\s*(.+)");

    private static final Pattern SPY_START =
            Pattern.compile("/hm\\s+spyfrz\\s+(\\S+).*");

    private static final Pattern SPY_END =
            Pattern.compile("/hm\\s+sban.*");

    // ================= SEND MESSAGE =================

    public void onSendMessage(String message) {

        Matcher startMatcher = SPY_START.matcher(message);
        if (startMatcher.matches()) {
            HolyWorldAIClient.isSpying = true;
            HolyWorldAIClient.checkedPlayerNick = startMatcher.group(1);

            playerMessages.clear();
            moderatorResponses.clear();

            HolyWorldAIClient.LOGGER.info("Spy started for: " + HolyWorldAIClient.checkedPlayerNick);
            return;
        }

        if (SPY_END.matcher(message).matches()) {
            HolyWorldAIClient.isSpying = false;

            if (HolyWorldAIClient.isLearning && !playerMessages.isEmpty()) {
                saveSession();
            }

            HolyWorldAIClient.checkedPlayerNick = null;
            playerMessages.clear();
            moderatorResponses.clear();

            HolyWorldAIClient.LOGGER.info("Spy ended");
            return;
        }

        if (HolyWorldAIClient.isSpying && HolyWorldAIClient.isLearning) {
            if (!message.startsWith("/")) {
                moderatorResponses.add(message);
                HolyWorldAIClient.LOGGER.info("Recorded mod response: " + message);
            }
        }
    }

    // ================= RECEIVE CHAT =================

    public void onChatMessage(String fullMessage) {

        if (!HolyWorldAIClient.isSpying) return;

        // Убираем цвет-коды
        String clean = COLOR_PATTERN.matcher(fullMessage).replaceAll("");

        Matcher matcher = CHECK_PATTERN.matcher(clean);

        if (matcher.find()) {

            String nick = matcher.group(1);
            String msg = matcher.group(2).trim();

            HolyWorldAIClient.LOGGER.info("[CHECK] " + nick + ": " + msg);

            if (HolyWorldAIClient.isLearning) {
                playerMessages.add(msg);
            }

            if (HolyWorldAIClient.isAutoResponding) {
                autoRespond(msg);
            }
        }
    }

    // ================= AUTO RESPONSE =================

    private void autoRespond(String playerMessage) {
        new Thread(() -> {
            try {
                String response = HolyWorldAIClient.aiEngine.generateResponse(playerMessage);

                if (response != null && !response.isEmpty()) {

                    Thread.sleep(1500 + (long) (Math.random() * 2000));

                    MinecraftClient client = MinecraftClient.getInstance();
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.networkHandler.sendChatMessage(response);
                        }
                    });

                    HolyWorldAIClient.LOGGER.info("AI replied: " + response);
                }

            } catch (Exception e) {
                HolyWorldAIClient.LOGGER.error("Auto respond error", e);
            }
        }, "AI-Reply").start();
    }

    // ================= SAVE SESSION =================

    private void saveSession() {

        for (int i = 0; i < playerMessages.size(); i++) {

            String pMsg = playerMessages.get(i);
            String mResp = i < moderatorResponses.size()
                    ? moderatorResponses.get(i)
                    : null;

            if (mResp != null) {
                HolyWorldAIClient.aiEngine.addTrainingPair(pMsg, mResp);
                HolyWorldAIClient.LOGGER.info("Trained: [" + pMsg + "] -> [" + mResp + "]");
            }
        }

        HolyWorldAIClient.aiEngine.saveModel();
    }
}

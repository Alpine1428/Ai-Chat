package com.alpine.holyworldai;

import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMonitor {

    private final List<String> playerMessages = new ArrayList<>();
    private final List<String> moderatorResponses = new ArrayList<>();

    private static final Pattern CHECK_PATTERN =
        Pattern.compile("\\[CHECK\\]\\s*(\\S+)\\s*[:\\u00bb>]\\s*(.+)");

    private static final Pattern SPY_START =
        Pattern.compile("/hm\\s+spyfrz.*");

    private static final Pattern SPY_END =
        Pattern.compile("/hm\\s+sban.*");

    public void onSendMessage(String message) {
        if (SPY_START.matcher(message).matches()) {
            HolyWorldAIClient.isSpying = true;
            String[] parts = message.split("\\s+");
            if (parts.length >= 3) {
                HolyWorldAIClient.checkedPlayerNick = parts[2];
            }
            playerMessages.clear();
            moderatorResponses.clear();
            HolyWorldAIClient.LOGGER.info("Spy started: " + HolyWorldAIClient.checkedPlayerNick);
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
            }
        }
    }

    public void onChatMessage(String fullMessage) {
        if (!HolyWorldAIClient.isSpying) return;

        Matcher m = CHECK_PATTERN.matcher(fullMessage);
        if (m.find()) {
            String nick = m.group(1);
            String msg = m.group(2).trim();
            if (HolyWorldAIClient.isLearning) {
                playerMessages.add(msg);
            }
            if (HolyWorldAIClient.isAutoResponding) {
                autoRespond(msg);
            }
            return;
        }

        if (HolyWorldAIClient.checkedPlayerNick != null) {
            String nick = HolyWorldAIClient.checkedPlayerNick;
            Pattern p = Pattern.compile(
                "(?:\\[CHECK\\]\\s*)?" + Pattern.quote(nick) + "\\s*[:\\u00bb>]\\s*(.+)"
            );
            Matcher dm = p.matcher(fullMessage);
            if (dm.find()) {
                String msg = dm.group(1).trim();
                if (HolyWorldAIClient.isLearning) {
                    playerMessages.add(msg);
                }
                if (HolyWorldAIClient.isAutoResponding) {
                    autoRespond(msg);
                }
            }
        }
    }

    private void autoRespond(String playerMessage) {
        new Thread(() -> {
            try {
                String response = HolyWorldAIClient.aiEngine.generateResponse(playerMessage);
                if (response != null && !response.isEmpty()) {
                    Thread.sleep(1500 + (long)(Math.random() * 2000));
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.networkHandler.sendChatMessage(response);
                        }
                    });
                }
            } catch (Exception e) {
                HolyWorldAIClient.LOGGER.error("Auto respond error", e);
            }
        }, "AI-Reply").start();
    }

    private void saveSession() {
        for (int i = 0; i < playerMessages.size(); i++) {
            String pMsg = playerMessages.get(i);
            String mResp = i < moderatorResponses.size() ? moderatorResponses.get(i) : null;
            if (mResp != null) {
                HolyWorldAIClient.aiEngine.addTrainingPair(pMsg, mResp);
            }
        }
        HolyWorldAIClient.aiEngine.saveModel();
    }
}

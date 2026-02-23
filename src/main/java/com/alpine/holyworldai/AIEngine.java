package com.alpine.holyworldai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AIEngine {

    private static final String DATA_DIR = "config/holyworldai/";
    private static final String MODEL_FILE = DATA_DIR + "training_data.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Random random = new Random();

    private Map<String, List<String>> trainingData = new HashMap<>();
    private final Set<String> vocabulary = new HashSet<>();

    private static final Map<String, List<String>> DEFAULTS = new HashMap<>();
    private static final Map<String, String> CATEGORIES = new HashMap<>();

    static {
        DEFAULTS.put("\u043f\u0440\u0438\u0432\u0435\u0442", Arrays.asList(
            "\u0417\u0434\u0440\u0430\u0432\u0441\u0442\u0432\u0443\u0439\u0442\u0435, \u0432\u044b \u0437\u0430\u043c\u043e\u0440\u043e\u0436\u0435\u043d\u044b \u0434\u043b\u044f \u043f\u0440\u043e\u0432\u0435\u0440\u043a\u0438.",
            "\u041f\u0440\u0438\u0432\u0435\u0442\u0441\u0442\u0432\u0443\u044e. \u0421\u0442\u043e\u0439\u0442\u0435 \u043d\u0430 \u043c\u0435\u0441\u0442\u0435."
        ));
        DEFAULTS.put("\u0437\u0430 \u0447\u0442\u043e", Arrays.asList(
            "\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u043d\u0438\u0435 \u043d\u0430 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435 \u0441\u0442\u043e\u0440\u043e\u043d\u043d\u0435\u0433\u043e \u041f\u041e.",
            "\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u043d\u0438\u0435 \u043d\u0430 \u0447\u0438\u0442\u044b."
        ));
        DEFAULTS.put("\u043d\u0435 \u0447\u0438\u0442\u0435\u0440", Arrays.asList(
            "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u043f\u043e\u043a\u0430\u0436\u0435\u0442.",
            "\u0421\u0442\u0430\u043d\u0434\u0430\u0440\u0442\u043d\u0430\u044f \u043f\u0440\u043e\u0446\u0435\u0434\u0443\u0440\u0430."
        ));
        DEFAULTS.put("\u0447\u0442\u043e \u0434\u0435\u043b\u0430\u0442\u044c", Arrays.asList(
            "\u0421\u043a\u0430\u0447\u0430\u0439\u0442\u0435 AnyDesk \u0438 \u0441\u043a\u0438\u043d\u044c\u0442\u0435 \u043a\u043e\u0434.",
            "\u041e\u0436\u0438\u0434\u0430\u0439\u0442\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0438\u0439."
        ));
        DEFAULTS.put("anydesk", Arrays.asList(
            "\u0421\u043a\u0430\u0447\u0430\u0439\u0442\u0435 AnyDesk \u0441 anydesk.com \u0438 \u043f\u0440\u0438\u0448\u043b\u0438\u0442\u0435 ID.",
            "\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u0435 AnyDesk."
        ));
        DEFAULTS.put("\u043d\u0435 \u0431\u0443\u0434\u0443", Arrays.asList(
            "\u041e\u0442\u043a\u0430\u0437 = \u0431\u0430\u043d.",
            "\u041f\u0440\u0438 \u043e\u0442\u043a\u0430\u0437\u0435 \u0431\u0443\u0434\u0435\u0442 \u0431\u0430\u043d."
        ));
        DEFAULTS.put("\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u0436\u0434\u0430\u0442\u044c", Arrays.asList(
            "\u041d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u043c\u0438\u043d\u0443\u0442.",
            "\u041d\u0435 \u0434\u043e\u043b\u0433\u043e."
        ));
        DEFAULTS.put("\u043f\u043e\u043a\u0430", Arrays.asList(
            "\u041d\u0435 \u043f\u043e\u043a\u0438\u0434\u0430\u0439\u0442\u0435 \u0441\u0435\u0440\u0432\u0435\u0440!",
            "\u0412\u044b\u0445\u043e\u0434 = \u0431\u0430\u043d."
        ));

        CATEGORIES.put("\u043f\u0440\u0438\u0432\u0435\u0442", "greeting");
        CATEGORIES.put("\u0437\u0434\u0440\u0430\u0432\u0441\u0442\u0432\u0443\u0439", "greeting");
        CATEGORIES.put("\u0445\u0430\u0439", "greeting");
        CATEGORIES.put("\u0437\u0434\u0430\u0440\u043e\u0432\u0430", "greeting");
        CATEGORIES.put("\u0437\u0430 \u0447\u0442\u043e", "reason");
        CATEGORIES.put("\u043f\u043e\u0447\u0435\u043c\u0443", "reason");
        CATEGORIES.put("\u0437\u0430\u0447\u0435\u043c", "reason");
        CATEGORIES.put("\u043d\u0435 \u0447\u0438\u0442\u0435\u0440", "denial");
        CATEGORIES.put("\u0447\u0438\u0441\u0442", "denial");
        CATEGORIES.put("\u0447\u0442\u043e \u0434\u0435\u043b\u0430\u0442\u044c", "instructions");
        CATEGORIES.put("anydesk", "anydesk");
        CATEGORIES.put("\u043d\u0435 \u0431\u0443\u0434\u0443", "refusal");
        CATEGORIES.put("\u043e\u0442\u043a\u0430\u0437\u044b\u0432\u0430\u044e\u0441\u044c", "refusal");
        CATEGORIES.put("\u0441\u043a\u043e\u043b\u044c\u043a\u043e", "waiting");
        CATEGORIES.put("\u0434\u043e\u043b\u0433\u043e", "waiting");
        CATEGORIES.put("\u0436\u0434\u0430\u0442\u044c", "waiting");
        CATEGORIES.put("\u043f\u043e\u043a\u0430", "leaving");
        CATEGORIES.put("\u0443\u0445\u043e\u0436\u0443", "leaving");
        CATEGORIES.put("\u0432\u044b\u0445\u043e\u0436\u0443", "leaving");
    }

    public void addTrainingPair(String playerMsg, String modResp) {
        String key = normalize(playerMsg);
        trainingData.computeIfAbsent(key, k -> new ArrayList<>()).add(modResp);
        Collections.addAll(vocabulary, key.split("\\s+"));
    }

    public String generateResponse(String playerMsg) {
        String norm = normalize(playerMsg);
        if (trainingData.containsKey(norm)) {
            return pick(trainingData.get(norm));
        }
        String best = findBest(norm);
        if (best != null) {
            return pick(trainingData.get(best));
        }
        String cat = findCategoryResponse(norm);
        if (cat != null) return cat;
        String def = findDefault(norm);
        if (def != null) return def;
        return pick(Arrays.asList(
            "\u041e\u0436\u0438\u0434\u0430\u0439\u0442\u0435.",
            "\u0421\u043b\u0435\u0434\u0443\u0439\u0442\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0438\u044f\u043c.",
            "\u0421\u0442\u043e\u0439\u0442\u0435 \u043d\u0430 \u043c\u0435\u0441\u0442\u0435.",
            "\u041d\u0435 \u043f\u043e\u043a\u0438\u0434\u0430\u0439\u0442\u0435 \u0441\u0435\u0440\u0432\u0435\u0440."
        ));
    }

    private String findBest(String input) {
        if (trainingData.isEmpty()) return null;
        String[] iw = input.split("\\s+");
        Map<String, Double> iTf = tfidf(iw);
        double bestScore = 0;
        String bestKey = null;
        for (String key : trainingData.keySet()) {
            String[] kw = key.split("\\s+");
            Map<String, Double> kTf = tfidf(kw);
            double sim = cosine(iTf, kTf);
            if (sim > bestScore) {
                bestScore = sim;
                bestKey = key;
            }
        }
        if (bestScore >= 0.3) return bestKey;
        bestScore = 0;
        bestKey = null;
        for (String key : trainingData.keySet()) {
            String[] kw = key.split("\\s+");
            double common = commonWords(iw, kw);
            double score = common / Math.max(iw.length, kw.length);
            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }
        if (bestScore >= 0.4) return bestKey;
        return null;
    }

    private String findCategoryResponse(String input) {
        for (Map.Entry<String, String> ce : CATEGORIES.entrySet()) {
            if (input.contains(ce.getKey())) {
                String cat = ce.getValue();
                for (Map.Entry<String, List<String>> de : trainingData.entrySet()) {
                    for (Map.Entry<String, String> ce2 : CATEGORIES.entrySet()) {
                        if (de.getKey().contains(ce2.getKey()) && ce2.getValue().equals(cat)) {
                            return pick(de.getValue());
                        }
                    }
                }
            }
        }
        return null;
    }

    private String findDefault(String input) {
        for (Map.Entry<String, List<String>> e : DEFAULTS.entrySet()) {
            if (input.contains(e.getKey())) {
                return pick(e.getValue());
            }
        }
        return null;
    }

    private Map<String, Double> tfidf(String[] words) {
        Map<String, Double> result = new HashMap<>();
        Map<String, Integer> cnt = new HashMap<>();
        for (String w : words) cnt.merge(w, 1, Integer::sum);
        for (Map.Entry<String, Integer> e : cnt.entrySet()) {
            double tf = (double) e.getValue() / words.length;
            double idf = Math.log((double)(trainingData.size() + 1) / (1 + docsWith(e.getKey())));
            result.put(e.getKey(), tf * idf);
        }
        return result;
    }

    private int docsWith(String word) {
        int c = 0;
        for (String key : trainingData.keySet()) {
            if (key.contains(word)) c++;
        }
        return c;
    }

    private double cosine(Map<String, Double> a, Map<String, Double> b) {
        Set<String> all = new HashSet<>();
        all.addAll(a.keySet());
        all.addAll(b.keySet());
        double dot = 0, nA = 0, nB = 0;
        for (String k : all) {
            double vA = a.getOrDefault(k, 0.0);
            double vB = b.getOrDefault(k, 0.0);
            dot += vA * vB;
            nA += vA * vA;
            nB += vB * vB;
        }
        if (nA == 0 || nB == 0) return 0;
        return dot / (Math.sqrt(nA) * Math.sqrt(nB));
    }

    private double commonWords(String[] a, String[] b) {
        Set<String> sb = new HashSet<>(Arrays.asList(b));
        int c = 0;
        for (String w : a) if (sb.contains(w)) c++;
        return c;
    }

    private String normalize(String text) {
        return text.toLowerCase()
            .replaceAll("[^\\p{L}\\p{N}\\s]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private String pick(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }

    public void saveModel() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.writeString(Paths.get(MODEL_FILE), gson.toJson(trainingData));
        } catch (IOException e) {
            HolyWorldAIClient.LOGGER.error("Save failed", e);
        }
    }

    public void loadModel() {
        try {
            Path p = Paths.get(MODEL_FILE);
            if (Files.exists(p)) {
                String json = Files.readString(p);
                Type t = new TypeToken<Map<String, List<String>>>() {}.getType();
                trainingData = gson.fromJson(json, t);
                if (trainingData == null) trainingData = new HashMap<>();
                for (String key : trainingData.keySet()) {
                    Collections.addAll(vocabulary, key.split("\\s+"));
                }
            }
        } catch (Exception e) {
            HolyWorldAIClient.LOGGER.error("Load failed", e);
            trainingData = new HashMap<>();
        }
    }

    public void resetModel() {
        trainingData.clear();
        vocabulary.clear();
        saveModel();
    }

    public int getTrainingDataSize() {
        return trainingData.values().stream().mapToInt(List::size).sum();
    }
}

package me.catand.spdnetserver.service;

import jakarta.annotation.PostConstruct;
import me.catand.spdnetserver.SpdProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class BannedWordsService {

    private static final Logger log = LoggerFactory.getLogger(BannedWordsService.class);

    @Autowired
    private SpdProperties spdProperties;

    private Map<Character, Object> dfaMap = new HashMap<>();
    private final Set<Character> ignoreChars = Set.of(' ', '*', '-', '_', '.', ',', '!', '?', '@', '#', '$', '%', '^', '&', '(', ')', '[', ']', '{', '}', '|', '\\', '/', '<', '>', '~', '`');

    @PostConstruct
    public void init() {
        loadBannedWords();
    }

    public synchronized void loadBannedWords() {
        String filePath = spdProperties.getBannedWordsFile();
        if (filePath == null || filePath.isEmpty()) {
            log.warn("未配置屏蔽词文件路径");
            return;
        }

        List<String> words = new ArrayList<>();
        
        java.io.File externalFile = new java.io.File(filePath);
        if (externalFile.exists() && externalFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(externalFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        words.add(line.toLowerCase());
                    }
                }
                log.info("从外部文件加载了 {} 个屏蔽词: {}", words.size(), filePath);
            } catch (IOException e) {
                log.error("加载外部屏蔽词文件失败: {}", filePath, e);
                return;
            }
        } else {
            InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
            if (is == null) {
                log.error("找不到屏蔽词文件: {} (外部文件和classpath都不存在)", filePath);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        words.add(line.toLowerCase());
                    }
                }
                log.info("从classpath加载了 {} 个屏蔽词: {}", words.size(), filePath);
            } catch (IOException e) {
                log.error("加载classpath屏蔽词文件失败: {}", filePath, e);
                return;
            }
        }

        buildDfaMap(words);
        log.info("DFA 敏感词树构建完成，共 {} 个词", words.size());
    }

    @SuppressWarnings("unchecked")
    private void buildDfaMap(List<String> words) {
        dfaMap = new HashMap<>();
        for (String word : words) {
            if (word == null || word.isEmpty()) continue;
            Map<Character, Object> current = dfaMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Object next = current.get(c);
                if (next == null) {
                    Map<Character, Object> newNode = new HashMap<>();
                    current.put(c, newNode);
                    current = newNode;
                } else {
                    current = (Map<Character, Object>) next;
                }
            }
            current.put('\0', true);
        }
    }

    public boolean containsBannedWord(String text) {
        if (text == null || text.isEmpty()) return false;
        String lowerText = text.toLowerCase();
        for (int i = 0; i < lowerText.length(); i++) {
            int matchLen = matchDfa(lowerText, i);
            if (matchLen > 0) return true;
        }
        return false;
    }

    public String getFirstBannedWord(String text) {
        if (text == null || text.isEmpty()) return null;
        String lowerText = text.toLowerCase();
        for (int i = 0; i < lowerText.length(); i++) {
            int matchLen = matchDfa(lowerText, i);
            if (matchLen > 0) {
                return lowerText.substring(i, i + matchLen);
            }
        }
        return null;
    }

    public String censorText(String text) {
        if (text == null || text.isEmpty()) return text;
        String lowerText = text.toLowerCase();
        StringBuilder result = new StringBuilder(text);
        int i = 0;
        while (i < lowerText.length()) {
            int matchLen = matchDfaWithIgnore(lowerText, i);
            if (matchLen > 0) {
                int actualLen = 0;
                int j = i;
                while (actualLen < matchLen && j < result.length()) {
                    char c = lowerText.charAt(j);
                    if (!ignoreChars.contains(c)) {
                        result.setCharAt(j, '*');
                        actualLen++;
                    }
                    j++;
                }
                i = j;
            } else {
                i++;
            }
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private int matchDfa(String text, int start) {
        Map<Character, Object> current = dfaMap;
        int matchLen = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (ignoreChars.contains(c)) continue;
            current = (Map<Character, Object>) current.get(c);
            if (current == null) break;
            if (current.containsKey('\0')) {
                matchLen = i - start + 1;
            }
        }
        return matchLen;
    }

    @SuppressWarnings("unchecked")
    private int matchDfaWithIgnore(String text, int start) {
        Map<Character, Object> current = dfaMap;
        int matchLen = 0;
        int charCount = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (ignoreChars.contains(c)) continue;
            current = (Map<Character, Object>) current.get(c);
            if (current == null) break;
            charCount++;
            if (current.containsKey('\0')) {
                matchLen = charCount;
            }
        }
        return matchLen;
    }

    public synchronized void reload() {
        loadBannedWords();
    }
}

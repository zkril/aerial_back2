package com.zkril.aerial_back.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VerifyCodeCache {

    private static class CodeEntry {
        String code;
        long timestamp;

        public CodeEntry(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();

    public void save(String email, String code) {
        cache.put(email, new CodeEntry(code, System.currentTimeMillis()));
    }

    public boolean verify(String email, String code) {
        CodeEntry entry = cache.get(email);
        if (entry == null) return false;

        long now = System.currentTimeMillis();
        // 5分钟 = 300000ms
        if ((now - entry.timestamp) > 5 * 60 * 1000) {
            cache.remove(email);
            return false;
        }
        return entry.code.equals(code);
    }

    public void remove(String email) {
        cache.remove(email);
    }
}

package utils;

import java.util.HashMap;
import java.util.Map;

public class Cache {
    private final int N;
    private final float theta;
    private final Map<String, CacheEntry> map;

    public Cache(int N, float theta) {
        this.N = N;
        this.theta = theta;
        this.map = new HashMap<>();
    }

    public void add(String request, String response) {
        if (map.size() >= this.N) {
            // getting frequency total and getting lowest frequency key
            int totalFreq = 0;
            int minFreq = Integer.MAX_VALUE;
            String toDel = null;
            for (Map.Entry<String, CacheEntry> entry  : map.entrySet()) {
                totalFreq += entry.getValue().freq;
                if (entry.getValue().freq < minFreq) {
                    toDel = entry.getKey();
                }
            }

            // reducing frequency if threshold is crossed
            if (totalFreq/(float) map.size() > this.theta) {
                for (CacheEntry ce : map.values()) {
                    ce.freq /= 2;
                }
            }

            // Removing the lowest frequency item
            synchronized (map) {
                map.remove(toDel);
            }
        }

        // Adding the new element
        synchronized (map) {
            map.put(request, new CacheEntry(response, 1));
        }
    }

    // Returns the response to the request if the object is in cache, null otherwise
    public String get(String request) {
        if (map.containsKey(request)) {
            CacheEntry ce = map.get(request);
            ce.freq += 1;
            return ce.response;
        } else {
            return null;
        }
    }

    // Object we are storing in the cache (contains the response and the frequency)
    public static class CacheEntry {
        public String response;
        public int freq;

        public CacheEntry(String response, int freq) {
            this.response = response;
            this.freq = freq;
        }
    }
}


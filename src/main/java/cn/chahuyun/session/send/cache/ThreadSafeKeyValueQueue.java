package cn.chahuyun.session.send.cache;


import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 这是一个线程安全的，具有先进先出的键值对缓存
 *
 * @author Moyuyanli
 * @date 2024/7/11 11:12
 */

public class ThreadSafeKeyValueQueue<K, V> {
    private final LinkedBlockingQueue<Map.Entry<K, V>> queue;
    private final int maxCapacity;
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public ThreadSafeKeyValueQueue(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.queue = new LinkedBlockingQueue<>(maxCapacity);
    }

    public void put(K key, V value) {
        synchronized (this) {
            if (queue.size() >= maxCapacity) {
                Map.Entry<K, V> removedEntry = queue.poll();
                if (removedEntry != null) {
                    map.remove(removedEntry.getKey());
                }
            }
                queue.add(new AbstractMap.SimpleEntry<>(key, value));
            map.put(key, value);
        }
    }

    public V get(K key) {
        return map.get(key);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void clear() {
        synchronized (this) {
            queue.clear();
            map.clear();
        }
    }
}
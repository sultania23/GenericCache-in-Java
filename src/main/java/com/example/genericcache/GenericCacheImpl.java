package com.example.genericcache;

import lombok.Synchronized;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GenericCacheImpl<K,V> implements IGeneric<K,V>{

    public static final long DEFAULT_CACHE_TIME_OUT = 60000L;

    protected long cacheTimeOut;

    protected Map<K, CacheValue<V> > map;

    public GenericCacheImpl()
    {
        this(DEFAULT_CACHE_TIME_OUT);
    }
    
    public GenericCacheImpl(Long cacheTimeOut)
    {
        this.cacheTimeOut =cacheTimeOut;
        this.clear();
    }

    protected interface CacheValue<V> {
        V getValue();
        LocalDateTime getCreatedAt();
    }
    
    @Override
    @Synchronized
    public void clean() {

        Set<K> expiredKeys = this.getExpiredKeys();
        for (K key : expiredKeys)
        {
            this.remove(key);
        }
    }

    private Set<K> getExpiredKeys()
    {
        return this.map.keySet().parallelStream().filter(this::isExpired).collect(Collectors.toSet());
    }

    private boolean isExpired(K key)
    {
        LocalDateTime expired = this.map.get(key).getCreatedAt().plus(this.cacheTimeOut, ChronoUnit.MILLIS);
        return LocalDateTime.now().isAfter(expired);
    }

    @Override
    public void clear() {
       map = new HashMap<>();
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public Optional get(K key) {
        this.clean();
        return Optional.ofNullable(this.map.get(key).getValue());
    }

    @Override
    public void put(K key, V value) {
        map.put(key, this.createCacheValue(value));
    }

    protected CacheValue<V> createCacheValue(V value) {
        LocalDateTime now  = LocalDateTime.now();
        return new CacheValue<V>() {
            @Override
            public V getValue() {
                return value;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return now;
            }
        };
    }

    @Override
    public void remove(K key) {
        this.map.remove(key);
    }
}

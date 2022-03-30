package com.example.genericcache;

import java.util.Optional;

public interface IGeneric<K,V> {
    void clean();

    void clear();

    boolean containsKey(K key);

    Optional<V> get(K key);

    void put(K Key,V value);

    void remove(K key);

}

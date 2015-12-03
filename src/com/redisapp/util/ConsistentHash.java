package com.redisapp.util;

import java.util.*;
import java.util.logging.Logger;

public final class ConsistentHash<T> {

    private static final Logger log = Logger.getLogger(ConsistentHash.class.getName());

    private final HashFunction hashFunction;
    private final int numberOfReplicas;
    
    private final SortedMap<Integer, T> circle = new TreeMap<Integer, T>() {
        @Override
        public String toString() {
            Iterator<Map.Entry<Integer, T>> i = entrySet().iterator();
            if (!i.hasNext())
                return "{}";

            StringBuilder sb = new StringBuilder();
            sb.append('{');
            for (; ; ) {
                Map.Entry<Integer, T> e = i.next();
                T value = e.getValue();
                sb.append(value == this ? "(this Map)" : value);
                if (!i.hasNext())
                    return sb.append('}').toString();
                sb.append(',').append(' ');
            }
        }
    };

    
    public ConsistentHash(final HashFunction hashFunction, final int numberOfReplicas,
                          final Collection<T> nodes) {
        this.hashFunction = hashFunction;
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            log.info("Adding node : " + node);
            this.add(node);
        }
    }

    
    @Override
    public String toString() {
        return "ConsistentHash [ circle " + circle + " ] ";
    }

    
    public void add(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.put(hashFunction.hash(node.toString() + i), node);
        }
    }

    
    public void remove(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.remove(hashFunction.hash(node.toString() + i));
        }
    }

    
    public T get(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hashFunction.hash((String) key);
        if (!circle.containsKey(hash)) {
            SortedMap<Integer, T> tailMap =  circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }
}
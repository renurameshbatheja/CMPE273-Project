package com.redisapp.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

public final class RendezvousHash<T extends RedisServerInfo> {

    private static final Logger log = Logger.getLogger(RendezvousHash.class.getName());
    private Collection<T> nodes = new HashSet<T>();
    private final HashFunction hashFunction;

    public RendezvousHash(HashFunction hashFunction,
                          Collection<T> nodes) {
        this.hashFunction = hashFunction;
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "HRW [ nodes " + nodes + " ] ";
    }

    public void add(T node) {
        nodes.add(node);
    }

    public void remove(T node) {
        nodes.remove(node);
    }

    public T get(Object key) {
        T selectedNode = null;
        Integer biggestHash = Integer.MIN_VALUE;
        for (T node : nodes) {
            Integer newHash = hashFunction.hash(node.getId() + "-" + key);
            if (newHash > biggestHash) {
                biggestHash = newHash;
                selectedNode = node;
            }
        }
        return selectedNode;
    }
}

package com.redisapp.util;

import java.util.HashMap;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;


/**
 * RedisManager - This is a controller class which calls appropriate service methods
 * for dynamically adding/removing a Redis Instance
 *
 */
public final class RedisManager {
    private static final Logger log = Logger.getLogger(RedisManager.class.getName());

    
    /**
     * @param host
     * @param port
     * @param id
     * Method to add a Redis Instance
     */
    public static void addNodesDynamic(final String host, final String port, final String id) {
        storeOldServersAndRh();

        RedisServerInfo server = new RedisServerInfo(host, port, id);
        log.info("Adding new redis server as " + server);

        RedisClient.servers.add(server);
        Jedis jedis = new Jedis(host, new Integer(port));
        RedisClient.serverObjs.put(server, jedis);
        RedisClient.rh = new RendezvousHash<>(new HashFunction(), RedisClient.servers);

        log.info("Servers added :  " + RedisClient.servers);
        distributeAgain("Add", host, port, id);
    }


    /**
     * Store old servers and HRW hashing information
     */
    private static void storeOldServersAndRh() {
        RedisClient.oldSetOfServers.clear();
        RedisClient.oldSetOfServers.addAll(RedisClient.servers);

        RedisClient.oldRh = new RendezvousHash<>(new HashFunction(), RedisClient.oldSetOfServers);
        RedisClient.oldServerObjs = (HashMap)((HashMap)RedisClient.serverObjs).clone();
    }


    /**
     * @param method
     * @param host
     * @param port
     * @param id
     * Re-Distribute data if a Redis Instance is being added/removed 
     */
    private static void distributeAgain(String method, String host, String port, String id) {
        try {
            RedisClient.updateHRWRing(method, host, port, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param host
     * @param port
     * @param id
     * Method to remove a Redis Instance
     */
    public static void removeNode(final String host, final String port, final String id) {
        storeOldServersAndRh();

        RedisServerInfo server = new RedisServerInfo(host, port, id);
        log.info("Removing redis server as " + server);
        RedisClient.servers.remove(server);
        RedisClient.rh = new RendezvousHash<>(new HashFunction(), RedisClient.servers);
        RedisClient.serverObjs.remove(server);

        log.info("Servers after removal :  " + RedisClient.servers);
        distributeAgain("Remove", host, port, id);
    }
}

package com.redisapp.util;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * Class to rehash and redistribute when a new Redis Instance 
 * is added in the system.
 *
 */
public final class RehashAndAddTask implements Runnable {

    private static final Logger log = Logger.getLogger(RedisClient.class.getName() + Thread.currentThread().getName());

    private final RedisServerInfo redisServer;

    private final CyclicBarrier barrier;


    public RehashAndAddTask(RedisServerInfo server, CyclicBarrier barrier) {
        this.redisServer = server;
        this.barrier = barrier;
    }

    
    @Override
    public void run() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisServer.getHost(), redisServer.getPort());

        //Get all keys
        Jedis jedis = jedisPool.getResource();
        Set<String> keys = jedis.keys("*");
        jedis.close();

        //List of keys to be deleted from this server
        List<String> toDeleteList = new LinkedList<>();

        //Map of keys-values to be added to other servers
        Map<RedisServerInfo, List<String>> toAddServerKeyValues = new HashMap<>();

        for (String key : keys) {
            //Check if key needs moving
            RedisServerInfo oldRSI = RedisClient.oldRh.get(key);
            RedisServerInfo newRSI = RedisClient.rh.get(key);

            if (!oldRSI.equals(newRSI)) {
                toDeleteList.add(key);
                List<String> keyValuesToAdd = toAddServerKeyValues.get(newRSI);
                if (keyValuesToAdd == null) {
                    keyValuesToAdd = new LinkedList<>();
                }
                //Get Value
                Jedis j = jedisPool.getResource();
                String value = j.get(key);
                j.close();

                keyValuesToAdd.add(key);
                keyValuesToAdd.add(value);

                toAddServerKeyValues.put(newRSI, keyValuesToAdd);
            }
        }

        log.info("Moving " + toDeleteList.size() + " keys for server " + redisServer);
        if (toDeleteList.size() > 0) {
            //Bulk delete
            Jedis j = jedisPool.getResource();
            j.del(toDeleteList.toArray(new String[toDeleteList.size()]));
            j.close();
        }

        //Bulk add
        for (Map.Entry<RedisServerInfo, List<String>> entry : toAddServerKeyValues.entrySet()) {
            RedisServerInfo rsi = entry.getKey();
            List<String> addList = entry.getValue();
            Jedis je = new Jedis(rsi.getHost(), rsi.getPort());
            je.mset(addList.toArray(new String[addList.size()]));
            je.close();
        }

        try {
            log.info(new Date() + " Finished redistributing keys for "
                    + Thread.currentThread().getName());
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        
        jedisPool.close();
    }
}

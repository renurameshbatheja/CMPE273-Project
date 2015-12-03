package com.redisapp.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * RedisClient - This client distributes the data (web cache)
 * on multiple redis instances based on HRW hashing.
 *
 */
public class RedisClient {

    private static final Logger log = Logger.getLogger(RedisClient.class.getName());

    public static Set<RedisServerInfo> servers = new HashSet<RedisServerInfo>();
    public static Set<RedisServerInfo> oldSetOfServers = new HashSet<RedisServerInfo>();

    public static RendezvousHash<RedisServerInfo> rh;
    public static RendezvousHash<RedisServerInfo> oldRh;
    public static Map<RedisServerInfo, Jedis> serverObjs = new HashMap<RedisServerInfo, Jedis>();
    public static Map<RedisServerInfo, Jedis> oldServerObjs = new HashMap<RedisServerInfo, Jedis>();
    public static final String cachedDir = "cache" + File.separator;

    
    /**
     * Initialization method - for distributing (web cache) data
     * statically on the already configured and available redis instances in the system. 
     */
    public static void init() {
        log.info("Initialising data with first redis instance node");
        new File(cachedDir).mkdirs();
        String redisServers = ConfigProperties.prop.getProperty("redisInstances");
        log.info("RedisInstances : " + redisServers);
        String[] redisServerList = redisServers.split(",");
        Integer serverId = 1;
        for(String redisServer : redisServerList) {
        	log.info("Redis Server Instance : " + redisServer);
        	String[] serverDetails = redisServer.split(":");
        	servers.add(new RedisServerInfo(serverDetails[0], serverDetails[1], serverId.toString()));
        	log.info("Added in the servers : " + serverDetails[0]+ ":" + serverDetails[1] + ":" + serverId.toString());
        	serverId++;
        }

        rh = new RendezvousHash<>(new HashFunction(), servers);
        try {
            updateHRWRing("Init", "", "", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Initialization method - to be called on Server startup 
     */
    public static void initNodeAndData() {
        init();
        try {
            //distributeStaticData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param method
     * @param host
     * @param port
     * @param id
     * @throws Exception
     * Update HRW Ring based on new addition/removal of Redis Instance
     */
    public static void updateHRWRing(String method, String host, String port, String id) throws Exception {
        log.info("Old set of Servers :  " + oldSetOfServers);
        log.info("New set of Servers :  " + servers);
        if (method.equals("Init")) {
            serverObjs.clear();
            for (RedisServerInfo rsi : servers) {
                log.info("rsi : " + rsi.toString());
                Jedis jedis = new Jedis(rsi.getHost(), rsi.getPort(), 10000, 10000);
                serverObjs.put(rsi, jedis);
            }
        }
        log.info("ServerObjs after Init : " + serverObjs);
        if (method.equals("Add")) {
            redistributeAfterAddition(host, port);
        } else if (method.equals("Remove")) {
            redistributeAfterRemoval(host, port, id);
        }

    }


    /**
     * @param host
     * @param port
     * @throws UnsupportedEncodingException
     * @throws Exception
     * If a new Redis Instance is added, it gets its share of key-value pairs from 
     * already configured and available Redis Instances in the system.
     */
    private static void redistributeAfterAddition(String host, String port)
            throws UnsupportedEncodingException, Exception {
        final CyclicBarrier cb = new CyclicBarrier(oldSetOfServers.size() + 1, new Runnable() {
            @Override
            public void run() {
                log.info(new Date() + " Barrier reached by all threads");
            }
        });

        for (RedisServerInfo rsi : oldSetOfServers) {
            log.info("RSI : " + rsi.toString());
            Thread t1 = new Thread(new RehashAndAddTask(rsi, cb), ("Thread-" + rsi.toString()));
            t1.start();
        }
        cb.await();
    }

    
    /**
     * @param host
     * @param port
     * @param id
     * @throws UnsupportedEncodingException
     * @throws Exception
     * If a Redis Instance is removed, it distributes its key-value pairs of web cache
     * among the already available and configured Redis Instances.
     * 
     */
    private static void redistributeAfterRemoval(String host, String port, String id)
            throws UnsupportedEncodingException, Exception {
        RedisServerInfo redisServer = new RedisServerInfo(host, port, id);
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisServer.getHost(), redisServer.getPort());

        //Get all keys
        Jedis jedis = jedisPool.getResource();
        Set<String> keys = jedis.keys("*");
        jedis.close();

        //Map of keys-values to be added to other servers
        Map<RedisServerInfo, List<String>> toAddServerKeyValues = new HashMap<RedisServerInfo, List<String>>();

        for (String key : keys) {
            //Check if key needs moving
            RedisServerInfo newRSI = RedisClient.rh.get(key);

            List<String> keyValuesToAdd = toAddServerKeyValues.get(newRSI);
            if (keyValuesToAdd == null) {
                keyValuesToAdd = new LinkedList<String>();
            }
            //Get Value
            Jedis j = jedisPool.getResource();
            String value = j.get(key);
            j.close();

            keyValuesToAdd.add(key);
            keyValuesToAdd.add(value);

            toAddServerKeyValues.put(newRSI, keyValuesToAdd);
        }

        if (keys.size() > 0) {
            //Bulk delete
            Jedis j = jedisPool.getResource();
            j.del(keys.toArray(new String[keys.size()]));
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
        
        jedisPool.close();
    }

    
    /**
     * @throws Exception
     * Method to distribute static data for initial build-up of web cache.
     */
    public static void distributeStaticData() throws Exception {
        Integer i = 3400;
        BinaryJedis j;
        String url = "http://www.google.com/search?q=";
        byte[] compressedWebPageContent = null;
        while (i-- > 2000) {
            String key = url + i;

            RedisServerInfo s = rh.get(key);
            j = serverObjs.get(s);
            
            //compressedWebPageContent = HttpURLConnectionExample.sendGet(key);
            File cacheFile = new File(cachedDir + i);
            if (!cacheFile.exists()) {
            	compressedWebPageContent = HttpURLConnectionExample.sendGet(key);
                Files.write(cacheFile.toPath(), compressedWebPageContent, StandardOpenOption.CREATE);
            }

            compressedWebPageContent = Files.readAllBytes(cacheFile.toPath());
            log.info("Saving : " + key + " : " + compressedWebPageContent + " to " + s);
            j.set(key.getBytes(), compressedWebPageContent);
            j.save();
        }
    }


    /**
     * @param query
     * @return
     * @throws Exception
     * Method to retrieve web cache results stored in Redis Instances.
     * 
     */
    public static ObjectToJsp storeDataInCache(String query) throws Exception {
        String url = "http://www.google.com/search?q=";

        //First check if the same is present in cache or not
        RedisServerInfo server = rh.get(url + query);
        BinaryJedis jremote = serverObjs.get(server);
        log.info("Jremote : " + jremote.toString());

        byte[] compressedWebPageContent = jremote.get((url + query).getBytes("UTF-8"));
        log.info("Received compressed value : " + compressedWebPageContent);
        ObjectToJsp data = new ObjectToJsp();
        if (compressedWebPageContent == null || compressedWebPageContent.length == 0) {
            //Cache miss!
            //If not, save it!
            log.info("Cache Miss!");
            compressedWebPageContent = HttpURLConnectionExample.sendGet(url + query);
            log.info("Saving this : " + url + query + " : " + compressedWebPageContent);
            jremote.set((url + query).getBytes(), compressedWebPageContent);
            jremote.save();
            data.hitormiss = 0;
            //return "Cache Miss";
        } else {
            //Cache hit!
            log.info("Cache Hit!");
            data.hitormiss = 1;
            //	return "Cache Hit";
        }
        data.content = HttpURLConnectionExample.decompress(compressedWebPageContent);
        return data;
    }

    
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Thread mainThread = new Thread() {
            @Override
            public void run() {
                synchronized (RedisClient.class) {
                    init();
                    try {
                        //distributeStaticData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread managerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    synchronized (RedisClient.class) {
                        //log.info("Adding/Removing a node!");
                        //RedisManager.addNodesDynamic("127.0.0.3", "6379");
                        //RedisManager.removeNode("127.0.0.3", "6379");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mainThread.start();
        managerThread.start();
    }

    
    /**
     * @param infoParam
     * @return
     * Parse and display Redis Server Information for monitoring purpose.
     */
    public static Map<String, String> parseInfo(String infoParam) {
        String serverInfo[];
        Map<String, String> infoParams = new HashMap<String, String>();

        serverInfo = infoParam.split("\\n");
        for (int i = 0; i < serverInfo.length; i++) {
            String params[];
            params = serverInfo[i].toString().split(":");
            if (params.length == 2) {
                infoParams.put(params[0].toString(), params[1].toString());
            }
        }
        return infoParams;
    }

    
    /**
     * @param server
     * @param jedis
     * Retrieve 'Server' Section information for Redis Instance
     */
    public static void setServerParams(RedisServerInfo server, Jedis jedis) {
        String serverSection = jedis.info("Server");
        Map<String, String> serverParams = new HashMap<String, String>();
        serverParams = parseInfo(serverSection);

        if (serverParams.containsKey("redis_version")) {
            String redis_version = serverParams.get("redis_version");
            server.setRedisVersion(redis_version.trim());
        }
        if (serverParams.containsKey("uptime_in_seconds")) {
            String uptime_in_seconds = serverParams.get("uptime_in_seconds");
            server.setUptimeInSeconds(uptime_in_seconds);
        }
    }

    
    /**
     * @param server
     * @param jedis
     * Retrieve 'Clients' Section information for Redis Instance
     */
    public static void setClientParams(RedisServerInfo server, Jedis jedis) {
        String clientSection = jedis.info("Clients");
        Map<String, String> clientParams = new HashMap<String, String>();
        clientParams = parseInfo(clientSection);

        if (clientParams.containsKey("connected_clients")) {
            String connected_clients = clientParams.get("connected_clients");
            server.setConnectedClients(connected_clients);
        }

    }

    
    /**
     * @param server
     * @param jedis
     * Retrieve 'Memory' Section information for Redis Instance
     */
    public static void setMemoryParams(RedisServerInfo server, Jedis jedis) {
        String memorySection = jedis.info("Memory");
        Map<String, String> memoryParams = new HashMap<String, String>();
        memoryParams = parseInfo(memorySection);

        if (memoryParams.containsKey("used_memory")) {
            String used_memory = memoryParams.get("used_memory");
            server.setUsedMemory(used_memory);
        }
    }

    
    /**
     * @param server
     * @param jedis
     * Retrieve 'Stats' Section information for Redis Instance
     */
    public static void setStatsParams(RedisServerInfo server, Jedis jedis) {
        String statsSection = jedis.info("Stats");
        Map<String, String> statsParams = new HashMap<String, String>();
        statsParams = parseInfo(statsSection);

        if (statsParams.containsKey("keyspace_hits")) {
            String keyspace_hits = statsParams.get("keyspace_hits");
            server.setKeyHits(keyspace_hits);
        }

        if (statsParams.containsKey("keyspace_misses")) {
            String keyspace_misses = statsParams.get("keyspace_misses");
            server.setKeyMisses(keyspace_misses);
        }
    }

    
    /**
     * @param server
     * @param jedis
     * Retrieve 'Replication' Section information for Redis Instance
     */
    public static void setreplicationParams(RedisServerInfo server, Jedis jedis) {
        String replicationSection = jedis.info("Replication");
        Map<String, String> replicationParams = new HashMap<String, String>();
        replicationParams = parseInfo(replicationSection);

        if (replicationParams.containsKey("role")) {
            String role = replicationParams.get("role");
            server.setRole(role);
        }
    }

    
    /**
     * @param server
     * @param jedis
     * Retrieve 'Keyspace' Section information for Redis Instance
     */
    public static void setKeyspaceParams(RedisServerInfo server, Jedis jedis) {
        String keyspaceSection = jedis.info("Keyspace");
        Map<String, String> keyspaceParams = new HashMap<String, String>();
        keyspaceParams = parseInfo(keyspaceSection);

        if (keyspaceParams.containsKey("db0")) {
            String info = keyspaceParams.get("db0");
            String infoparams[] = info.split(",");
            String keyinfo[] = infoparams[0].split("=");

            if (keyinfo.length >= 2) {
                String key = keyinfo[0].toString();
                String value = keyinfo[1].toString();
                if (key.equals("keys")) {
                    server.setNoOfKeys(value);
                }
            }
        }
    }

    
    /**
     * @param server
     * @param jedis
     * Retrieve information for Redis Instance
     */
    public static void getRedisServerInfo(RedisServerInfo server, Jedis jedis) {
        //sets the parameters like redis version and uptime in seconds for redis info server class.
        setServerParams(server, jedis);

        setClientParams(server, jedis);

        setMemoryParams(server, jedis);

        setStatsParams(server, jedis);

        setreplicationParams(server, jedis);

        setKeyspaceParams(server, jedis);

    }

}


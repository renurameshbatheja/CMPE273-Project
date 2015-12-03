package com.redisapp.util;

/**
 * A POJO to save Redis Server Information
 *
 */
public final class RedisServerInfo {
    private final String host;
    private final Integer port;

    //#Server section
    private String redisVersion;
    private String uptimeInSeconds;

    //#Clients section
    private String connectedClients;

    //#Memory
    private String usedMemory;

    //#Stats
    private String keyHits;
    private String keyMisses;

    //#Replication
    private String role;

    //#Keyspace
    private String noOfKeys;

    private String id;

    public RedisServerInfo(String host, String port, String id) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RedisServerInfo that = (RedisServerInfo) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return new Integer(id);
    }

    public String getRedisVersion() {
        return redisVersion;
    }

    public void setRedisVersion(String redisVersion) {
        this.redisVersion = redisVersion;
    }

    public String getUptimeInSeconds() {
        return uptimeInSeconds;
    }

    public void setUptimeInSeconds(String uptimeInSeconds) {
        this.uptimeInSeconds = uptimeInSeconds;
    }

    public String getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(String connectedClients) {
        this.connectedClients = connectedClients;
    }

    public String getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getKeyHits() {
        return keyHits;
    }

    public void setKeyHits(String keyHits) {
        this.keyHits = keyHits;
    }

    public String getKeyMisses() {
        return keyMisses;
    }

    public void setKeyMisses(String keyMisses) {
        this.keyMisses = keyMisses;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNoOfKeys() {
        return noOfKeys;
    }

    public void setNoOfKeys(String noOfKeys) {
        this.noOfKeys = noOfKeys;
    }


}

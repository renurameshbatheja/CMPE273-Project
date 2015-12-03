package com.redisapp.util;

/**
 * Hash function used for calculating hash.
 */
public class HashFunction {

    public Integer hash(String s) {
        return MurmurHash3.murmurhash3_x86_32(s, 0, s.length(), 0xf7ca7fd2);
    }
}

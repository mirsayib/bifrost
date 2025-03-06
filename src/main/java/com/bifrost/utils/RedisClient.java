package com.bifrost.utils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisClient {
	private static final JedisPool jedisPool = new JedisPool("localhost", 6379);

	public static Jedis getJedis() {
		return jedisPool.getResource();
	}
}

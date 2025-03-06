package com.bifrost.ratelimiter;

import com.bifrost.utils.RedisClient;
import redis.clients.jedis.Jedis;

import java.time.Instant;

public class RedisRateLimiter {
	private static final int REQUEST_LIMIT = 5;
	private static final int TIME_WINDOW = 10;   // Only five requests per 10 seconds

	private RedisRateLimiter() {
		// UTIL CLASS
	}

	public static boolean isAllowed(String clientIp){
		try(Jedis jedis = RedisClient.getJedis()){
			String key = "rate:" + clientIp;
			long now = Instant.now().getEpochSecond();
			jedis.zremrangeByScore(key, 0, (double) now - TIME_WINDOW);
			long requestCount = jedis.zcard(key);

			if(requestCount >= REQUEST_LIMIT){
				return false;
			}

			jedis.zadd(key, now, String.valueOf(now));
			jedis.expire(key, TIME_WINDOW);
			return true;
		}
	}
}

/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.devops.iam.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.util.Assert;

import redis.clients.jedis.JedisCluster;

/**
 * RedisCache Manager implements let Shiro use Redis caching
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @time 2017年4月13日
 * @since
 */
public class JedisCacheManager implements EnhancedCacheManager {

	final private Map<String, EnhancedCache> caching = new ConcurrentHashMap<>();

	private String prefix;
	private JedisCluster jedisCluster;

	public JedisCacheManager(String prefix, JedisCluster jedisCluster) {
		Assert.notNull(prefix, "'prefix' must not be null");
		Assert.notNull(jedisCluster, "'jedisCluster' must not be null");
		this.prefix = prefix;
		this.jedisCluster = jedisCluster;
	}

	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Cache<EnhancedKey, Object> getCache(String name) throws CacheException {
		return this.getEnhancedCache(name);
	}

	/**
	 * Getting enhanced cache instance
	 * 
	 * @param name
	 * @return
	 * @throws CacheException
	 */
	@Override
	public EnhancedCache getEnhancedCache(String name) throws CacheException {
		String cacheName = this.getCacheName(name);
		EnhancedCache cache = this.caching.get(cacheName);
		if (cache == null) {
			this.caching.put(cacheName, (cache = new JedisEnhancedCache(cacheName, this.jedisCluster)));
		}
		return cache;
	}

	private String getCacheName(String name) {
		return this.prefix + name;
	}

}
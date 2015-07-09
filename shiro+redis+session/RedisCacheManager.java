package cn.yesway.yconnect.mgt.shiro.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCacheManager implements CacheManager {

	private static final Logger logger = LoggerFactory
			.getLogger(RedisCacheManager.class);

	@SuppressWarnings("rawtypes")
	private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

	private RedisSentinelManager redisSentinelManager;
 
	private String keyPrefix = SessionKeyEnum.getName(1);
	 
	public String getKeyPrefix() {
		return keyPrefix;
	}
	 
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
	
	public RedisSentinelManager getRedisSentinelManager() {
		return redisSentinelManager;
	}

	public void setRedisSentinelManager(RedisSentinelManager redisSentinelManager) {
		this.redisSentinelManager = redisSentinelManager;
	}
	
	/**
	 * 不启用缓存
	 */
	@Override
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		logger.debug("获取名称为: " + name + " 的RedisCache实例");
		
		Cache c = caches.get(name);
		
		if (c == null) {
			redisSentinelManager.init();
			c = new RedisCache<K, V>(redisSentinelManager, keyPrefix);
			caches.put(name, c);
		}
		return c;
		
	}
}

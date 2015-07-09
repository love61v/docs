/*
 * Beijing YESWAY Information Technology Co,Ltd.
 * All rights reserved.
 * 北京九五智驾信息技术股份有限公司
 * <p>RedisSentinelManager.java</p>
 */
package cn.yesway.yconnect.mgt.shiro.redis;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * redis的sentinel管理类.
 * 
 * @version : Ver 1.0
 * @author : <a href="mailto:hubo@95190.com">hubo</a>
 * @date : 2015-4-27 上午10:32:52
 */
public class RedisSentinelManager {

	/*过期时间*/
	private int expire = 2000;

	/*连接超时时间*/
	private int timeout = 2000;
	
	/*redis master name*/
	private String mastername = "";

	/* 元素格式: ip:port　　冒号连接 */
	private Set<String> sentinels = new HashSet<String>();
 
	private JedisSentinelPool jedisSentinelPool;
	
	private ShardedJedisPool shardedJedisPool;

	private RedisSentinelManager(String mastername, Set<String> sentinels) {
		this.mastername = mastername;
		this.sentinels = sentinels;
	}

	/**
	 * 初始化JedisSentinelPool
	 *
	 * @author 	: <a href="mailto:hubo@95190.com">hubo</a>  2015-4-27 上午11:10:13
	 */
	public void init() {
		if (null == jedisSentinelPool) {
			jedisSentinelPool = new JedisSentinelPool(mastername, sentinels); 
		}
	}

	public String getMastername() {
		return mastername;
	}

	public void setMastername(String mastername) {
		this.mastername = mastername;
	}

	public Set<String> getSentinels() {
		return sentinels;
	}

	public void setSentinels(Set<String> sentinels) {
		this.sentinels = sentinels;
	}

	/**
	 * get value from redis
	 * 
	 * @param key
	 * @return
	 */
	public byte[] get(byte[] key) {
		byte[] value = null;
		ShardedJedis shardedJedis = null;
		Jedis jedis = null;
		try {
			shardedJedis = shardedJedisPool.getResource();
			if (shardedJedis.exists(key)) {
				value = shardedJedis.get(key);
			}
		} finally {
			if (null != jedis) {
				jedis.close();
			}
			if (null != shardedJedis) {
				shardedJedis.close();
			}
		}

		return value;
	}
	
	
	/**
	 * get value from redis
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		String value = null;
		ShardedJedis shardedJedis = null;
		Jedis jedis = null;
		try {
			shardedJedis = shardedJedisPool.getResource();
			if (shardedJedis.exists(key)) {
				value = shardedJedis.get(key);
			}
		} finally {
			if (null != jedis) {
				jedis.close();
			}
			if (null != shardedJedis) {
				shardedJedis.close();
			}
		}

		return value;
	}

	/**
	 * set
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public byte[] set(byte[] key, byte[] value) {
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			jedis.set(key, value);
			if(this.expire != 0){
				jedis.expire(key, this.expire);
		 	}
		} finally {
			jedis.close();
		}
		return value;
	}

	public void set(String key, String value) {
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			jedis.set(key, value);
			if(this.expire != 0){
				jedis.expire(key, this.expire);
		 	}
		} finally {
			jedis.close();
		}
	}
	
	public void hset(String key,String field, String value){
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			jedis.hset(key, field, value);
			if(this.expire != 0){
				jedis.expire(key, this.expire);
		 	}
		} finally {
			jedis.close();
		}
	}
	
	public void hset(byte[] key,byte[] field, byte[] value){
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			jedis.hset(key, field, value);
			if(this.expire != 0){
				jedis.expire(key, this.expire);
		 	}
		} finally {
			jedis.close();
		}
	}
	
	public String hget(String key,String field){
		Jedis jedis = jedisSentinelPool.getResource();
		String value = null;
		try {
			value = jedis.hget(key, field);
			 
		} finally {
			jedis.close();
		}
		return value;
	}

	/**
	 * set
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 * @return
	 */
	public byte[] set(byte[] key, byte[] value, int expire) {
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			jedis.set(key, value);
			if (expire != 0) {
				jedis.expire(key, expire);
			}
		} finally {
			jedis.close();
		}
		return value;
	}

	/**
	 * del
	 * 
	 * @param key
	 */
	public void del(byte[] key) {
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			if(exists(key)){
				jedis.del(key);
			}
		} finally {
			jedis.close();
		}
	}
	
	public boolean exists(byte [] key){
		boolean flag = false;
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			flag = jedis.exists(key) ;
		}finally{
			jedis.close();
		}
		return flag;
	}
	
	/**
	 * 获得byte[]型的key
	 * @param key
	 * @return
	 */
	public byte[] getByteKey(Object key){
		if(key instanceof String){
    		return key.toString().getBytes();
    	}else{
    		return SerializeUtils.serialize(key);
    	}
	}

	/**
	 * flush
	 */
	public void flushDB() {
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			jedis.flushDB();
		} finally {
			jedis.close();
		}
	}

	/**
	 * size
	 */
	public Long dbSize() {
		Long dbSize = 0L;
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			dbSize = jedis.dbSize();
		} finally {
			jedis.close();
		}
		return dbSize;
	}

	/**
	 * keys
	 * 
	 * @param regex
	 * @return
	 */
	public Set<byte[]> keys(String pattern) {
		Set<byte[]> keys = null;
		Jedis jedis = jedisSentinelPool.getResource();
		try {
			keys = jedis.keys(pattern.getBytes());
		} finally {
			jedis.close();
		}
		return keys;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public ShardedJedisPool getShardedJedisPool() {
		return shardedJedisPool;
	}

	public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
		this.shardedJedisPool = shardedJedisPool;
	}

}

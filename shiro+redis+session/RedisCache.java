package cn.yesway.yconnect.mgt.shiro.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.yesway.yconnect.mgt.mgtservice.entity.User;

public class RedisCache<K, V> implements Cache<K, V> {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	 
	private RedisSentinelManager cache;
	
	private String keyPrefix = SessionKeyEnum.getName(1);
	 
	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
	
	/**
	 * 通过一个JedisManager实例构造RedisCache
	 */
	public RedisCache(RedisSentinelManager cache){
		 if (cache == null) {
	         throw new IllegalArgumentException("Cache argument cannot be null.");
	     }
	     this.cache = cache;
	}
	 
	public RedisCache(RedisSentinelManager cache, String prefix){
		this( cache );
		this.keyPrefix = prefix;
	}
	
	/**
	 * 获得byte[]型的key
	 * @param key
	 * @return
	 */
	private byte[] getByteKey(K key){
		if(key instanceof String){
			String preKey = this.keyPrefix + key;
    		return preKey.getBytes();
    	}else{
    		return SerializeUtils.serialize(key);
    	}
	}
 	
	@SuppressWarnings("unchecked")
	@Override
	public V get(K key) throws CacheException {
		try {
			if (key == null) {
				return null;
			} else {
				byte[] rawValue = null;
				if (key instanceof PrincipalCollection) {// 权限资源
					String authkey = makeAuthKey(key);
					logger.debug("获取 key [" + authkey + "]的值");

					rawValue = cache.get(authkey.getBytes());
				} else {
					logger.debug("获取 key [" + key + "]的值");

					rawValue = cache.get(getByteKey(key));
				}

				V value = (V) SerializeUtils.deserialize(rawValue);
				return value;
			}
		} catch (Throwable t) {
			throw new CacheException(t);
		}

	}

	/**
	 * 当前key对象是PrincipalCollection实例时构造用户权限的key
	 * @param key
	 * @return
	 */
	private String makeAuthKey(K key) {
		PrincipalCollection pc = (PrincipalCollection) key;
		User user = (User) pc.getPrimaryPrincipal();
		String authzkey = SessionKeyEnum.getName(2) + user.getUserId();
		return authzkey;
	}

	/**
	 * shiro的缓存默认存放权限资源是将PrincipalCollection接口的子类对象作为键与值的
	 * 此外用户的权限key=user_role_permission:userId组合
	 */
	@Override
	public V put(K key, V value) throws CacheException {
		try {
			if (key instanceof PrincipalCollection) {// 权限资源
				String authkey = makeAuthKey(key);
				logger.debug("存储 key [" + authkey + "]");
				cache.set(authkey.getBytes(), SerializeUtils.serialize(value));
			} else {
				logger.debug("存储 key [" + key + "]");
				cache.set(getByteKey(key), SerializeUtils.serialize(value));
			}
			return value;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@Override
	public V remove(K key) throws CacheException {
		try {
			if (key instanceof PrincipalCollection) {// 权限资源
				String authkey = makeAuthKey(key);
				logger.debug("从redis中删除 key [" + authkey + "]");
				cache.del(authkey.getBytes());
			} else {
				V previous = get(key);
				logger.debug("从redis中删除 key [" + key + "]");
				cache.del(getByteKey(key));
				
				return previous;
			}
		} catch (Throwable t) {
			throw new CacheException(t);
		}

		return null;
	}

	@Override
	public void clear() throws CacheException {
		logger.debug("从redis中删除所有元素");
		try {
            cache.flushDB();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
	}

	@Override
	public int size() {
		try {
			Long longSize = new Long(cache.dbSize());
            return longSize.intValue();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<K> keys() {
		try {
            Set<byte[]> keys = cache.keys(this.keyPrefix + "*");
            if (CollectionUtils.isEmpty(keys)) {
            	return Collections.emptySet();
            }else{
            	Set<K> newKeys = new HashSet<K>();
            	for(byte[] key:keys){
            		newKeys.add((K)key);
            	}
            	return newKeys;
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
	}

	@Override
	public Collection<V> values() {
		try {
            Set<byte[]> keys = cache.keys(this.keyPrefix + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                List<V> values = new ArrayList<V>(keys.size());
                for (byte[] key : keys) {
                    @SuppressWarnings("unchecked")
					V value = get((K)key);
                    if (value != null) {
                        values.add(value);
                    }
                }
                return Collections.unmodifiableList(values);
            } else {
                return Collections.emptyList();
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
	}

}

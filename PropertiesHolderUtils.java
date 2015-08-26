 
package cn.yesway.newenergy.common.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

/**
 *  读取属性配置文件属性值
 *
 * @version : Ver 1.0
 * @author	: <a href="mailto:358911056@qq.com">hubo</a>
 * @date	: 2015-8-4 下午3:03:37 
 */
public class PropertiesHolderUtils {

	/** src/main/resources/system.config.properties 属性文件 */
	private final static String CONFIG = "system.config.properties";
	
	/** map缓存*/
	private final static Map<String, String> cacheProperty = new ConcurrentHashMap<String, String>();
	
	/**
	 * 从缓存中取属性值
	 *
	 * @author 	: <a href="mailto:358911056@qq.com">hubo</a>  2015-8-4 下午3:03:37
	 * @param key
	 * @return
	 */
	public static String get(String key){
		String value = cacheProperty.get(key);  
		if(StringUtils.isBlank(value)){//缓存中不存在
			value = getPropertity(key);
			cacheProperty.put(key, value); //添加到缓存
		}
		return value;
	}
	
	/**
	 * 根据key获取value
	 * @param key
	 * @return  值
	 */
	public static String getPropertity(String key) {
		String value = "";
		if (null == key) {
			return value;
		}

		InputStream inputStream = null;
		try {
			inputStream  = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(CONFIG);
			Properties prop = new Properties();
			prop.load(inputStream);
			 
			value = prop.getProperty(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(null != inputStream){
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return value;
	}
	
	/*
	 * 清空缓存
	 */
	public static void clear(){
		cacheProperty.clear();
	}
	
	/**
	 * 删除
	 *
	 * @author 	: <a href="mailto:358911056@qq.com">hubo</a>  
	 * @since 	: 2015-8-26 下午2:08:38
	 * @param key
	 */
	public static void remove(String key){
		cacheProperty.remove(key);
	}
	
	/**
	 * 测试
	 *
	 * @author 	: <a href="mailto:358911056@qq.com">hubo</a>  2015-8-26 下午2:09:41
	 * @param args
	 */
	public static void main(String[] args) {
		String uri = PropertiesHolderUtils.get("VOICENOTEPAD_URL");
		System.out.println(uri);
	}
	
}

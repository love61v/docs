/*
 * Beijing YESWAY Information Technology Co,Ltd.
 * All rights reserved.
 * 北京九五智驾信息技术股份有限公司
 * <p>SessionKeyEnum.java</p>
 */
package cn.yesway.yconnect.mgt.shiro.redis;

/**
 * shrio redis用户认证与授权enum类.
 * 
 * @version : Ver 1.0
 * @author : <a href="mailto:hubo@95190.com">hubo</a>
 * @date : 2015-4-27 上午10:21:52
 */
public enum SessionKeyEnum {

	/* shiro的session对象存放到redis中的key */
	shirosessionkey("shiro_redis_session:", 1),
	
	/* shiro 的授权信息 存放到redis中hashmap中的key*/
	authz("user_role_permission:", 2),
	
	/*授权信息hashmap中角色的field*/
	role_field("user_role_:", 3),
	
	/*授权信息hashmap中权限的field*/
	permission_field("user_permission:", 4);

	private String name;

	private int index;

	private SessionKeyEnum(String name, int index) {
		this.name = name;
		this.index = index;
	}

	/**
	 * 根据index获取key名称
	 *
	 * @author 	: <a href="mailto:hubo@95190.com">hubo</a>  2015-5-11 上午9:30:15
	 * @param index 构造方法中的下标
	 * @return
	 */
	public static String getName(int index) {
		for (SessionKeyEnum sessionKeyEnum : SessionKeyEnum.values()) {
			if (sessionKeyEnum.getIndex() == index) {
				return sessionKeyEnum.name;
			}
		}

		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}

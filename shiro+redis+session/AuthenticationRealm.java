package cn.yesway.yconnect.mgt.shiro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.yesway.yconnect.mgt.mgtservice.entity.Resource;
import cn.yesway.yconnect.mgt.mgtservice.entity.Role;
import cn.yesway.yconnect.mgt.mgtservice.entity.User;
import cn.yesway.yconnect.mgt.mgtservice.service.UserService;
import cn.yesway.yconnect.mgt.shiro.redis.RedisSentinelManager;
import cn.yesway.yconnect.mgt.shiro.redis.SessionKeyEnum;

import com.alibaba.fastjson.JSON;

public class AuthenticationRealm extends AuthorizingRealm {
	
	private final static Logger logs = LoggerFactory.getLogger(AuthenticationRealm.class);
	
	private static final String DEFAULT_AUTHORIZATION_CACHE_SUFFIX = ".authorizationCache";

	@Autowired
	private UserService userService;
	
	@Autowired
	RedisSentinelManager redisSentinelManager;

	/**
	 * 认证回调函数,登录时调用.
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken)
			throws AuthenticationException {
		logs.debug("登陆认证回调函数doGetAuthenticationInfo(token)....");
		
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		String userName = token.getUsername(); // 用户名
		String password = new String(token.getPassword()); // 密码

		if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
			User user = userService.login(userName, password);
			if (user == null) {
				throw new UnknownAccountException();
			}

			SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
					user, user.getPassword(), getName());

			return authenticationInfo;
		} else {
			return null;
		}
	}

	/**
	 *授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		User user = (User) principals.getPrimaryPrincipal();
		
		if (user != null && StringUtils.isNotBlank(user.getLoginName())) {
			String key = SessionKeyEnum.getName(2) + user.getUserId();
			
			getUserRolePermissions(user, key); //获取权限
			
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			
			String roleStr = redisSentinelManager.hget(key, SessionKeyEnum.getName(3));
			if(StringUtils.isNotBlank(roleStr)){
				List<Role> roles = JSON.parseArray(roleStr, Role.class);
				info.addRoles(changeRoleList(roles));
			}
			
			String permissionStr = redisSentinelManager.hget(key, SessionKeyEnum.getName(4));
			if(StringUtils.isNotBlank(permissionStr)){
				List<Resource> permissions = JSON.parseArray(permissionStr, Resource.class);
				info.addStringPermissions(changeResourceList(permissions));
			}

			return info;
		}
		return null;
	}

	/**
	 * 从redis中获取权限,无则从数据库中查询再设置到redis的hashmap中
	 *
	 * @author 	: <a href="mailto:hubo@95190.com">hubo</a>  2015-5-8 下午1:46:00
	 * @param user
	 * @param key
	 */
	private void getUserRolePermissions(User user, String key) {
		if(!redisSentinelManager.exists(key.getBytes())){
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("userId", user.getUserId());// 当前用户ID
			
			List<Role> roleList = userService.findUserRoleList(params);
			List<Resource> resourceList = userService.findUserResourceList(params);
			
			redisSentinelManager.hset(key, SessionKeyEnum.getName(3), JSON.toJSONString(roleList));
			redisSentinelManager.hset(key, SessionKeyEnum.getName(4), JSON.toJSONString(resourceList));
		}
	}

	@Override
	protected void clearCachedAuthorizationInfo(PrincipalCollection principals) {
		logs.debug("退出清除缓存中的授权信息....");
		super.clearCachedAuthorizationInfo(principals);
		
		User user = (User) principals.getPrimaryPrincipal();
		String key = SessionKeyEnum.getName(2) + user.getUserId();
		
		redisSentinelManager.del(key.getBytes());
		
		logs.debug("退出清除缓存中的授权信息.... key[" + key +"]");
	}

	/**
	 * 资源
	 * 
	 * @author : <a href="mailto:hubo@95190.com">hubo</a> 2015-4-23 上午10:00:33
	 * @param resourceList
	 * @return
	 */
	private Collection<String> changeResourceList(List<Resource> resourceList) {
		if (null != resourceList) {
			List<String> list = new ArrayList<String>();
			for (Resource resource : resourceList) {
				list.add(resource.getPermission());
			}
			
			return list;
		}
		return null;
	}
	

	/**
	 * 角色
	 * 
	 * @author : <a href="mailto:hubo@95190.com">hubo</a> 2015-4-23 上午10:00:45
	 * @param roleList
	 * @return
	 */
	private Collection<String> changeRoleList(List<Role> roleList) {
		if (null != roleList) {
			List<String> list = new ArrayList<String>();
			for (Role role : roleList) {
				list.add(role.getRoleName());
			}
			
			return list;
		}
		return null;
	}
	
	public static String getAuthorizationCacheSuffix(){
		return DEFAULT_AUTHORIZATION_CACHE_SUFFIX;
	}
}

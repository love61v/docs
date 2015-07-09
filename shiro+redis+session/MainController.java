package cn.yesway.yconnect.mgt.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.yesway.yconnect.mgt.mgtservice.service.UserService;
import cn.yesway.yconnect.mgt.shiro.redis.RedisSentinelManager;

@Controller
@Scope("prototype")
@RequestMapping(value = "/main")
public class MainController extends BaseController{
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(MainController.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	RedisSentinelManager redisSentinelManager;

	/**
	 * 登陆
	 *
	 * @author 	: <a href="mailto:hubo@95190.com">hubo</a>  2015-5-11 上午9:45:19
	 * @param model
	 * @param username 用户名
	 * @param password 密码
	 * @param rememberMe  记住我
	 * @return
	 */
	@RequestMapping(value = "/login.html", method = RequestMethod.POST)
	public String login(ModelMap model,String username, String password, String rememberMe) {

		UsernamePasswordToken token = new UsernamePasswordToken(username,
				password, Boolean.valueOf(rememberMe));

		Subject currentUser = SecurityUtils.getSubject();
		String errorMessage = "";
		try {
			currentUser.login(token); // 登陆

			return "redirect:/main/index.html";
		} catch (UnknownAccountException e) {
			errorMessage = "用户名/密码错误";
		} catch (IncorrectCredentialsException e) {
			errorMessage = "密码错误";
		} catch (LockedAccountException e) {
			errorMessage = "用户锁定";
		} catch (DisabledAccountException e) {
			errorMessage = "用户被禁止";
		} catch (AuthenticationException e) {
			errorMessage = "未知错误";
		}
		model.addAttribute("errorMessage", errorMessage);

		return "common/login";
	}

	
	/**
	 * 登出
	 *
	 * @author 	: <a href="mailto:hubo@95190.com">hubo</a>  2015-4-23 上午11:15:32
	 * @param req
	 * @param res
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/logout.html",method=RequestMethod.GET)
	public String logouts(HttpServletRequest request) {
		log.debug("退出.......sessionId=" + request.getSession().getId());

		Subject subject = SecurityUtils.getSubject();
		subject.logout(); // 退出

		 return "redirect:/login.html";
	}
	
	/**
	 * 后台首页
	 *
	 * @author 	: <a href="mailto:hubo@95190.com">hubo</a>  2015-5-8 上午8:51:24
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/index.html", method = RequestMethod.GET)
	public String toMain(HttpServletRequest request) {
		return "common/main";
	}
	
	/**
	 * 进入登录页面
	 *
	 * @author 	: <a href="mailto:hubo@95190.com">hubo</a>  2015-5-8 上午8:51:19
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login.html", method = RequestMethod.GET)
	public String goLogin(HttpServletRequest request) {
		return "common/login";
	}
	
}

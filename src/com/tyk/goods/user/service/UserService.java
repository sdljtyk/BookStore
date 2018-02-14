package com.tyk.goods.user.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;

import com.tyk.goods.user.dao.UserDao;
import com.tyk.goods.user.domain.User;
import com.tyk.goods.user.service.exception.UserException;
import com.tyk.goods.util.SendJMail;

import cn.itcast.commons.CommonUtils;
import cn.itcast.mail.Mail;
import cn.itcast.mail.MailUtils;
/**
 * 用户模块业务层
 * @author yk
 *
 */
public class UserService {
	private UserDao userDao = new UserDao();
	
	/**
	 * 修改密码
	 * @param uid
	 * @param newPass
	 * @param oldPass
	 * @throws UserException 
	 */
	public void updatePassword(String uid, String newPass, String oldPass) throws UserException {

		try {
			/*
			 * 1. 校验老密码
			 */
			boolean bool = userDao.findByUidAndPassword(uid, oldPass);
			if(!bool) {//如果老密码错误
				throw new UserException("老密码错误！");
			}
			
			/*
			 * 2. 修改密码
			 */
			userDao.updatePassword(uid, newPass);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 登录功能
	 * @param user
	 * @return
	 */
	public User login(User user) {
		try {
			return userDao.findByLoginnameAndLoginpass(user.getLoginname(), user.getLoginpass());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 激活功能
	 * @param code
	 * @throws UserException 
	 */
	public void activatioin(String code) throws UserException {
		/**
		 * 1. 通过激活码查询用户
		 * 2. 如果User为null，说明是无效激活码，抛出异常，给出异常信息（无效激活码）
		 * 3. 查看用户状态是否为true，如果为true，抛出异常，给出异常信息（请不要二次激活）
		 * 4. 修改用户状态为true
		 */
		try {
			User user = userDao.findByCode(code);
			if(user == null) throw new UserException("无效的激活码！");
			if(user.isStatus()) throw new UserException("您已经激活过了，不要二次激活！");
			userDao.updateStatus(user.getUid(), true);//修改状态
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 用户名注册校验
	 * @param loginname
	 * @return
	 */
	public boolean ajaxValidateLoginname(String loginname) {
		try {
			return userDao.ajaxValidateLoginname(loginname);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Email校验
	 * @param email
	 * @return
	 */
	public boolean ajaxValidateEmail(String email) {
		try {
			return userDao.ajaxValidateEmail(email);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 注册功能
	 * @param user
	 */
	public void regist(User user) throws MessagingException, IOException {
		
		 // 1. 数据的补齐
		
		user.setUid(CommonUtils.uuid());
		user.setStatus(false);
		user.setActivationCode(CommonUtils.uuid() + CommonUtils.uuid());
		//2. 向数据库插入
		try {
			userDao.add(user);
			String mailMsg="恭喜，您已注册成功，请点击<a href='http://localhost:8080/goods/UserServlet?method=activation&activationCode="+user.getActivationCode()+"'>这里</a>完成激活。";
			String mail=user.getEmail();
			SendJMail.sendMail(mail, mailMsg);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}

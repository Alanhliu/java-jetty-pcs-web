package com.siasun.pcsweb.mapper;

import java.util.Map;

import com.siasun.pcsweb.beans.UserInfo;

public interface UserMapper {

	/**
	 * 查询用户
	 * 
	 * @return
	 * @throws Exception
	 */
	UserInfo getUser(Map<String, String> map) throws Exception;

	/**
	 * 更新token
	 * 
	 * @return
	 * @throws Exception
	 */
	void updateToken(UserInfo userInfo) throws Exception;

	/**
	 * 获取token
	 * 
	 * @return
	 * @throws Exception
	 */
	String getToken(String username) throws Exception;

}
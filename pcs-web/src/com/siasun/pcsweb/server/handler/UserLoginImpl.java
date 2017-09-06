package com.siasun.pcsweb.server.handler;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.siasun.pcsweb.beans.UserInfo;
import com.siasun.pcsweb.mapper.UserMapper;
import com.siasun.pcsweb.tools.DBTools;
import com.xiaoleilu.hutool.date.DateBetween;
import com.xiaoleilu.hutool.date.DateUnit;
import com.xiaoleilu.hutool.util.RandomUtil;

public class UserLoginImpl implements UrlHandlerInter {

	private final static Logger logger = LoggerFactory.getLogger(UserLoginImpl.class.getName());
	private final static int LIMIT = 60;

	@Override
	public void UrlHandler(HttpServletRequest request, HttpServletResponse response) {

		String jsoncallback = request.getParameter("jsoncallback");
		HashMap<String, Object> output = new HashMap<String, Object>();
		PrintWriter out = null;
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			out = response.getWriter();
		} catch (Exception e) {
			logger.error("Server getWriter failed.", e);
			output.put("success", "0");
			output.put("msg", e.getMessage());
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			logger.warn("Requ username/password is NULL, please check the Req URL.");
			output.put("success", "0");
			output.put("msg", "request data empty");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}
		logger.debug("Request username: {}, password: {}.", username, password);

		SqlSession session = DBTools.getSession();
		UserMapper mapper = session.getMapper(UserMapper.class);

		HashMap<String, String> map = new HashMap<>();
		map.put("username", username);
		map.put("password", password);

		UserInfo info = null;

		try {
			info = mapper.getUser(map);
			session.commit();
		} catch (Exception e) {
			logger.error("Sql exception.", e);
			output.put("success", "0");
			output.put("msg", "db exception");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		} finally {
			session.close();
		}

		if (null == info) {
			logger.error("No data or password wrong.");
			output.put("success", "0");
			output.put("msg", "no data or password wrong");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}

		DateBetween datee = new DateBetween(info.getLoginTime(), new Date());
		long minute = datee.between(DateUnit.MINUTE);
		logger.debug("{}", minute);

		if (minute < LIMIT) {
			logger.debug("In {} limit minutes.", LIMIT);
			output.put("success", "1");
			output.put("msg", "success");
			output.put("token", info.getToken());
			output.put("role", info.getRole());
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}

		// 生成token
		logger.debug("Out {} limit minutes.", LIMIT);
		String token = RandomUtil.randomNumbers(32);
		logger.debug("Token:{}.", token);

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername(username);
		userInfo.setToken(token);

		SqlSession session2 = DBTools.getSession();
		UserMapper mapper2 = session2.getMapper(UserMapper.class);
		try {
			mapper2.updateToken(userInfo);
			session2.commit();
		} catch (Exception e) {
			logger.error("Sql exception.", e);
			output.put("success", "0");
			output.put("msg", "db exception");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		} finally {
			session2.close();
		}

		output.put("success", "1");
		output.put("msg", "success");
		output.put("token", token);
		output.put("role", info.getRole());
		logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
		out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
		out.flush();
		return;
	}
}
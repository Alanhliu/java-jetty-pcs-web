package com.siasun.pcsweb.server.handler;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.siasun.pcsweb.beans.EmsInfo;
import com.siasun.pcsweb.mapper.EmsMapper;
import com.siasun.pcsweb.mapper.UserMapper;
import com.siasun.pcsweb.tools.DBTools;

public class EmsQueryImpl implements UrlHandlerInter {

	private final static Logger logger = LoggerFactory.getLogger(EmsQueryImpl.class.getName());

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
		String token = request.getParameter("token");
		String orderSn = request.getParameter("order_sn");
		String mobile = request.getParameter("mobile");
		logger.debug("username:{}, token:{}, orderSn:{}, mobile:{}.", username, token, orderSn, mobile);

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(token)) {
			logger.warn("Requ some data is NULL, please check the Req URL.");
			output.put("success", "0");
			output.put("msg", "request data empty");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}

		// check token
		SqlSession sessionToken = DBTools.getSession();
		UserMapper mapperToken = sessionToken.getMapper(UserMapper.class);
		String tokenDb = "";
		try {
			tokenDb = mapperToken.getToken(username);
			sessionToken.commit();
		} catch (Exception e) {
			logger.error("Sql exception.", e);
			output.put("success", "0");
			output.put("msg", "db exception");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		} finally {
			sessionToken.close();
		}

		if (!token.equals(tokenDb)) {
			logger.warn("Requ token illegal.");
			output.put("success", "-1");
			output.put("msg", "request token illegal");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}

		// check mobile
		if (StringUtils.isNotEmpty(mobile) && 11 != mobile.length()) {
			logger.warn("Requ mobile illegal.");
			output.put("success", "0");
			output.put("msg", "request mobile illegal");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}

		SqlSession session = DBTools.getSession();
		EmsMapper mapper = session.getMapper(EmsMapper.class);
		List<EmsInfo> list = null;

		HashMap<String, String> map = new HashMap<>();
		map.put("orderSn", orderSn);
		map.put("mobile", mobile);

		try {
			list = mapper.ListEms(map);
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

		if (null == list || 0 == list.size()) {
			logger.error("No data.");
			output.put("success", "0");
			output.put("msg", "no data");
			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.flush();
			return;
		}

		output.put("success", "1");
		output.put("msg", "success");
		output.put("data", list);
		logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
		out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
		out.flush();
		return;
	}
}
package com.siasun.pcsweb.server.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siasun.pcsweb.mapper.UserMapper;
import com.siasun.pcsweb.tools.DBTools;
import com.siasun.pcsweb.tools.EmsExcelTool;

public class EmsDownloadExcelImpl implements UrlHandlerInter {

	private final static Logger logger = LoggerFactory.getLogger(EmsDownloadExcelImpl.class.getName());

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
//			output.put("success", "0");
//			output.put("msg", e.getMessage());
//			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
//			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write("服务器异常.");
			out.flush();
			return;
		}

		String requestDate = request.getParameter("date");
		String username = request.getParameter("username");
		String token = request.getParameter("token");
		logger.debug("username:{}, token:{}, date:{}.", username, token, requestDate);

		if (StringUtils.isEmpty(requestDate) || StringUtils.isEmpty(username) || StringUtils.isEmpty(token)) {
			logger.warn("Request some data is NULL, please check the Req URL.");
//			output.put("success", "0");
//			output.put("msg", "request data empty");
//			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
//			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write("请求数据为空, 请检查请求参数.");
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
//			output.put("success", "0");
//			output.put("msg", "db exception");
//			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
//			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write("数据库异常.");
			out.flush();
			return;
		} finally {
			sessionToken.close();
		}

		if (!token.equals(tokenDb)) {
			logger.warn("Requ token illegal.");
//			output.put("success", "-1");
//			output.put("msg", "request token illegal");
//			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
//			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write("请求TOKEN超时, 请重新登陆.");
			out.flush();
			return;
		}

		logger.info("Downloading EMS excel, requestDate: {}.", requestDate);

		String path = EmsExcelTool.generateEmsImportExcel(requestDate);

		if (StringUtils.isEmpty(path)) {
			logger.warn(String.format("Generate [%s] EMS excel failed", requestDate));
//			output.put("success", "0");
//			output.put("msg", String.format("RequestDate [%s] no ems data.", requestDate));
//			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
//			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write(String.format("%s 没有数据.", requestDate));
			out.flush();
			return;
		}

		// path是指欲下载的文件的路径。
		File file = new File(path);
		// 取得文件名。
		String filename = file.getName();
		// 取得文件的后缀名。
		// String ext = filename.substring(filename.lastIndexOf(".") +
		// 1).toUpperCase();

		// 以流的形式下载文件。
		InputStream fis;
		byte[] buffer = null;
		try {
			fis = new BufferedInputStream(new FileInputStream(path));
			buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
		} catch (Exception e) {
			logger.error("", e);
//			output.put("success", "0");
//			output.put("msg", e.getMessage());
//			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
//			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write("服务器异常.");
			out.flush();
			return;
		}

		// 清空response
		response.reset();
		// 设置response的Header
		response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
		response.addHeader("Content-Length", "" + file.length());
		OutputStream toClient = null;
		try {
			toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
		} catch (IOException e) {
			logger.error("", e);
//			output.put("success", "0");
//			output.put("msg", e.getMessage());
//			logger.debug(jsoncallback + "(" + JSON.toJSONString(output) + ")");
//			out.write(jsoncallback + "(" + JSON.toJSONString(output) + ")");
			out.write("服务器异常.");
			out.flush();
			return;
		}

		logger.info("Download EMS excel: {} success.", filename);// + ext);
	}
}
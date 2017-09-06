package com.siasun.pcsweb.tools;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siasun.pcsweb.base.Constants;

public class MsgKit extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(MsgKit.class.getName());
	private final String url = Constants.MESSAGE_URL;	
	//"http://221.180.145.214/sendmessage/auth/";
	//"http://221.180.145.207/sendmessage/auth/";
	public final static String METHOD_FK = "付款";
	public final static String METHOD_TK = "退款";
	public final static String METHOD_JJ = "寄件";

	private final String key = "babfe90be82e1d4925d0e05c3b4e06cf";
	private final String comid = "siasun";
	private final String appid = "xzsp";
	private final String ver = "01";
	private String templateno;// = "xzsp-fk#02";

	private String reserve;
	private String mobile;

	public MsgKit(String mobile, String orderSn, String payMoney, String mailId, String method) {

		switch (method) {
		case METHOD_FK:
			this.templateno = "xzsp-fk#02";
			DecimalFormat myformat = new DecimalFormat();
			myformat.applyPattern("##,###.00");
			String tradeValue = myformat.format(Integer.parseInt(payMoney) / 100.00) + "元"; // 参数金额
			this.reserve = orderSn + "," + tradeValue;
			logger.debug("[{}] param: {}", METHOD_FK, reserve);
			break;

		case METHOD_TK:
			this.templateno = "xzsp-tk#02";
			DecimalFormat myformat2 = new DecimalFormat();
			myformat2.applyPattern("##,###.00");
			String tradeValue2 = myformat2.format(Integer.parseInt(payMoney) / 100.00) + "元"; // 参数金额
			this.reserve = orderSn + "," + tradeValue2;
			logger.debug("[{}] param: {}", METHOD_TK, reserve);
			break;

		case METHOD_JJ:
			this.templateno = "xzsp-jj#02";
			this.reserve = orderSn + "," + mailId;
			logger.debug("[{}] param: {}", METHOD_JJ, reserve);
			break;
		default:
			break;
		}

		this.mobile = mobile;
	}

	public void run() {

		Map<String, String> map = new TreeMap<String, String>();
		map.put("appid", appid);
		map.put("comid", comid);
		map.put("phone", mobile);
		map.put("reserve", reserve);
		map.put("templateno", templateno);
		map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
		map.put("ver", ver);

		Set<String> keySet = map.keySet();

		Iterator<String> iter = keySet.iterator();

		StringBuilder sb = new StringBuilder();
		StringBuilder arg = new StringBuilder();
		while (iter.hasNext()) {

			String key = iter.next();
			sb.append(key);
			sb.append(map.get(key));

			arg.append(key);
			arg.append("=");
			arg.append(map.get(key));
			arg.append("&");
		}
		String sign = DigestUtils.md5Hex(sb.toString());
		logger.info("Data: " + sb.toString() + ", MD5: " + sign);

		String orgMsg = arg.append("sign=" + sign).toString();
		logger.info("OMsg: " + orgMsg);

		AesUtil aes = new AesUtil();
		String encryptData = null;
		try {
			encryptData = aes.Encrypt(orgMsg, key);
		} catch (Exception e1) {
			logger.error("Exception about AesUtil.java encrypt method.", e1);
			return;
		}

		if (null == encryptData) {
			logger.error("Unknown error[NULL] about AesUtil.java encrypt method.");
			return;
		}
		logger.info("Aes(OMsg): " + encryptData);

		// qs=comid=xxx&appid=xxx&arg=base64(aes(appid=xxx&comid=xxx&ver=xxx&phone=xxx&templateno=xxx&timestamp=xxx&reserve=xxx&sign=xxx))
		String qs = String.format("comid=%s&appid=%s&arg=%s", comid, appid, encryptData);
		logger.info("Qs: " + qs + ", length: " + qs.length());

		// send
		// HttpRequestHelper.sendPost(URL, qs);

		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		method.getParams().setContentCharset("UTF-8");
		method.setParameter("qs", qs);
		try {
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				logger.info("SendMessage: OK.");
			} else {
				logger.warn("HTTP Post failed, StatusCode: " + method.getStatusCode());
			}
		} catch (IOException e) {
			logger.warn("HTTP Post " + url + ", Exception.");
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
	}

	public static void main(String[] args) {

		MsgKit msg = new MsgKit("15604015619", "A000000003", "2000", "1137444832410", MsgKit.METHOD_FK);
		msg.run();
	}
}
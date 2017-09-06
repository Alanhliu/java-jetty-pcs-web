package com.siasun.pcsweb.server.handler;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlRouteHandler extends AbstractHandler {

	private final static Logger logger = LoggerFactory.getLogger(UrlRouteHandler.class.getName());
	private HashMap<String, UrlHandlerInter> handlers = new HashMap<String, UrlHandlerInter>();

	public UrlRouteHandler() {
		prepareHandlers();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {

		logger.debug("OPENED : {}", Thread.currentThread().getName());
		logger.debug("Request API -- [ {} ]", target.toLowerCase());
		if (handlers.containsKey(target.toLowerCase())) {
			handlers.get(target.toLowerCase()).UrlHandler(request, response);
		}
		baseRequest.setHandled(true);
		logger.debug("CLOSED : {}", Thread.currentThread().getName());
	}

	private void prepareHandlers() {
		synchronized (handlers) {
			handlers.put("/pcs-web/orderquery", new OrderQueryImpl());
			handlers.put("/pcs-web/userlogin", new UserLoginImpl());
			handlers.put("/pcs-web/emsquery", new EmsQueryImpl());
			handlers.put("/pcs-web/emsdownloadexcel", new EmsDownloadExcelImpl());
			handlers.put("/pcs-web/emsuploadexcel", new EmsUploadExcelImpl());
		}
	}
}
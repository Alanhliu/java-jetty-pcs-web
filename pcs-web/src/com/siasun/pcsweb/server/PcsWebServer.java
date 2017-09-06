package com.siasun.pcsweb.server;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siasun.pcsweb.base.Constants;
import com.siasun.pcsweb.server.handler.UrlRouteHandler;

public class PcsWebServer {

	private final static Logger logger = LoggerFactory.getLogger(PcsWebServer.class.getName());

	public static void main(String[] args) {

		try {
			InetSocketAddress addr = new InetSocketAddress(Constants.IP, Constants.PORT);
			Server server = new Server(addr);
			ContextHandler context = new ContextHandler();

			context.setContextPath("/");
			context.setResourceBase(".");
			context.setClassLoader(Thread.currentThread().getContextClassLoader());
			server.setHandler(context);
			context.setHandler(new UrlRouteHandler());

			server.start();
			server.join();

		} catch (Exception e) {
			logger.error("Pcs-web started failed.", e);
		}
	}
}
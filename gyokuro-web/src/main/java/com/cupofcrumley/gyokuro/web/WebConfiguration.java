package com.cupofcrumley.gyokuro.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.cupofcrumley.gyokuro.core.CoreAppConfig;
import com.cupofcrumley.gyokuro.core.config.EnableConfig;
import com.yammer.metrics.jetty.InstrumentedBlockingChannelConnector;
import com.yammer.metrics.jetty.InstrumentedQueuedThreadPool;

@Configuration
@EnableConfig( JettyConfig.class )
public class WebConfiguration {
	Logger logger = LoggerFactory.getLogger(WebConfiguration.class);
	
	public Server jettyServer(CoreAppConfig coreAppConfig, JettyConfig config) throws Exception {
		Server server = new Server();

		logger.info("Initializing jetty server with config name: {} and server name: {}", coreAppConfig.getConfigName());
		
		server.addConnector(new InstrumentedBlockingChannelConnector(8080));

		QueuedThreadPool threadPool = new InstrumentedQueuedThreadPool();
		threadPool.setMinThreads(2);
		threadPool.setMaxThreads(10);
		server.setThreadPool(threadPool);

		server.setStopAtShutdown(true);
		server.setGracefulShutdown(5000);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			logger.error("Unable to start server, shutting down", e);
			server.stop();
		}
		
		return server;
	}
}

package com.psl.fabric.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ConfigServletContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		NetworkConfig config = new NetworkConfig();
		config.initialConfig(configPath);
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}

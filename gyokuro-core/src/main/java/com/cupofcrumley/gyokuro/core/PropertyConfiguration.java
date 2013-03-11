package com.cupofcrumley.gyokuro.core;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(name = "core.config", value = { "${config.override:classpath:application.override.properties}" })
public class PropertyConfiguration {
	private static final Logger log = LoggerFactory.getLogger(PropertyConfiguration.class);

	@Autowired
	private Environment env;

	@PostConstruct
	public void init() {
		log.info("Loaded property configuration from location '{}'.", env.getProperty("config.override"));
	}
}
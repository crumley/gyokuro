package com.cupofcrumley.gyokuro.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class GyokuroApp {
	Logger log = LoggerFactory.getLogger(GyokuroApp.class);

	private AnnotationConfigApplicationContext context;

	public void start(String[] args) {
		Class<?> conf = getSpringConfiguration();
		context = new AnnotationConfigApplicationContext();
		context.register(conf);
		context.refresh();
	}

	public abstract Class<?> getSpringConfiguration();
}
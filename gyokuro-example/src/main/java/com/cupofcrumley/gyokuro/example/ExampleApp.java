package com.cupofcrumley.gyokuro.example;

import com.cupofcrumley.gyokuro.core.app.GyokuroApp;

public class ExampleApp extends GyokuroApp {
	@Override
	public Class<?> getSpringConfigurationClass() {
		return ExampleConfiguration.class;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ExampleApp().start(args);
	}
}
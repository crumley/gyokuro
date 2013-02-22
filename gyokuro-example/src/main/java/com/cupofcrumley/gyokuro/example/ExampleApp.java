package com.cupofcrumley.gyokuro.example;

import com.cupofcrumley.gyokuro.core.cli.GyokuroApp;

public class ExampleApp extends GyokuroApp {

	public ExampleApp() {
	}
	
	@Override
	public Class<?> getSpringConfiguration() {
		return ExampleConfiguration.class;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ExampleApp().start(args);
	}
}

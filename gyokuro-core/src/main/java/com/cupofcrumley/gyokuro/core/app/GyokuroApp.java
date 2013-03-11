package com.cupofcrumley.gyokuro.core.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public abstract class GyokuroApp<T> {
	Logger log = LoggerFactory.getLogger(GyokuroApp.class);

	public void start(String... args) {
		// Pre-parse arguments so we can load configuration before
		// spring starts. Argument parsing will happen again
		// in AppService after all the AppCommands have the 
		// opportunity to register themselves.
		JCommander jc = new JCommander();
		AppOptions appOptions = new AppOptions();
		jc.addObject(appOptions);
		jc.setAcceptUnknownOptions(true);
		jc.parseWithoutValidation(args);

		AnnotationConfigApplicationContext context = createSpringApplicationContext(appOptions.getConfigLocations());

		AppService appService = context.getBean(AppService.class);
		appService.start(this, jc, args);
	}

	protected AnnotationConfigApplicationContext createSpringApplicationContext(List<String> configLocations) {
		// TODO Create lifecycle abstraction so spring context can be closed on app shutdown. 
		AnnotationConfigApplicationContext springContext = new AnnotationConfigApplicationContext();
		for (String location : configLocations) {
			log.info("Adding config location to spring environment: {}", location);
			ResourcePropertySource source;
			try {
				source = new ResourcePropertySource(location);
			} catch (IOException e) {
				throw new IllegalArgumentException("Unable to load configuration resource: " + location, e);
			}
			springContext.getEnvironment().getPropertySources().addLast(source);
		}

		Class<T> conf = getSpringConfigurationClass();
		springContext.register(conf);
		springContext.refresh();

		return springContext;
	}

	public abstract Class<T> getSpringConfigurationClass();

	@Override
	public String toString() {
		return "[Application " + getClass().getSimpleName() + "]";
	}

	public static class AppOptions {
		@Parameter(names = { "-c", "-config", "--config" }, description = "Location of property files to load for application configuration. Spring resource prefixes supported (default is 'file:')", arity = 1)
		private List<String> configLocations = new ArrayList<String>();

		@Parameter(description = "Extended Arguments", hidden = true)
		private List<String> arguments = new ArrayList<String>();

		public List<String> getConfigLocations() {
			return configLocations;
		}

		public void setConfigLocations(List<String> configLocations) {
			this.configLocations = configLocations;
		}
	}
}
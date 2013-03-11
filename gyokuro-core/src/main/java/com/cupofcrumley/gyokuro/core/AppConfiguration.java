package com.cupofcrumley.gyokuro.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.cupofcrumley.gyokuro.config.Config;
import com.cupofcrumley.gyokuro.core.app.AppCommand;
import com.cupofcrumley.gyokuro.core.app.AppService;
import com.cupofcrumley.gyokuro.core.app.commands.ConfigCommand;
import com.cupofcrumley.gyokuro.core.config.EnableConfig;

@Configuration
@EnableConfig(CoreAppConfig.class)
public class AppConfiguration {
	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public ConfigCommand configCommand(Config[] configs) {
		return new ConfigCommand(configs);
	}

	@Bean
	public AppService commandRegistery(AppCommand<?>[] commands) {
		return new AppService(commands);
	}
}
package com.cupofcrumley.gyokuro.core.config;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.google.common.collect.ImmutableSet;

/**
 * 
 * @author Ryan Crumley
 */
@Configuration
@PropertySource(name = "core.config", value = { "${config.override:classpath:application.override.properties}" })
@EnableConfig(CoreConfig.class)
public class PropertyConfiguration {
	private static final Logger log = LoggerFactory.getLogger(PropertyConfiguration.class);

	@Autowired
	private Environment env;

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public BeanValidationPostProcessor beanValidationPostProcessor(Validator validator) {
		BeanValidationPostProcessor postProcessor = new BeanValidationPostProcessor();
		postProcessor.setValidator(validator);
		// Validate after initialization so we can validate objects created by FactoryBean.
		postProcessor.setAfterInitialization(true);
		return postProcessor;
	}

	@Bean
	public ConversionServiceFactoryBean conversionService() {
		ConversionServiceFactoryBean factoryBean = new ConversionServiceFactoryBean();
		factoryBean.setConverters(ImmutableSet.of(new StringToClass()));
		return factoryBean;
	}

	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
		MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
		postProcessor.setValidator(validator);
		return postProcessor;
	}

	@PostConstruct
	public void init() {
		log.info("Loaded property configuration from location '{}'.", env.getProperty("config.override"));
	}
}
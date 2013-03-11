package com.cupofcrumley.gyokuro.core;

import javax.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class ValidationConfiguration {
	@Bean
	public BeanValidationPostProcessor beanValidationPostProcessor(Validator validator) {
		BeanValidationPostProcessor postProcessor = new BeanValidationPostProcessor();
		postProcessor.setValidator(validator);
		// Validate after initialization so we can validate objects created by FactoryBean.
		postProcessor.setAfterInitialization(true);
		return postProcessor;
	}

	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
		MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
		postProcessor.setValidator(validator);
		return postProcessor;
	}
}
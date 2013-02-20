package com.cupofcrumley.gyokuro.core.config;

import javax.validation.Validator;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.cupofcrumley.gyokuro.config.Config;
import com.cupofcrumley.gyokuro.config.ConfigImpl;
import com.cupofcrumley.gyokuro.config.ConfigResolver;

public class ConfigFactoryBean<T extends Config> implements FactoryBean<T> {
	private Class<T> clazz;
	private ConfigResolver configResolver;
	private Validator validator;

	@Autowired
	public ConfigFactoryBean(Class<T> clazz, Environment env) {
		this.clazz = clazz;
		this.configResolver = new SpringConfigResolver(env);
	}

	@Autowired(required = false)
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	@Override
	public T getObject() throws Exception {
		return ConfigImpl.newInstance(clazz, configResolver, validator);
	}

	@Override
	public Class<?> getObjectType() {
		return clazz;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
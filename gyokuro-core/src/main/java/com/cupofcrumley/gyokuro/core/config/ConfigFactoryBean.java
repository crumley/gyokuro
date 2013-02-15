package com.cupofcrumley.gyokuro.core.config;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.env.Environment;

public class ConfigFactoryBean<T extends Config> implements FactoryBean<T> {
	private Environment env;
	private Class<T> clazz;
	private ConfigResolver configResolver;

	public ConfigFactoryBean(Class<T> clazz, Environment env) {
		this.clazz = clazz;
		this.env = env;
		this.configResolver = new SpringConfigResolver(env);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		ConfigInvocationHandler<T> handler = new ConfigInvocationHandler<T>(clazz, configResolver);
		T configInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
		return configInstance;
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
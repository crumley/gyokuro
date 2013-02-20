package com.cupofcrumley.gyokuro.core.config;

import org.springframework.core.env.Environment;

import com.cupofcrumley.gyokuro.config.ConfigResolver;

public class SpringConfigResolver implements ConfigResolver {

	private Environment env;

	public SpringConfigResolver(Environment env) {
		this.env = env;
	}

	@Override
	public <T> T getConfigValue(String key, Class<T> returnType, Object defaultValue, Object... args) {
		if (returnType == Class.class) {
			String className = env.getProperty(key);
			if (className == null) {
				return returnType.cast(defaultValue);
			}

			Class<?> ret;
			try {
				ret = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Class not found: " + className, e);
			}
			return returnType.cast(ret);
		}
		return env.getProperty(key, returnType, returnType.cast(defaultValue));
	}
}

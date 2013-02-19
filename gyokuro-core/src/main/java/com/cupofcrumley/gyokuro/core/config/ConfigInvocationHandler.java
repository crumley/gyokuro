package com.cupofcrumley.gyokuro.core.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cupofcrumley.gyokuro.core.config.Config.DefaultBooleanValue;
import com.cupofcrumley.gyokuro.core.config.Config.DefaultClassValue;
import com.cupofcrumley.gyokuro.core.config.Config.DefaultDoubleValue;
import com.cupofcrumley.gyokuro.core.config.Config.DefaultIntegerValue;
import com.cupofcrumley.gyokuro.core.config.Config.DefaultStringValue;
import com.cupofcrumley.gyokuro.core.config.Config.Key;
import com.google.common.base.CaseFormat;

public class ConfigInvocationHandler<T extends Config> implements InvocationHandler {
	Logger logger = LoggerFactory.getLogger(ConfigInvocationHandler.class);

	private final Class<T> clazz;
	private final ConfigResolver resolver;

	public ConfigInvocationHandler(Class<T> clazz, ConfigResolver resolver) {
		super();
		this.clazz = clazz;
		this.resolver = resolver;
		validateMethods();
	}

	private void validateMethods() {
		Method[] methods = this.clazz.getMethods();
		for (Method method : methods) {
			if (method.getReturnType().isPrimitive()) {
				throw new IllegalStateException("Primitive return types are not supported: " + this.clazz.getName() + "." + method.getName());
			}
			Object defaultValue = getDefault(method);
			if (defaultValue != null) {
				if (!method.getReturnType().isAssignableFrom(defaultValue.getClass())) {
					throw new IllegalStateException("Default value provided for: " + this.clazz.getName() + "." + method.getName() + " does not match the return type.");
				}
			}
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (clazz == method.getDeclaringClass()) {
			return resolveConfigProperty(method, args);
		}

		return method.invoke(this, args);
	}

	private Object resolveConfigProperty(Method method, Object[] args) {
		Class<?> returnType = method.getReturnType();
		Object defaultValue = getDefault(method);
		String key = getKey(method);
		Object value = resolver.getConfigValue(key, returnType, defaultValue, args);
		logger.info("Resolved property: '{}' to value: '{}' (default: '{}')", key, value, defaultValue);
		return value;
	}

	private String getKey(Method method) {
		String prefixValue = "";
		KeyPrefix prefix = method.getDeclaringClass().getAnnotation(KeyPrefix.class);
		if (prefix != null) {
			prefixValue = prefix.value();
		}

		Key key = method.getAnnotation(Config.Key.class);
		if (key != null) {
			return prefixValue + key.value();
		}

		String value = method.getName();
		if (value.startsWith("get")) {
			value = value.substring(3).intern();
			value = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, value);
		}

		return prefixValue + value;
	}

	private Object getDefault(Method method) {
		DefaultStringValue stringAnnotation = method.getAnnotation(DefaultStringValue.class);
		if (stringAnnotation != null) {
			return stringAnnotation.value();
		}
		DefaultBooleanValue booleanAnnotation = method.getAnnotation(DefaultBooleanValue.class);
		if (booleanAnnotation != null) {
			return booleanAnnotation.value();
		}
		DefaultIntegerValue integerAnnotation = method.getAnnotation(DefaultIntegerValue.class);
		if (integerAnnotation != null) {
			return integerAnnotation.value();
		}
		DefaultDoubleValue doubleAnnotation = method.getAnnotation(DefaultDoubleValue.class);
		if (doubleAnnotation != null) {
			return doubleAnnotation.value();
		}
		DefaultClassValue classAnnotation = method.getAnnotation(DefaultClassValue.class);
		if (classAnnotation != null) {
			return classAnnotation.value();
		}

		return null;
	}

	@Override
	public String toString() {
		return "[Configuration: " + clazz.getName() + "]";
	}
}

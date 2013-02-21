package com.cupofcrumley.gyokuro.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import javax.validation.Validator;

import org.hibernate.validator.method.MethodConstraintViolation;
import org.hibernate.validator.method.MethodConstraintViolationException;
import org.hibernate.validator.method.MethodValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cupofcrumley.gyokuro.config.Config.DefaultBooleanValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultClassValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultDoubleValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultIntegerValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultStringValue;
import com.cupofcrumley.gyokuro.config.Config.Key;
import com.google.common.base.CaseFormat;

public class ConfigImpl<T extends Config> implements InvocationHandler {
	Logger logger = LoggerFactory.getLogger(ConfigImpl.class);

	public static <T extends Config> T newInstance(Class<T> clazz, ConfigResolver configResolver) {
		return newInstance(clazz, configResolver, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Config> T newInstance(Class<T> clazz, ConfigResolver configResolver, Validator validator) {
		ConfigImpl<T> handler = new ConfigImpl<T>(clazz, configResolver);
		handler.setValidator(validator);
		T configInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
		if (validator != null) {
			configInstance.validate();
		}
		return configInstance;
	}

	private final Class<T> clazz;
	private final ConfigResolver resolver;

	private MethodValidator validator;

	public ConfigImpl(Class<T> clazz, ConfigResolver resolver) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null.");
		}
		if (resolver == null) {
			throw new IllegalArgumentException("resolver must not be null.");
		}

		this.clazz = clazz;
		this.resolver = resolver;

		validateDefaultValues(null);
	}

	private void validateDefaultValues(Object proxy) {
		Method[] methods = this.clazz.getMethods();
		for (Method method : methods) {
			if ("validate".equals(method.getName())) {
				continue;
			}

			if (method.getReturnType().isPrimitive()) {
				throw new IllegalStateException("Primitive return types are not supported: " + this.clazz.getName() + "." + method.getName());
			}
			if (method.getGenericParameterTypes().length > 0) {
				throw new IllegalArgumentException("Config methods must not accept parameters.");
			}

			Object defaultValue = getDefault(method);
			validateReturnValue(this, method, defaultValue);
			if (defaultValue != null) {
				if (!method.getReturnType().isAssignableFrom(defaultValue.getClass())) {
					throw new IllegalStateException("Default value provided for: " + this.clazz.getName() + "." + method.getName() + " does not match the return type.");
				}
			}

			if (proxy != null) {
				try {
					Object ret = method.invoke(proxy);
					validateReturnValue(proxy, method, ret);
				} catch (Exception e) {
					throw new IllegalStateException("Exception evaluating method for validation: " + method, e);
				}
			}
		}
	}

	private void validate(Object proxy) {
		if (validator == null) {
			throw new IllegalStateException("Can't validate without a validator!");
		}
		validateDefaultValues(proxy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if (clazz == method.getDeclaringClass()) {
			validateParameters(proxy, method, args);
			Object ret = resolveConfigProperty(method, args);
			validateReturnValue(proxy, method, ret);
			return ret;
		} else if (Config.class == method.getDeclaringClass()) {
			if ("validate".equals(method.getName())) {
				validate(proxy);
			}
		}

		return method.invoke(this, args);
	}

	private void validateParameters(Object proxy, Method method, Object[] args) {
		if (validator != null) {
			Set<MethodConstraintViolation<Object>> result = validator.validateAllParameters(proxy, method, args);
			if (!result.isEmpty()) {
				throw new MethodConstraintViolationException(result);
			}
		}
	}

	private void validateReturnValue(Object proxy, Method method, Object ret) {
		if (validator != null) {
			Set<MethodConstraintViolation<Object>> result = validator.validateReturnValue(proxy, method, ret);
			if (!result.isEmpty()) {
				throw new MethodConstraintViolationException(result);
			}
		}
	}

	private Object resolveConfigProperty(Method method, Object[] args) {
		Class<?> returnType = method.getReturnType();
		Object defaultValue = getDefault(method);
		String key = getKey(method);
		Object value = resolver.getConfigValue(key, returnType, defaultValue, args);
		logger.info("Resolved property: '{}' to value: '{}' (default: '{}')", key, value, defaultValue);
		return value;
	}

	public void setValidator(Validator validator) {
		this.validator = validator == null ? null : validator.unwrap(MethodValidator.class);
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

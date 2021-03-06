package com.cupofcrumley.gyokuro.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Validator;

import org.hibernate.validator.method.MethodConstraintViolation;
import org.hibernate.validator.method.MethodConstraintViolationException;
import org.hibernate.validator.method.MethodValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cupofcrumley.gyokuro.config.Config.ConfigSummary;
import com.cupofcrumley.gyokuro.config.Config.DefaultBooleanValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultClassValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultDoubleValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultIntegerValue;
import com.cupofcrumley.gyokuro.config.Config.DefaultStringValue;
import com.cupofcrumley.gyokuro.config.Config.Description;
import com.cupofcrumley.gyokuro.config.Config.Key;

public class ConfigImpl<T extends Config> implements InvocationHandler {
	private Logger log = LoggerFactory.getLogger(ConfigImpl.class);

	public static <T extends Config> T newInstance(Class<T> clazz, ConfigResolver configResolver) {
		return newInstance(clazz, configResolver, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Config> T newInstance(Class<T> clazz, ConfigResolver configResolver, Validator validator) {
		ConfigImpl<T> handler = new ConfigImpl<T>(clazz, configResolver);
		handler.setValidator(validator);
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
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

		validateClass(clazz);

		this.clazz = clazz;
		this.resolver = resolver;
	}

	public List<ConfigSummary> getConfigOptions() {
		List<ConfigSummary> options = new ArrayList<ConfigSummary>();

		Method[] methods = clazz.getMethods();
		for (final Method method : methods) {
			if (method.getDeclaringClass() == clazz) {
				String key = getKey(method);
				Object defaultValue = getDefault(method);
				String description = getDescription(method);
				options.add(new ConfigOptionImpl(method, key, description, defaultValue));
			}
		}

		return options;
	}

	private void validateClass(Class<T> clazz) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getDeclaringClass() == Config.class) {
				continue;
			}

			if (method.getReturnType().isPrimitive()) {
				throw new IllegalStateException("Primitive return types are not supported: " + clazz.getName() + "." + method.getName());
			}
			if (method.getGenericParameterTypes().length > 0) {
				throw new IllegalArgumentException("Config methods must not accept parameters.");
			}

			Object defaultValue = getDefault(method);
			if (defaultValue != null) {
				if (!method.getReturnType().isAssignableFrom(defaultValue.getClass())) {
					throw new IllegalStateException("Default value provided for: " + clazz.getName() + "." + method.getName() + " does not match the return type.");
				}
			}
		}
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
				validateValues(proxy);
				return null;
			} else if ("getConfigSummary".equals(method.getName())) {
				return getConfigOptions();
			}
		}

		return method.invoke(this, args);
	}

	private void validateValues(Object proxy) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (validator == null) {
			throw new IllegalStateException("Can't validate without a validator!");
		}

		Method[] methods = this.clazz.getMethods();
		for (Method method : methods) {
			// Don't validate non config methods.
			if (method.getDeclaringClass() == Config.class) {
				continue;
			}

			// Ensure the default value is valid!
			Object defaultValue = getDefault(method);
			if (defaultValue != null) {
				validateReturnValue(proxy, method, defaultValue);
			}

			// By invoking the method we verify the method can execute and the return value is valid.
			method.invoke(proxy);
		}
	}

	private void validateParameters(Object proxy, Method method, Object[] args) {
		if (validator != null) {
			Set<MethodConstraintViolation<Object>> result = validator.validateAllParameters(proxy, method, args);
			if (!result.isEmpty()) {
				// TODO exception output is more complicated than we need. Simplify it.
				throw new MethodConstraintViolationException(result);
			}
		}
	}

	private void validateReturnValue(Object proxy, Method method, Object ret) {
		if (validator != null) {
			Set<MethodConstraintViolation<Object>> result = validator.validateReturnValue(proxy, method, ret);
			if (!result.isEmpty()) {
				// TODO exception output is more complicated than we need. Simplify it. 
				throw new MethodConstraintViolationException(result);
			}
		}
	}

	private Object resolveConfigProperty(Method method, Object[] args) {
		Class<?> returnType = method.getReturnType();
		Object defaultValue = getDefault(method);
		String key = getKey(method);
		Object value = resolver.getConfigValue(key, returnType, defaultValue, args);
		log.info("Resolved property: '{}' to value: '{}' (default: '{}')", key, value, defaultValue);
		return value;
	}

	private String getDescription(Method method) {
		Description description = method.getAnnotation(Config.Description.class);
		return description == null ? null : description.value();
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
			value = firstCharToLower(value);
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

	public void setValidator(Validator validator) {
		this.validator = validator == null ? null : validator.unwrap(MethodValidator.class);
	}

	@Override
	public String toString() {
		return "[Configuration: " + clazz.getName() + "]";
	}

	private static String firstCharToLower(String word) {
		return (word.isEmpty()) ? word : new StringBuilder(word.length()).append(Character.toLowerCase(word.charAt(0))).append(word.substring(1)).toString();
	}

	private final class ConfigOptionImpl implements ConfigSummary {
		private final Method method;
		private final String key;
		private final String description;
		private final Object defaultValue;

		private ConfigOptionImpl(Method method, String key, String description, Object defaultValue) {
			this.method = method;
			this.key = key;
			this.description = description;
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean isRequired() {
			// TODO Enhance to detect if default value satisfies validator
			return defaultValue == null;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getDefault() {
			return defaultValue == null ? null : defaultValue.toString();
		}

		@Override
		public String getValue() {
			Object value = resolveConfigProperty(method, new Object[0]);
			return value == null ? "" : value.toString();
		}
	}
}
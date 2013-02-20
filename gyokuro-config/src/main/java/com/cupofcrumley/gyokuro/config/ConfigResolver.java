package com.cupofcrumley.gyokuro.config;

public interface ConfigResolver {
	public <T> T getConfigValue(String key, Class<T> returnType, Object defaultValue, Object... args);
}
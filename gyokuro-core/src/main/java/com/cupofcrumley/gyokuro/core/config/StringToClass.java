package com.cupofcrumley.gyokuro.core.config;

import org.springframework.core.convert.converter.Converter;

public class StringToClass implements Converter<String, Class<?>> {

	@Override
	public Class<?> convert(String source) {
		Class<?> ret;
		try {
			ret = Class.forName(source);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not convert string to class. Not found: " + source, e);
		}
		return ret;
	}

}

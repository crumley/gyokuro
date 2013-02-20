package com.cupofcrumley.gyokuro.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Config {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Key {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Description {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Documented
	public @interface DefaultStringValue {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Documented
	public @interface DefaultBooleanValue {
		boolean value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Documented
	public @interface DefaultIntegerValue {
		int value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Documented
	public @interface DefaultDoubleValue {
		double value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Documented
	public @interface DefaultClassValue {
		Class<?> value();
	}

	public void validate();
}
package com.cupofcrumley.gyokuro.core;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

import com.cupofcrumley.gyokuro.core.config.PropertyConfig;

public class PropertyTest {
	@Configuration
	@Import({ PropertyConfig.class })
	static class ContextConfiguration {
		@Autowired
		public Environment env;

		@Bean(name = "propertyValue")
		public String propertyValue() {
			return env.getProperty("testProperty");
		}
	}

	@Test
	public void testNoOverride() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(ContextConfiguration.class);
		context.refresh();

		String envValue = context.getEnvironment().getProperty("testProperty");
		Assert.assertNull(envValue);

		Object beanValue = context.getBean("propertyValue");
		Assert.assertNull(beanValue);

		context.close();
	}

	@Test
	public void testOverride() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("config.override", "classpath:testproperties.properties");

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		ConfigurableEnvironment env = context.getEnvironment();
		env.getPropertySources().addFirst(new MapPropertySource("mps", map));

		context.register(ContextConfiguration.class);
		context.refresh();

		String envValue = context.getEnvironment().getProperty("testProperty");
		Assert.assertEquals("This is a test", envValue);

		Object beanValue = context.getBean("propertyValue");
		Assert.assertEquals("This is a test", beanValue);

		context.close();
	}
}
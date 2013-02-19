package com.cupofcrumley.gyokuro.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.env.MockPropertySource;

import com.cupofcrumley.gyokuro.core.TestSpringConfig.ConfigTestConfiguration.GenericTestConfig;
import com.cupofcrumley.gyokuro.core.TestSpringConfig.ConfigTestConfiguration.PrefixTestConfig;
import com.cupofcrumley.gyokuro.core.TestSpringConfig.ConfigTestConfiguration.TypeTestConfig;
import com.cupofcrumley.gyokuro.core.config.Config;
import com.cupofcrumley.gyokuro.core.config.EnableConfig;
import com.cupofcrumley.gyokuro.core.config.KeyPrefix;
import com.cupofcrumley.gyokuro.core.config.PropertyConfiguration;

public class TestSpringConfig {
	private MockPropertySource ps;
	private AnnotationConfigApplicationContext context;

	@Before
	public void setUp() {
		ps = new MockPropertySource();
		ps.withProperty("integerValue", "1");
		ps.withProperty("doubleValue", "1.234");
		ps.withProperty("stringValue", "abc123");
		ps.withProperty("classValue", Config.class.getName());
		ps.withProperty("com.cupofcrumley.stringValue", "abcdef");
		context = createAppContext(ConfigTestConfiguration.class);
	}

	@After
	public void tearDown() {
		context.close();
	}

	@Configuration
	@EnableConfig({ GenericTestConfig.class, TypeTestConfig.class, PrefixTestConfig.class })
	@Import({ PropertyConfiguration.class })
	static class ConfigTestConfiguration {
		static interface GenericTestConfig extends Config {
			String getMissingKey();

			@Key("integerValue")
			Integer getIntegerValueWithDifferentKey();

			@DefaultIntegerValue(50)
			Integer getDefaultIntegerValueTest();

			@DefaultStringValue("aabbcc")
			String getDefaultStringValueTest();

			@DefaultDoubleValue(1.234)
			Double getDefaultDoubleValueTest();

			@DefaultClassValue(TestSpringConfig.class)
			Class<?> getDefaultClassValueTest();
		}

		static interface TypeTestConfig extends Config {
			Integer getIntegerValue();

			Double getDoubleValue();

			String getStringValue();

			Class<?> getClassValue();
		}

		@KeyPrefix("com.cupofcrumley.")
		static interface PrefixTestConfig extends Config {
			@Description("Should be missing due to prefix.")
			Integer getIntegerValue();

			String getStringValue();

			@Key("stringValue")
			@Description("Ensure @KeyPrefix is prefixed to @Key value. This should be the same as getStringValue()")
			String getStringValueWithDifferentKey();
		}
	}

	@Test
	public void testGeneral() {
		GenericTestConfig config = context.getBean(GenericTestConfig.class);
		Assert.assertNotNull(config);
		Assert.assertNull(config.getMissingKey());
		Assert.assertEquals(Integer.valueOf(1), config.getIntegerValueWithDifferentKey());
		Assert.assertEquals(Integer.valueOf(50), config.getDefaultIntegerValueTest());
		Assert.assertEquals("aabbcc", config.getDefaultStringValueTest());
		Assert.assertEquals(Double.valueOf(1.234), config.getDefaultDoubleValueTest());
		Assert.assertEquals(TestSpringConfig.class, config.getDefaultClassValueTest());
	}

	@Test
	public void testTypes() {
		TypeTestConfig config = context.getBean(TypeTestConfig.class);
		Assert.assertNotNull(config);
		Assert.assertEquals(Integer.valueOf(1), config.getIntegerValue());
		Assert.assertEquals(Double.valueOf(1.234), config.getDoubleValue());
		Assert.assertEquals("abc123", config.getStringValue());
		Assert.assertEquals(Config.class, config.getClassValue());
	}

	@Test
	public void testPrefixes() {
		PrefixTestConfig config = context.getBean(PrefixTestConfig.class);
		Assert.assertNotNull(config);
		Assert.assertNull(config.getIntegerValue());
		Assert.assertEquals("abcdef", config.getStringValue());
		Assert.assertEquals("abcdef", config.getStringValueWithDifferentKey());
	}

	private AnnotationConfigApplicationContext createAppContext(Class<?> configurationClass) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().getPropertySources().addFirst(ps);
		context.register(configurationClass);
		context.refresh();
		return context;
	}
}
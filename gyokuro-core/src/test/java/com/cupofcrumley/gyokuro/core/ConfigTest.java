package com.cupofcrumley.gyokuro.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.env.MockPropertySource;

import com.cupofcrumley.gyokuro.core.ConfigTest.GeneralConfiguration.GenericTestConfig;
import com.cupofcrumley.gyokuro.core.ConfigTest.TypeTestConfiguration.TypeTestConfig;
import com.cupofcrumley.gyokuro.core.config.Config;
import com.cupofcrumley.gyokuro.core.config.EnableConfig;
import com.cupofcrumley.gyokuro.core.config.PropertyConfiguration;

public class ConfigTest {
	private MockPropertySource ps;

	@Before
	public void setUp() {
		ps = new MockPropertySource();
		ps.withProperty("integerValue", "1");
		ps.withProperty("doubleValue", "1.234");
		ps.withProperty("stringValue", "abc123");
		ps.withProperty("classValue", Config.class.getName());
	}

	@Configuration
	@EnableConfig(GenericTestConfig.class)
	@Import({ PropertyConfiguration.class })
	static class GeneralConfiguration {
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

			@DefaultClassValue(ConfigTest.class)
			Class<?> getDefaultClassValueTest();
		}
	}

	@Test
	public void testGeneral() {
		AnnotationConfigApplicationContext context = createAppContext(GeneralConfiguration.class);

		GenericTestConfig config = context.getBean(GenericTestConfig.class);
		Assert.assertNotNull(config);
		Assert.assertNull(config.getMissingKey());
		Assert.assertEquals(Integer.valueOf(1), config.getIntegerValueWithDifferentKey());
		Assert.assertEquals(Integer.valueOf(50), config.getDefaultIntegerValueTest());
		Assert.assertEquals("aabbcc", config.getDefaultStringValueTest());
		Assert.assertEquals(Double.valueOf(1.234), config.getDefaultDoubleValueTest());
		Assert.assertEquals(ConfigTest.class, config.getDefaultClassValueTest());

		context.close();
	}

	@Configuration
	@EnableConfig(TypeTestConfig.class)
	@Import({ PropertyConfiguration.class })
	static class TypeTestConfiguration {
		static interface TypeTestConfig extends Config {
			Integer getIntegerValue();

			Double getDoubleValue();

			String getStringValue();

			Class<?> getClassValue();
		}
	}

	@Test
	public void testTypes() {
		AnnotationConfigApplicationContext context = createAppContext(TypeTestConfiguration.class);

		TypeTestConfig config = context.getBean(TypeTestConfig.class);
		Assert.assertNotNull(config);
		Assert.assertEquals(config.getIntegerValue(), Integer.valueOf(1));
		Assert.assertEquals(config.getDoubleValue(), Double.valueOf(1.234));
		Assert.assertEquals(config.getStringValue(), "abc123");
		Assert.assertEquals(config.getClassValue(), Config.class);

		context.close();
	}

	private AnnotationConfigApplicationContext createAppContext(Class<?> configurationClass) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().getPropertySources().addFirst(ps);
		context.register(configurationClass);
		context.refresh();
		return context;
	}
}
package com.cupofcrumley.gyokuro.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestConfigImpl {
	private ConfigResolver configResolver;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		configResolver = Mockito.mock(ConfigResolver.class);
		Mockito.when(configResolver.getConfigValue(Mockito.eq("integerValue"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn(1);
		Mockito.when(configResolver.getConfigValue(Mockito.eq("doubleValue"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn(1.234);
		Mockito.when(configResolver.getConfigValue(Mockito.eq("stringValue"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn("abc123");
		Mockito.when(configResolver.getConfigValue(Mockito.eq("classValue"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn(Config.class);
		Mockito.when(configResolver.getConfigValue(Mockito.eq("com.cupofcrumley.stringValue"), Mockito.any(Class.class), Mockito.anyVararg(), Mockito.any())).thenReturn("abcdef");
		Mockito.when(configResolver.getConfigValue(Mockito.eq("defaultIntegerValueTest"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn(50);
		Mockito.when(configResolver.getConfigValue(Mockito.eq("defaultStringValueTest"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn("aabbcc");
		Mockito.when(configResolver.getConfigValue(Mockito.eq("defaultDoubleValueTest"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn(1.234);
		Mockito.when(configResolver.getConfigValue(Mockito.eq("defaultClassValueTest"), Mockito.any(Class.class), Mockito.any(), Mockito.anyVararg())).thenReturn(TestConfigImpl.class);
	}

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

		@DefaultClassValue(TestConfigImpl.class)
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

	static interface ConfigWithPrimitive extends Config {
		int getPrimitive();
	}

	static interface ConfigWithBadDefault extends Config {
		@DefaultBooleanValue(true)
		Integer getIntegerValue();
	}

	static interface ConfigWithArguments extends Config {
		Integer getIntegerValue(int arg1);
	}

	@Test
	public void testGeneral() {
		GenericTestConfig config = ConfigImpl.newInstance(GenericTestConfig.class, configResolver);
		Assert.assertNotNull(config);
		Assert.assertNull(config.getMissingKey());
		Assert.assertEquals(Integer.valueOf(1), config.getIntegerValueWithDifferentKey());
		Assert.assertEquals(Integer.valueOf(50), config.getDefaultIntegerValueTest());
		Assert.assertEquals("aabbcc", config.getDefaultStringValueTest());
		Assert.assertEquals(Double.valueOf(1.234), config.getDefaultDoubleValueTest());
		Assert.assertEquals(TestConfigImpl.class, config.getDefaultClassValueTest());
	}

	@Test
	public void testTypes() {
		TypeTestConfig config = ConfigImpl.newInstance(TypeTestConfig.class, configResolver);
		Assert.assertNotNull(config);
		Assert.assertEquals(Integer.valueOf(1), config.getIntegerValue());
		Assert.assertEquals(Double.valueOf(1.234), config.getDoubleValue());
		Assert.assertEquals("abc123", config.getStringValue());
		Assert.assertEquals(Config.class, config.getClassValue());
	}

	@Test
	public void testPrefixes() {
		PrefixTestConfig config = ConfigImpl.newInstance(PrefixTestConfig.class, configResolver);
		Assert.assertNotNull(config);
		Assert.assertNull(config.getIntegerValue());
		Assert.assertEquals("abcdef", config.getStringValue());
		Assert.assertEquals("abcdef", config.getStringValueWithDifferentKey());
	}

	@Test(expected = IllegalStateException.class)
	public void testNoValidator() {
		PrefixTestConfig config = ConfigImpl.newInstance(PrefixTestConfig.class, configResolver);
		config.validate();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoPrimitiveReturnTypes() {
		ConfigImpl.newInstance(ConfigWithPrimitive.class, configResolver);
	}

	@Test(expected = IllegalStateException.class)
	public void testDefaultTypeValidation() {
		ConfigImpl.newInstance(ConfigWithBadDefault.class, configResolver);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConfigWithArguments() {
		ConfigImpl.newInstance(ConfigWithArguments.class, configResolver);
	}

	// TODO test validation!
}
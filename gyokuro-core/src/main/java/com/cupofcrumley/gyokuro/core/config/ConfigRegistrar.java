package com.cupofcrumley.gyokuro.core.config;

import java.util.Map;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class ConfigRegistrar implements ImportBeanDefinitionRegistrar {
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Map<String, Object> rawAttributes = importingClassMetadata.getAnnotationAttributes(EnableConfig.class.getName());
		AnnotationAttributes attributes = new AnnotationAttributes(rawAttributes);
		Class<?>[] classes = attributes.getClassArray("value");

		for (Class<?> clazz : classes) {
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ConfigFactoryBean.class);
			builder.addConstructorArgValue(clazz);
			builder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);

			registry.registerBeanDefinition(clazz.getName(), builder.getBeanDefinition());
		}
	}
}

package com.cupofcrumley.gyokuro.web;

import javax.validation.constraints.Min;

import com.cupofcrumley.gyokuro.config.Config;

public interface JettyConfig extends Config {
	@Min(2)
	@DefaultIntegerValue(10)
	public Integer getMaxThreads();
	
	@Min(1)
	@DefaultIntegerValue(2)
	public Integer getMinThreads();
}

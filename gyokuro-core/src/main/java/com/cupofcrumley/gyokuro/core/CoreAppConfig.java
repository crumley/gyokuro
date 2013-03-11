package com.cupofcrumley.gyokuro.core;

import org.hibernate.validator.constraints.NotEmpty;

import com.cupofcrumley.gyokuro.config.Config;
import com.cupofcrumley.gyokuro.config.KeyPrefix;

@KeyPrefix("core.")
public interface CoreAppConfig extends Config {

	@NotEmpty
	@Description("Represents the name of this configuration. It is good practice to name each configuration file so they can be identified at runtime.")
	public String getConfigName();

}

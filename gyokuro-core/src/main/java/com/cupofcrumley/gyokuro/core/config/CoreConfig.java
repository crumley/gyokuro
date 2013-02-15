package com.cupofcrumley.gyokuro.core.config;

import org.hibernate.validator.constraints.NotEmpty;

public interface CoreConfig extends Config {

	@NotEmpty
	@Description("Represents the name of this configuration. It is good practice to name each configuration file so they can be identified at runtime.")
	public String getConfigName();

}

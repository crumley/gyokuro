package com.cupofcrumley.gyokuro.core.app;

import javax.validation.constraints.NotNull;

public interface AppCommand<E extends CommandOptions> {
	@NotNull
	public String getName();

	public Class<E> getOptionsClass();

	public int execute(@NotNull E params);
}
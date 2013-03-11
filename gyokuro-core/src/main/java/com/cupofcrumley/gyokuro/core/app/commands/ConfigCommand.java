package com.cupofcrumley.gyokuro.core.app.commands;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import org.springframework.util.Assert;

import com.beust.jcommander.Parameter;
import com.cupofcrumley.gyokuro.config.Config;
import com.cupofcrumley.gyokuro.config.Config.ConfigSummary;
import com.cupofcrumley.gyokuro.core.app.AppCommand;
import com.cupofcrumley.gyokuro.core.app.CommandOptions;
import com.cupofcrumley.gyokuro.core.app.commands.ConfigCommand.ConfigOptions;
import com.google.common.base.Strings;

public class ConfigCommand implements AppCommand<ConfigOptions> {
	public static class ConfigOptions implements CommandOptions {
		@Parameter(names = { "-f", "--file" }, description = "Filename to output configuration properties to (default is stdout)")
		private String outputFilename;
	}

	private Config[] configs;

	public ConfigCommand(Config[] configs) {
		Assert.notNull(configs);
		this.configs = configs;
	}

	@Override
	public Class<ConfigOptions> getOptionsClass() {
		return ConfigOptions.class;
	}

	@Override
	public String getName() {
		return "config";
	}

	@Override
	public int execute(ConfigOptions options) {
		// TODO Should we force UTF-8?
		Writer writer;
		if (options.outputFilename != null) {
			try {
				writer = new FileWriter(options.outputFilename);
			} catch (IOException e) {
				System.err.println("Error writing options to file: " + options.outputFilename);
				e.printStackTrace();
				return 1;
			}
		} else {
			writer = new PrintWriter(System.out);
		}

		for (Config config : configs) {
			try {
				outputOptions(writer, config);
			} catch (IOException e) {
				System.err.println("Error writing options: " + e);
				e.printStackTrace();
				return 1;
			}
		}

		try {
			writer.flush();
		} catch (IOException e) {
			System.err.println("Error flushing stream: " + e);
			e.printStackTrace();
			return 1;
		}

		return 0;
	}

	protected void outputOptions(Writer writer, Config config) throws IOException {
		writer.write("# " + config.getClass().getInterfaces()[0].getName() + "\n");
		List<ConfigSummary> allSummaries = config.getConfigSummary();
		for (ConfigSummary summary : allSummaries) {
			String description = summary.getDescription();
			String defaultValue = summary.getDefault();
			if (!Strings.isNullOrEmpty(description)) {
				writer.write("# " + description);
				if (defaultValue != null) {
					writer.write(" Default: '" + defaultValue + "'");
				}
				writer.write("\n");
			} else if (defaultValue != null) {
				writer.write("# Default: '" + defaultValue + "'\n");
			}

			if (defaultValue != null && defaultValue.equals(summary.getValue())) {
				writer.write("#");
			}
			writer.write(summary.getKey());
			writer.write("=");
			writer.write(summary.getValue());
			writer.write("\n");
		}
		writer.write("\n");
	}
}
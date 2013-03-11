package com.cupofcrumley.gyokuro.core.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

public class AppService {
	private static final Logger log = LoggerFactory.getLogger(AppService.class);

	private List<AppCommand<?>> commands = new ArrayList<AppCommand<?>>(1);

	public AppService(AppCommand<?>[] commands) {
		this.commands.addAll(Arrays.asList(commands));
	}

	public <T extends GyokuroApp<?>> void start(T app, JCommander jc, String[] args) {
		log.debug("Application: {} started with arguments: {}", app, args);

		Map<String, CommandOptions> commandOptionMap = new HashMap<String, CommandOptions>();
		Map<String, AppCommand<? extends CommandOptions>> commandMap = new HashMap<String, AppCommand<? extends CommandOptions>>();

		for (AppCommand<? extends CommandOptions> command : commands) {
			log.debug("Command registered: {} class: ", command.getName(), command.getClass().getName());

			CommandOptions commandOptions = createCommandOptionsInstance(command);
			commandMap.put(command.getName(), command);
			commandOptionMap.put(command.getName(), commandOptions);
			jc.addCommand(command.getName(), commandOptions);
		}

		// Ensure we complain about unknown options.
		jc.setAcceptUnknownOptions(false);
		jc.setProgramName(app.getClass().getSimpleName());
		jc.parse(args);

		String commandName = jc.getParsedCommand();

		// If we weren't able to establish a command print usage and exit
		if (commandName == null) {
			jc.usage();
			System.exit(0);
		}

		// Otherwise run the specified command
		AppCommand<?> command = commandMap.get(commandName);
		CommandOptions parameters = commandOptionMap.get(commandName);
		System.exit(execute(command, parameters));
	}

	private <T extends CommandOptions> T createCommandOptionsInstance(AppCommand<T> command) {
		Class<T> optionsClass = command.getOptionsClass();
		try {
			return optionsClass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to create options class for command line parsing. Class must have a public default constructor.", e);
		}
	}

	private <T extends CommandOptions> int execute(AppCommand<T> command, CommandOptions options) {
		log.info("Executing command: {} with options: {}", command.getName(), options);
		return command.execute(command.getOptionsClass().cast(options));
	}
}

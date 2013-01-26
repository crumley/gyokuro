/**
 * The MIT License
 * 
 * Copyright (c) 2010-2012 Ryan Crumley
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.cupofcrumley.gyokuro.core.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Loads configuration from properties file into Spring Environment. Default location for properties file is
 * <code>classpath:application.override.properties</code>. This can be overriden by setting the system property
 * "config.override". For example:
 * 
 * java MainClass -Dconfig.override=file:/opt/app/myconfig.properties
 * 
 * @author Ryan Crumley
 */
@Configuration
@PropertySource(name = "core.config", value = { "${config.override:classpath:application.override.properties}" })
public class PropertyConfig {
	private static final Logger log = LoggerFactory.getLogger(PropertyConfig.class);

	@Autowired
	private Environment env;

	@PostConstruct
	public void init() {
		log.info("Loaded property configuration from location '{}'.", env.getProperty("config.override"));
	}
}
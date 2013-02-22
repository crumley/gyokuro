package com.cupofcrumley.gyokuro.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.cupofcrumley.gyokuro.core.config.PropertyConfiguration;
import com.cupofcrumley.gyokuro.web.WebConfiguration;

@Configuration
@Import(value = {PropertyConfiguration.class, WebConfiguration.class})
public class ExampleConfiguration {

}

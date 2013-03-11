package com.cupofcrumley.gyokuro.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.cupofcrumley.gyokuro.core.AppConfiguration;
import com.cupofcrumley.gyokuro.web.WebConfiguration;

@Configuration
@Import(value = {AppConfiguration.class, WebConfiguration.class})
public class ExampleConfiguration {

}

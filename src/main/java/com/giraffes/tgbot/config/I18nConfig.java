package com.giraffes.tgbot.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class I18nConfig {
    @Bean
    public MessageSource messageSource() {
        final ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setDefaultLocale(Locale.ENGLISH);
        source.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}

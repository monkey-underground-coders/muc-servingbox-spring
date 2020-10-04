package com.a6raywa1cher.mucservingboxspring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@PropertySource(value = "classpath:strings.properties", encoding = "UTF-8")
@ConfigurationProperties(prefix = "strings")
@Data
@Validated
public class StringConfigProperties {
}

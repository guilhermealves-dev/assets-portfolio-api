package dev.guilhermealves.assets.portfolio.api.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.TimeZone;

@Slf4j
@Configuration
public class TimeZoneConfig {

    @Value("${application.timezone}")
    private String applicationTimeZone;

    @PostConstruct
    public void initialization() {
        log.info("Default time zone configured to {}", applicationTimeZone);
        TimeZone.setDefault(TimeZone.getTimeZone(applicationTimeZone));
    }
}

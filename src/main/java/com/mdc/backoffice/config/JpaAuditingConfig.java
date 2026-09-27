package com.mdc.backoffice.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Until authentication is introduced, writes are attributed to the application.
    @Bean
    AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }
}

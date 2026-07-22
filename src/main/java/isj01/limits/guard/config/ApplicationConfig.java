package isj01.limits.guard.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("UTC"));
    }

}

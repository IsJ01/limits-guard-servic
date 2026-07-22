package isj01.limits.guard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GuardApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuardApplication.class, args);
	}

}

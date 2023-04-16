package clusterless.scenario.conductor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"com.netflix.conductor", "io.orkes.conductor"})
public class ConductorApp {
    private static final Logger LOG = LogManager.getLogger(ConductorApp.class);

    public static ConfigurableApplicationContext run(String... args) {
        LOG.info("Starting Conductor");
        ConfigurableApplicationContext run = SpringApplication.run(ConductorApp.class, args);
        LOG.info("Started Conductor");

        return run;
    }
}

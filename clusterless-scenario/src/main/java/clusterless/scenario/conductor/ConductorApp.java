/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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

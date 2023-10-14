/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.config;

import clusterless.cls.json.JSONUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class ConfigTest {

    static class TestConfig extends Configuration {
        String a;
        String b;
        String c;

        public TestConfig() {
        }

        public TestConfig(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public String name() {
            return "test";
        }
    }

    @Test
    void verifyDefaults() {
        TestConfig overrideConfig = new TestConfig("a1", null, null);
        TestConfig defaultConfig = new TestConfig("a", "b", "c");

        List<ObjectNode> configs = new LinkedList<>();

        configs.add(JSONUtil.valueToObjectNodeNoNulls(overrideConfig));
        configs.add(JSONUtil.valueToObjectNodeNoNulls(defaultConfig));

        TestConfig resultConfig = ConfigManager.mergeIntoConfig(configs, TestConfig.class);

        Assertions.assertNotNull(resultConfig.a);
        Assertions.assertNotNull(resultConfig.b);
        Assertions.assertNotNull(resultConfig.c);
        Assertions.assertEquals("a1", resultConfig.a);
        Assertions.assertEquals("b", resultConfig.b);
        Assertions.assertEquals("c", resultConfig.c);
    }

    @Test
    void verifyDefaultsProperties() {
        Properties properties = new Properties();

        properties.setProperty("common.c", "cp");
        properties.setProperty("unknown.c", "cp");

        TestConfig overrideConfig = new TestConfig("a1", null, null);
        TestConfig defaultConfig = new TestConfig("a", "b", "c");

        List<ObjectNode> configs = new LinkedList<>();

        configs.add(JSONUtil.valueToObjectNodeNoNulls(JSONUtil.readPropertiesSafe(properties, "common", TestConfig.class)));
        configs.add(JSONUtil.valueToObjectNodeNoNulls(overrideConfig));
        configs.add(JSONUtil.valueToObjectNodeNoNulls(defaultConfig));

        TestConfig resultConfig = ConfigManager.mergeIntoConfig(configs, TestConfig.class);

        Assertions.assertNotNull(resultConfig.a);
        Assertions.assertNotNull(resultConfig.b);
        Assertions.assertNotNull(resultConfig.c);
        Assertions.assertEquals("a1", resultConfig.a);
        Assertions.assertEquals("b", resultConfig.b);
        Assertions.assertEquals("cp", resultConfig.c);
    }
}

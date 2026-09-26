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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    static class MapConfig extends Configuration {
        String a;
        String b;
        Map<String, String> m = new LinkedHashMap<>();

        @Override
        public String name() {
            return "map";
        }
    }

    // the persisted TOML config omits null fields and null map content
    @Test
    void tomlWriterOmitsNulls() {
        MapConfig config = new MapConfig();
        config.a = "a1";
        config.m.put("k", "v");
        config.m.put("n", null);

        Assertions.assertEquals("a = 'a1'\nm.k = 'v'\n", ConfigManager.toString(config));
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

    // kata 7fvz: the nearest-.clsconfig walk must terminate at the filesystem root when cwd is
    // outside $HOME, and must honor a .clsconfig sitting in $HOME itself.

    private static ConfigOptions options(Path home, Path local) {
        return ConfigOptions.Builder.builder()
                .withHomePath(home)
                .withGlobalConfigPath(home.resolve(".cls"))
                .withGlobalConfigName(Paths.get("config-test"))
                .withLocalPath(local)
                .withLocalConfigName(Paths.get(".clsconfig-test"))
                .withConfigNamespace("test")
                .withConfigClass(TestConfig.class)
                .build();
    }

    private static Path dirs(Path path) throws IOException {
        return Files.createDirectories(path);
    }

    private static void write(Path dir, String toml) throws IOException {
        Files.writeString(dirs(dir).resolve(".clsconfig-test"), toml);
    }

    @Test
    void outsideHomeDoesNotThrow(@TempDir Path root) throws IOException {
        Path home = dirs(root.resolve("home"));
        Path work = dirs(root.resolve("work"));

        TestConfig config = Assertions.assertDoesNotThrow(() -> ConfigManager.<TestConfig>loadConfig(options(home, work)));

        Assertions.assertNull(config.a);
    }

    @Test
    void outsideHomeFindsAncestorConfig(@TempDir Path root) throws IOException {
        Path home = dirs(root.resolve("home"));
        Path cwd = dirs(root.resolve("work/x/y"));
        write(root.resolve("work/x"), "a = \"x\"\n");

        TestConfig config = ConfigManager.loadConfig(options(home, cwd));

        Assertions.assertEquals("x", config.a);
    }

    @Test
    void homeLocalConfigHonoredBelowHome(@TempDir Path root) throws IOException {
        Path home = dirs(root.resolve("home"));
        Path cwd = dirs(home.resolve("a/b"));
        write(home, "a = \"home\"\n");

        TestConfig config = ConfigManager.loadConfig(options(home, cwd));

        Assertions.assertEquals("home", config.a);
    }

    @Test
    void homeLocalConfigHonoredAtHome(@TempDir Path root) throws IOException {
        Path home = dirs(root.resolve("home"));
        write(home, "a = \"home\"\n");

        TestConfig config = ConfigManager.loadConfig(options(home, home));

        Assertions.assertEquals("home", config.a);
    }

    @Test
    void nearestWinsFirstHitOnly(@TempDir Path root) throws IOException {
        Path home = dirs(root.resolve("home"));
        Path cwd = dirs(home.resolve("a/b"));
        write(home, "a = \"home\"\nb = \"home\"\n");
        write(home.resolve("a"), "a = \"near\"\n");

        TestConfig config = ConfigManager.loadConfig(options(home, cwd));

        Assertions.assertEquals("near", config.a);
        Assertions.assertNull(config.b);
    }

    @Test
    void walkStopsAtHome(@TempDir Path root) throws IOException {
        Path home = dirs(root.resolve("home"));
        Path cwd = dirs(home.resolve("a"));
        write(root, "a = \"above\"\n");

        TestConfig config = ConfigManager.loadConfig(options(home, cwd));

        Assertions.assertNull(config.a);
    }

    @Test
    void propertiesOverLocalOverGlobal(@TempDir Path root) throws IOException {
        Path home = dirs(root.resolve("home"));
        Path cwd = dirs(home.resolve("a"));
        write(cwd, "a = \"local\"\nb = \"local\"\n");
        Files.writeString(dirs(home.resolve(".cls")).resolve("config-test"), "a = \"global\"\nb = \"global\"\nc = \"global\"\n");

        Properties properties = new Properties();
        properties.setProperty("test.a", "prop");

        TestConfig config = ConfigManager.loadConfig(properties, options(home, cwd));

        Assertions.assertEquals("prop", config.a);
        Assertions.assertEquals("local", config.b);
        Assertions.assertEquals("global", config.c);
    }
}

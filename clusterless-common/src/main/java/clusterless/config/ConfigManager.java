/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ConfigManager {
    public static final Path CURRENT_DIR = Paths.get(".").toAbsolutePath();
    public static final Path HOME_DIR = Paths.get(System.getProperty("user.home")).toAbsolutePath();
    public static final Path GLOBAL_CONFIG_DIR = HOME_DIR.resolve(Paths.get(".cls"));
    public static final String GLOBAL_CONFIG_NAME = "config";
    public static final String LOCAL_CONFIG_NAME = ".clsconfig";

    interface Handler {

        TomlMapper mapper();

        ObjectWriter writer();

        ObjectReader reader();
    }

    static class Toml implements Handler {
        final TomlMapper mapper = new TomlMapper();

        {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        final ObjectReader reader = mapper
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
                .reader();

        @Override
        public TomlMapper mapper() {
            return mapper;
        }

        @Override
        public ObjectWriter writer() {
            return writer;
        }

        @Override
        public ObjectReader reader() {
            return reader;
        }
    }

    static Handler handler = new Toml();

    static JsonNodeFactory FACTORY = new ObjectMapper().getNodeFactory();

    public static ConfigOptions optionsFor(String name, Class<? extends Configuration> configClass) {
        if ("common".equals(name)) {
            return CommonConfig.configOptions;
        }

        return ConfigOptions.Builder.builder()
                .withGlobalConfigName(Paths.get(String.format("%s-%s", GLOBAL_CONFIG_NAME, name)))
                .withLocalConfigName(Paths.get(String.format("%s-%s", LOCAL_CONFIG_NAME, name)))
                .withConfigClass(configClass)
                .build();
    }

    public static void writeLocalConfig(ConfigOptions configOptions, Config config, boolean force) {
        Path localConfig = configOptions.localPath().resolve(configOptions.localConfigName());

        if (!force && Files.exists(localConfig)) {
            throw new IllegalStateException("local config already exists at: " + localConfig);
        }

        toFile(localConfig, config);
    }

    public static void writeGlobalConfig(ConfigOptions configOptions, Config config, boolean force) {
        Path globalConfig = configOptions.globalConfigPath().resolve(configOptions.globalConfigName());

        if (!force && Files.exists(globalConfig)) {
            throw new IllegalStateException("global config already exists at: " + globalConfig);
        }

        toFile(globalConfig, config);
    }

    public static <C extends Configuration> C loadConfig(ConfigOptions configOptions) {
        List<ObjectNode> configs = new LinkedList<>();

        Path currentDir = configOptions.localPath();

        while (!currentDir.equals(configOptions.homePath())) {
            Path path = currentDir.resolve(configOptions.localConfigName());

            if (Files.exists(path)) {
                configs.add(readTreeSafe(path.toFile()));
                break;
            }

            currentDir = currentDir.getParent();
        }

        Path globalConfig = configOptions.globalConfigPath().resolve(configOptions.globalConfigName());
        if (Files.exists(globalConfig)) {
            configs.add(readTreeSafe(globalConfig.toFile()));
        }

        return (C) mergeIntoConfig(configs, configOptions.configClass());
    }

    protected static <C extends Configuration> C mergeIntoConfig(List<ObjectNode> configs, Class<C> type) {
        List<ObjectNode> reversed = new LinkedList<>(configs);
        Collections.reverse(reversed);

        ObjectNode nodeConfig = reversed.stream().reduce(FACTORY.objectNode(), ObjectNode::setAll);

        return treeToValueSafe(nodeConfig, type);
    }

    public static <J extends JsonNode> J readTreeSafe(File file) {
        try {
            return readTree(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <J extends JsonNode> J readTree(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("does not exist: " + file);
        }

        return (J) handler.reader().readTree(new FileInputStream(file));
    }

    public static <T> T treeToValueSafe(JsonNode n, Class<T> type) {
        try {
            return handler.mapper().treeToValue(n, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toString(Config config) {
        try {
            return handler.writer().forType(config.getClass()).writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void toFile(Path path, Config config) {
        try {
            Path parent = path.getParent();

            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            handler.writer().forType(config.getClass())
                    .writeValue(path.toFile(), config);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

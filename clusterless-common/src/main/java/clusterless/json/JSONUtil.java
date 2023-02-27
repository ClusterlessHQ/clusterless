/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS;

/**
 *
 */
public class JSONUtil {

    public static final List<SimpleModule> modules = List.of(
            new JodaModule(),
            new JavaTimeModule()
    );

    private static ObjectMapper create() {
        return new ObjectMapper()
                .registerModules(modules)
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(WRITE_DURATIONS_AS_TIMESTAMPS, false);
    }

    public static final ObjectMapper OBJECT_MAPPER = create();

    public static final ObjectMapper OBJECT_MAPPER_NO_NULL = create()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    public static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer();

    public static final ObjectWriter OBJECT_WRITER_PRETTY = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    public static final ObjectReader OBJECT_READER = OBJECT_MAPPER
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
            .reader();

    public static List<String> getAt(ArrayNode jsonNodes, String pointer) {
        List<String> results = new LinkedList<>();

        jsonNodes.forEach(n -> results.add(n.at(pointer).asText()));

        return results;
    }

    public static ArrayNode readTrees(List<File> files) throws IOException {
        ArrayNode arrayNode = createArrayNode();

        for (File file : files) {
            arrayNode.add(readTree(file));
        }

        return arrayNode;
    }

    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.getNodeFactory().arrayNode();
    }

    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.getNodeFactory().objectNode();
    }

    public static JsonNode readTree(String json) throws IOException {
        return OBJECT_READER.readTree(json);
    }

    public static ObjectNode valueToObjectNodeNoNulls(Object object) {
        return OBJECT_MAPPER_NO_NULL.valueToTree(object);
    }

    public static ObjectNode valueToObjectNode(Object object) throws IOException {
        return OBJECT_MAPPER.valueToTree(object);
    }

    public static JsonNode readTree(InputStream inputStream) throws IOException {
        return OBJECT_READER.readTree(inputStream);
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

        return (J) OBJECT_READER.readTree(new FileInputStream(file));
    }

    public static <T> T readObjectSafe(String json, Class<T> type) {
        try {
            return OBJECT_READER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String writeAsStringSafe(Object object) {
        try {
            return OBJECT_WRITER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String writeAsPrettyStringSafe(Object object) {
        try {
            return OBJECT_WRITER_PRETTY.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T treeToValueSafe(JsonNode n, Class<T> type) {
        try {
            return OBJECT_MAPPER.treeToValue(n, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}


/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

    public static final List<SimpleModule> modules = List.of(new JodaModule(), new JavaTimeModule());
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER
                .registerModules(modules)
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(WRITE_DURATIONS_AS_TIMESTAMPS, false);
    }

    public static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer();

    public static final ObjectWriter OBJECT_WRITER_PRETTY = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

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

    public static JsonNode readTree(InputStream inputStream) throws IOException {
        return OBJECT_MAPPER.readTree(inputStream);
    }

    public static JsonNode readTree(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("does not exist: " + file);
        }

        return OBJECT_MAPPER.readTree(file);
    }

    public static <T> T readObjectSafe(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
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

    public static <T> T treeToValueSafe(JsonNode n, Class<T> type) {
        try {
            return OBJECT_MAPPER.treeToValue(n, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}


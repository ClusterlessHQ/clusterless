/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS;

/**
 *
 */
public class JSONUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer();

    public static final ObjectWriter OBJECT_WRITER_PRETTY = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    static {
        OBJECT_MAPPER
                .registerModule(new JodaModule())
                .registerModule(new JavaTimeModule())
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(WRITE_DURATIONS_AS_TIMESTAMPS, false);
    }

    public static List<String> readTreesWithPointer(List<File> files, String pointer) {
        List<String> results = new LinkedList<>();

        readTrees(files).forEach(n -> results.add(n.at(pointer).asText()));

        return results;
    }

    public static ArrayNode readTrees(List<File> files) {
        ArrayNode arrayNode = OBJECT_MAPPER.getNodeFactory().arrayNode();

        for (File file : files) {
            arrayNode.add(readTree(file));
        }

        return arrayNode;
    }

    public static JsonNode readTree(File file) {
        try {
            return OBJECT_MAPPER.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


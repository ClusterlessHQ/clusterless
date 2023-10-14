/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsSchema;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModules(modules)
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(WRITE_DURATIONS_AS_TIMESTAMPS, false);
    }

    private static JavaPropsMapper createPropertiesMapper() {
        return (JavaPropsMapper) new JavaPropsMapper()
                .registerModules(modules)
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(WRITE_DURATIONS_AS_TIMESTAMPS, false);
    }

    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    public static final ObjectMapper TYPED_OBJECT_MAPPER = createObjectMapper()
            .activateDefaultTypingAsProperty(
                    new DefaultBaseTypeLimitingValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    "__type"
            );
    public static final JavaPropsMapper PROPERTIES_MAPPER = createPropertiesMapper();

    public static final ObjectMapper OBJECT_MAPPER_NO_NULL = createObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    public static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer();

    public static final ObjectWriter OBJECT_WRITER_PRETTY = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();


    public static final ObjectWriter OBJECT_REQUIRED_WRITER_PRETTY = createObjectMapper()
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .writerWithDefaultPrettyPrinter()
            .withView(Views.Required.class);

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

    public static ObjectNode valueToObjectNode(Object object) {
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

    public static <T> T readObjectSafe(byte[] bytes, Class<T> type) {
        try {
            return OBJECT_READER.readValue(bytes, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readObjectSafe(String json, Class<T> type) {
        try {
            return OBJECT_READER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readTypedObjectSafe(String json, Class<T> type) {
        try {
            return TYPED_OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readObjectSafe(Path path, Class<T> type) {
        try {
            return OBJECT_READER.readValue(path.toFile(), type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readPropertiesSafe(Properties properties, String namespace, Class<T> type) {
        try {
            JavaPropsSchema schema = JavaPropsSchema.emptySchema().withPrefix(namespace);
            return PROPERTIES_MAPPER.readPropertiesAs(properties, schema, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, String> asMapSafe(String namespace, Object object) {
        try {
            JavaPropsSchema schema = JavaPropsSchema.emptySchema().withPrefix(namespace);
            return PROPERTIES_MAPPER.writeValueAsMap(object, schema);
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

    public static void writeAsStringSafe(Path path, Object object) {
        try {
            String string = OBJECT_WRITER.writeValueAsString(object);
            Files.write(path, List.of(string));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readAsObjectSafe(Path path, Class<T> type) {
        try {
            return OBJECT_READER.readValue(path.toFile(), type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readAsObjectSafe(Path path, TypeReference<T> type) {
        try {
            return OBJECT_READER.forType(type).readValue(path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readAsObject(Path path, TypeReference<T> type) throws IOException {
        return OBJECT_READER.forType(type).readValue(path.toFile());
    }

    public static <T> T readAsObjectSafe(InputStream inputStream, TypeReference<T> type) {
        try {
            return OBJECT_READER.forType(type).readValue(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String writeTypedAsStringSafe(Object object) {
        try {
            return TYPED_OBJECT_MAPPER.writeValueAsString(object);
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

    public static String writeRequiredAsPrettyStringSafe(Object object) {
        try {
            return OBJECT_REQUIRED_WRITER_PRETTY.writeValueAsString(object);
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

    public static <T> T treeToValue(JsonNode n, Class<T> type) throws JsonProcessingException {
        return OBJECT_MAPPER.treeToValue(n, type);
    }

    public static <E> ObjectReader objectReaderFor(Class<E> type) {
        return OBJECT_MAPPER.readerFor(type);
    }
}


/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model;

import clusterless.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
public class Loader {
    private final ArrayNode arrayNode;
    private final List<File> projectFiles;

    public Loader(List<File> projectFiles) throws IOException {
        this.projectFiles = projectFiles;

        if (projectFiles.isEmpty()) {
            throw new IllegalArgumentException("no project deploy files declared");
        }

        arrayNode = JSONUtil.readTrees(projectFiles);
    }

    public List<String> getStringsAt(String pointer) {
        return JSONUtil.getAt(arrayNode, pointer);
    }

    public <T> List<T> readObjects(String provider, String providerPointer, Class<T> type, BiConsumer<T, File> consumer) {
        return readObjects(
                n -> provider.equals(n.at(providerPointer).textValue()),
                type,
                (deploy, integer) -> {
                    if (!projectFiles.isEmpty()) {
                        consumer.accept(deploy, projectFiles.get(integer));
                    }
                    return deploy;
                });
    }

    public <T> List<T> readObjects(Predicate<JsonNode> retainFilter, Class<T> type, BiFunction<T, Integer, T> function) {
        List<T> results = new LinkedList<>();

        int count = 0;
        for (JsonNode jsonNode : arrayNode) {
            if (retainFilter.test(jsonNode)) {
                try {
                    results.add(function.apply(JSONUtil.treeToValue(jsonNode, type), count++));
                } catch (UnrecognizedPropertyException e) {
                    // todo: support proper exit code
                    // Unrecognized field "<propertyName>" (class <className>), not marked as ignorable (one known property: "<best guess property>"])
                    String path = e.getPath().stream().map(r -> r.getFieldName() == null ? "[" + r.getIndex() + "]" : r.getFieldName()).collect(Collectors.joining("."));
                    throw new IllegalArgumentException("invalid file format for \"" + type.getName() + "\", unrecognized field \"" + path + "\"", e);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return results;
    }
}

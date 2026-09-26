/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pins the inclusion and view rules of the specialized mappers in {@link JSONUtil}; their output
 * feeds config merging and the persisted/printed JSON, so it is a wire contract.
 */
public class JSONUtilTest {

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class Nullable {
        String present = "value";
        String absent = null;
        String empty = "";
        List<String> emptyList = List.of();
        List<String> listWithNull = Arrays.asList("x", null);
        Map<String, String> map = new LinkedHashMap<>();
        Map<String, String> absentMap = null;

        Nullable() {
            map.put("k", "v");
            map.put("n", null);
        }
    }

    @Test
    void valueToObjectNodeNoNullsDropsNullValuesAndNullMapContent() throws IOException {
        ObjectNode node = JSONUtil.valueToObjectNodeNoNulls(new Nullable());

        JsonNode expected = JSONUtil.readTree("""
                {
                  "present": "value",
                  "empty": "",
                  "emptyList": [],
                  "listWithNull": ["x", null],
                  "map": {"k": "v"}
                }
                """);

        Assertions.assertEquals(expected, node);
    }

    @Test
    void valueToObjectNodeRetainsNulls() throws IOException {
        ObjectNode node = JSONUtil.valueToObjectNode(new Nullable());

        JsonNode expected = JSONUtil.readTree("""
                {
                  "present": "value",
                  "absent": null,
                  "empty": "",
                  "emptyList": [],
                  "listWithNull": ["x", null],
                  "map": {"k": "v", "n": null},
                  "absentMap": null
                }
                """);

        Assertions.assertEquals(expected, node);
    }

    static class OtherView {
    }

    static class Viewed {
        @JsonView(Views.Required.class)
        public String required = "r";
        @JsonRequiredProperty
        public String requiredMeta = "m";
        @JsonView(Views.Required.class)
        public Instant instant = Instant.parse("2024-01-02T03:04:05Z");
        @JsonView(Views.Required.class)
        public Duration duration = Duration.ofMinutes(1);
        @JsonView(Views.Required.class)
        public org.joda.time.Instant jodaInstant = org.joda.time.Instant.parse("2024-01-02T03:04:05Z");
        @JsonView(Views.Required.class)
        public String requiredNull = null;
        public String unviewed = "u";
        @JsonView(OtherView.class)
        public String other = "o";
    }

    @Test
    void writeRequiredAsPrettyStringSafeEmitsOnlyRequiredView() throws IOException {
        String json = JSONUtil.writeRequiredAsPrettyStringSafe(new Viewed());

        JsonNode expected = JSONUtil.readTree("""
                {
                  "required": "r",
                  "requiredMeta": "m",
                  "instant": "2024-01-02T03:04:05Z",
                  "duration": "PT1M",
                  "jodaInstant": "2024-01-02T03:04:05.000Z",
                  "requiredNull": null
                }
                """);

        Assertions.assertEquals(expected, JSONUtil.readTree(json));
        Assertions.assertTrue(json.contains("\n"), "expected pretty printed output: " + json);
    }
}

/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.reporter;

import clusterless.cls.printer.Printer;
import clusterless.commons.naming.Label;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class TableReporter<T> extends Reporter<T> {
    private final Object[] fields;
    private final Object[][] headers;
    private final int[] lengths;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public TableReporter(Printer printer, Class<T> type) {
        super(printer, type);

        jsonMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<>() {
            public void serialize(Object value, JsonGenerator jgen,
                                  SerializerProvider provider)
                    throws IOException {
                jgen.writeString("-");
            }
        });

        objectWriter = jsonMapper.writer();

        CsvSchema columns = new CsvMapper().schemaFor(type);

        fields = new String[columns.size()];
        headers = new String[2][columns.size()];
        lengths = new int[headers[1].length];

        for (int i = 0; i < fields.length; i++) {
            fields[i] = columns.columnName(i);
            String[] name = Label.of(columns.columnName(i)).lowerHyphen().split("[.-]");
            headers[0][i] = name.length > 1 ? name[0] : "";
            headers[1][i] = name.length > 1 ? name[1] : name[0];
            lengths[i] = Math.max(headers[0][i].toString().length(), headers[1][i].toString().length());
        }
    }

    @Override
    public void report(Stream<T> stream) throws IOException {
        List<T> list = stream.toList();
        ArrayNode array = jsonMapper.valueToTree(list);

        for (JsonNode jsonNode : array) {
            for (int i = 0; i < lengths.length; i++) {
                JsonNode node = jsonNode.get(fields[i].toString());
                String value = node != null ? asText(node) : null;
                lengths[i] = Math.max(lengths[i], value == null ? 0 : value.length());
            }
        }

        int line = 0;
        StringBuilder formatBuilder = new StringBuilder();
        for (int i = 0; i < lengths.length; i++) {
            int length = lengths[i];
            if (length == 0) {
                formatBuilder.append("%s");
            } else {
                formatBuilder.append("%-").append(length).append("s");
            }

            line += length;

            if (i < lengths.length - 1) {
                formatBuilder.append("  ");
                line += 2;
            }
        }

        String format = formatBuilder.toString();
        printer.println(format.formatted(headers[0]));
        printer.println(format.formatted(headers[1]));
        printer.println("=".repeat(line));

        for (JsonNode jsonNode : array) {
            Object[] values = StreamEx.of(jsonNode.iterator()).map(TableReporter::asText).toArray(String.class);
            printer.println(format.formatted(values));
        }

        printer.println("");
    }

    private static String asText(JsonNode node) {
        // a hack as toString on textual quotes the values
        if (node.isTextual()) {
            return node.textValue();
        }

        if (node.isNumber()) {
            return "%,d".formatted(node.longValue());
        }
        return node.toString();
    }
}

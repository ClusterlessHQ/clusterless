/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.report.reporter;

import clusterless.printer.Printer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class TableReporter<T> extends Reporter<T> {
    private final Object[] headers;
    private final int[] lengths;
    private ObjectMapper jsonMapper = new ObjectMapper();

    public TableReporter(Printer printer, Class<T> type) {
        super(printer, type);

        objectWriter = jsonMapper.writer();

        CsvSchema columns = new CsvMapper().schemaFor(type);

        headers = new String[columns.size()];
        lengths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            headers[i] = columns.columnName(i);
            lengths[i] = columns.columnName(i).length();
        }
    }

    @Override
    public void report(Iterator<T> iterator) throws IOException {
        List<T> list = StreamEx.of(iterator).toList();
        ArrayNode array = jsonMapper.valueToTree(list);

        for (JsonNode jsonNode : array) {
            for (int i = 0; i < lengths.length; i++) {
                JsonNode node = jsonNode.get(headers[i].toString());
                String value = node != null ? node.textValue() : null;
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

        printer.println(format.formatted(headers));
        printer.println("=".repeat(line));

        for (JsonNode jsonNode : array) {
            Object[] values = StreamEx.of(jsonNode.iterator()).map(JsonNode::textValue).toArray(String.class);
            printer.println(format.formatted(values));
        }

        printer.println("");
    }
}

/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.reporter;

import clusterless.cls.printer.Printer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class Reporter<T> {
    public static <T> Reporter<T> instance(Printer printer, Class<T> type) {
        switch (printer.format()) {
            case table -> {
                return new TableReporter<>(printer, type);
            }
            case json -> {
                return new JsonReporter<T>(printer, type);
            }
            case csv -> {
                return new CSVReporter<>(printer, type);
            }
            case tsv -> {
                return new TSVReporter<>(printer, type);
            }
            default -> throw new IllegalStateException("Unexpected value: " + printer.format());
        }
    }

    Printer printer;
    Class<T> type;
    ObjectWriter objectWriter;

    public Reporter(Printer printer, Class<T> type) {
        this.printer = printer;
        this.type = type;
    }

    protected BufferedWriter writer() {
        return printer.writer();
    }

    private boolean addNewline() {
        return printer.format() == Printer.Format.json;
    }

    public ObjectWriter objectWriter() {
        return objectWriter;
    }

    public void report(Stream<T> list) throws IOException {
        report(list.iterator());
    }

    public void report(List<T> list) throws IOException {
        report(list.iterator());
    }

    public void report(Iterator<T> iterator) throws IOException {
        BufferedWriter writer = writer();

        JsonGenerator generator = objectWriter().createGenerator(writer);

        generator.writeObject(iterator);

        if (addNewline()) {
            writer.newLine();
        }

        writer.flush();
    }
}

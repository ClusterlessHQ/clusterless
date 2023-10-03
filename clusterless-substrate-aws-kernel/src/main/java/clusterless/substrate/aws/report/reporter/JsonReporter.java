/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.report.reporter;

import clusterless.printer.Printer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonReporter<T> extends Reporter<T> {
    private ObjectMapper jsonMapper = new ObjectMapper();

    public JsonReporter(Printer printer, Class<T> type) {
        super(printer, type);

        objectWriter = jsonMapper
                .writerFor(new TypeReference<T>() {})
                .withDefaultPrettyPrinter();
    }
}
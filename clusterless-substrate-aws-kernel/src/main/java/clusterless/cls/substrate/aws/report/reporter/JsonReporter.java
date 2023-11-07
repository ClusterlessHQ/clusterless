/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.reporter;

import clusterless.cls.printer.Printer;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;

/**
 * disable unwrapped annotation
 * https://github.com/FasterXML/jackson-databind/issues/133
 */
public class JsonReporter<T> extends Reporter<T> {
    private ObjectMapper jsonMapper = new ObjectMapper();

    public JsonReporter(Printer printer, Class<T> type) {
        super(printer, type);

        jsonMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            protected boolean _hasAnnotation(Annotated ann, Class<? extends Annotation> annoClass) {
                if (annoClass == JsonUnwrapped.class) {
                    return false;
                }

                return super._hasAnnotation(ann, annoClass);
            }

            @Override
            protected <A extends Annotation> A _findAnnotation(Annotated ann, Class<A> annoClass) {
                if (annoClass == JsonUnwrapped.class) {
                    return null;
                }
                return super._findAnnotation(ann, annoClass);
            }
        });

        objectWriter = jsonMapper
                .writerFor(new TypeReference<T>() {})
                .withDefaultPrettyPrinter();
    }
}

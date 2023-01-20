/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
}

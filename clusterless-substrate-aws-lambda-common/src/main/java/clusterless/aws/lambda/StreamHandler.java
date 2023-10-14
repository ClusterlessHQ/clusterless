/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.util.Memory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public abstract class StreamHandler<E> implements RequestStreamHandler {
    private static final byte[] _JsonNull = new byte[]{'n', 'u', 'l', 'l'};

    protected static final Logger LOG = LogManager.getLogger(StreamHandler.class);

    public static <E> ObjectReader objectReaderFor(Class<E> type) {
        return JSONUtil.OBJECT_MAPPER.readerFor(type);
    }

    protected ObjectReader reader;

    public StreamHandler(Class<E> type) {
        this.reader = objectReaderFor(type);
    }

    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            handleRequest(reader.readValue(input), context);

            output.write(_JsonNull);
        } finally {
            stopwatch.stop();

            LOG.info("memory: {}", Memory.memoryUsage());
            LOG.info("duration: {}", stopwatch.elapsed());
        }
    }

    public abstract void handleRequest(E event, Context context);

    protected void logObjectInfo(String message, Object object) {
        LOG.info(message, JSONUtil.writeAsStringSafe(object));
    }
}

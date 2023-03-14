/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import clusterless.json.JSONUtil;
import clusterless.substrate.aws.event.ArcStateContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

/**
 *
 */
public abstract class StreamResultHandler<E, R> implements RequestStreamHandler {
    protected static final Logger LOG = LogManager.getLogger(StreamResultHandler.class);

    public static <E> ObjectReader objectReaderFor(Class<E> type) {
        return JSONUtil.OBJECT_MAPPER.readerFor(type);
    }

    public static <E> ObjectReader objectReaderFor(JavaType type) {
        return JSONUtil.OBJECT_MAPPER.readerFor(type);
    }

    public static <R> ObjectWriter objectWriterFor(Class<R> type) {
        return JSONUtil.OBJECT_MAPPER.writerFor(type);
    }

    private ObjectWriter objectWriterFor(JavaType type) {
        return JSONUtil.OBJECT_MAPPER.writerFor(type);
    }

    protected final ObjectReader reader;

    protected final ObjectWriter writer;

    public StreamResultHandler(Class<ArcStateContext> readerType, JavaType writerType) {
        this.reader = objectReaderFor(readerType);
        this.writer = objectWriterFor(writerType);
    }

    public StreamResultHandler(Class<E> readerType, Class<R> writerType) {
        this.reader = objectReaderFor(readerType);
        this.writer = objectWriterFor(writerType);
    }

    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        E event = reader.readValue(input);
        R result = handleRequest(event, context);
        writer.writeValue(output, result);
    }

    public abstract R handleRequest(E event, Context context);

    protected void logObject(String message, Object object) {
        LOG.info(message, JSONUtil.writeAsStringSafe(object));
    }

    protected void logErrorAndThrow(Function<String, RuntimeException> exception, String format, Object... values) {
        String message = String.format(format, values);
        LOG.error(message);
        throw exception.apply(message);
    }
}

/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda;

import clusterless.cls.json.JSONUtil;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
public abstract class StreamResultHandler<E, R> implements RequestStreamHandler {
    private static final Logger LOG = LogManager.getLogger(StreamResultHandler.class);
    /**
     * Use the same message format as log4j.
     */
    protected static ParameterizedMessageFactory messageFactory = new ParameterizedMessageFactory();

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

    public StreamResultHandler(Class<E> readerType, JavaType writerType) {
        this.reader = objectReaderFor(readerType);
        this.writer = objectWriterFor(writerType);
    }

    public StreamResultHandler(Class<E> readerType, Class<R> writerType) {
        this.reader = objectReaderFor(readerType);
        this.writer = objectWriterFor(writerType);
    }

    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        E event;

        try {
            event = reader.readValue(input);
        } catch (IOException e) {
            LOG.error("unable to read input", e);
            throw e;
        }

        R result;

        try {
            result = handleRequest(event, context);
        } catch (RuntimeException e) {
            LOG.error("unable to handle request", e);
            throw e;
        }

        try {
            writer.writeValue(output, result);
        } catch (IOException e) {
            LOG.error("unable to write output", e);
            throw e;
        }
    }

    public abstract R handleRequest(E event, Context context);

    protected void logInfoObject(String message, Object object) {
        LOG.info(message, JSONUtil.writeAsStringSafe(object));
    }

    protected void logInfo(String format, Object... values) {
        // use the log4j message factory so that we don't introduce yet another log format
        LOG.info(format, values);
    }

    protected <T> T logErrorAndThrow(BiFunction<String, Throwable, RuntimeException> exceptionFactory, Throwable cause, String format, Object... values) {
        // use the log4j message factory so that we don't introduce yet another log format
        String message = messageFactory.newMessage(format, values).getFormattedMessage();
        LOG.error(message, cause);
        throw exceptionFactory.apply(message, cause);
    }

    protected <T> T logErrorAndThrow(Function<String, RuntimeException> exceptionFactory, String format, Object... values) {
        // use the log4j message factory so that we don't introduce yet another log format
        String message = messageFactory.newMessage(format, values).getFormattedMessage();
        LOG.error(message);
        throw exceptionFactory.apply(message);
    }
}

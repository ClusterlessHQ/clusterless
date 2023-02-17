/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.manifest;

import clusterless.json.JSONUtil;
import clusterless.lambda.transform.PutEventTransformHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public abstract class StreamHandler<E> implements RequestStreamHandler {
    protected static final Logger LOG = LogManager.getLogger(PutEventTransformHandler.class);
    protected ObjectReader reader;

    public StreamHandler(Class<E> type) {
        this.reader = JSONUtil.OBJECT_MAPPER.readerFor(type);
    }

    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        handleRequest(reader.readValue(input), context);
    }

    public abstract void handleRequest(E event, Context context);

    protected void logObject(String message, Object object) {
        LOG.info(message, JSONUtil.writeAsStringSafe(object));
    }

    protected void logMessage(String message, Object... objects) {
        LOG.info(message, objects);
    }
}

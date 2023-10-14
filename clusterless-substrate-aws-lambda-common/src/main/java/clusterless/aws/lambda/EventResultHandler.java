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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.MapType;

import java.util.LinkedHashMap;

public abstract class EventResultHandler<E, R, O extends EventObserver> extends StreamResultHandler<E, R> {
    public EventResultHandler(Class<E> incoming, Class<R> outgoing) {
        super(incoming, outgoing);
    }

    public EventResultHandler(Class<E> incoming, JavaType outgoing) {
        super(incoming, outgoing);
    }

    public static MapType getMapTypeFor(Class<String> keyClass, Class<?> valueClass) {
        return JSONUtil.OBJECT_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, keyClass, valueClass);
    }

    protected abstract O observer();

    @Override
    public R handleRequest(E event, Context context) {
        logInfoObject("incoming event: {}", event);

        R r = null;
        try {
            r = handleEvent(event, context, observer());
        } catch (Exception e) {
            // TODO: update to only catch unexpected exceptions
            logErrorAndThrow(RuntimeException::new, e, "failed executing handler: {}, with: {}", getClass().getName(), e.getMessage());
        }

        logInfoObject("outgoing object: {}", r);

        return r;
    }

    protected abstract R handleEvent(E event, Context context, O eventObserver);
}

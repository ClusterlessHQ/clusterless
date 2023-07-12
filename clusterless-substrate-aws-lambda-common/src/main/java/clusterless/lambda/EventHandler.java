/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.base.Stopwatch;

public abstract class EventHandler<E, O extends EventObserver> extends StreamHandler<E> {
    public EventHandler(Class<E> type) {
        super(type);
    }

    protected abstract O observer();

    @Override
    public void handleRequest(E event, Context context) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        logObjectInfo("incoming event: {}", event);

        handleEvent(event, context, observer());

        stopwatch.stop();

        LOG.info("event handled duration: {}", stopwatch.elapsed());
    }

    protected abstract void handleEvent(E event, Context context, O eventObserver);
}

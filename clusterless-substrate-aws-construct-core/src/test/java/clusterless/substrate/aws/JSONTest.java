/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.json.JSONUtil;
import clusterless.model.Model;
import clusterless.model.deploy.Extensible;
import clusterless.substrate.aws.arc.s3copy.S3CopyArc;
import clusterless.substrate.aws.boundary.s3put.S3PutListenerBoundary;
import clusterless.substrate.aws.resource.eventbridge.EventBridgeResource;
import clusterless.substrate.aws.resource.s3.S3BucketResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;

/**
 * Confirms we can write out a valid json and read it back into an object
 */
public class JSONTest {
    @ParameterizedTest
    @ValueSource(classes = {
            S3PutListenerBoundary.class,
            S3CopyArc.class,
            EventBridgeResource.class,
            S3BucketResource.class
    })
    void serialize(Class<? extends Extensible> originalClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Extensible original = originalClass.getConstructor().newInstance();

        Assertions.assertNotNull(original.type());
        Assertions.assertNotNull(original.modelType());

        String json = JSONUtil.writeAsPrettyStringSafe(original);

        Assertions.assertNotNull(json);

        Class<? extends Model> modelClass = original.modelType();

        Extensible copy = (Extensible) JSONUtil.readObjectSafe(json, modelClass);

        Assertions.assertEquals(original.getClass(), copy.getClass());
        Assertions.assertNotNull(copy.type());
        Assertions.assertNotNull(copy.modelType());
        Assertions.assertEquals(original.type(), copy.type());
    }
}

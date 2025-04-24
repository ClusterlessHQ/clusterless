/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.io;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.substrate.aws.sdk.S3;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

public class MetaReader<T> {
    protected final S3 s3 = new S3();
    protected final ObjectReader reader;

    public MetaReader(Class<T> type) {
        reader = JSONUtil.objectReaderFor(type);
    }

    public T retrieve(URI manifest) {
        S3.Response response = s3.get(manifest);

        if (!s3.exists(response)) {
            throw new IllegalStateException("manifest not found: " + manifest, response.exception());
        }

        try {
            return reader.readValue(response.asInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

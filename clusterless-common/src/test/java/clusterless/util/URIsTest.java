/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

/**
 *
 */
public class URIsTest {
    @Test
    void normalize() {
        Assertions.assertEquals("/", URIs.normalize("/"));
        Assertions.assertEquals("/", URIs.normalize("//"));
        Assertions.assertEquals("foo/", URIs.normalize("foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo/"));
        Assertions.assertEquals("", URIs.normalize(null));
    }

    @Test
    void prefix() {
        Assertions.assertEquals("foo", URIs.asPrefix(URI.create("/foo")));
        Assertions.assertEquals("foo/", URIs.asPrefix(URI.create("/foo/")));
        Assertions.assertEquals("foo/", URIs.asPrefix(URI.create("/foo//")));
        Assertions.assertEquals("foo/", URIs.asPrefix(URI.create("s3://bucket/foo//")));
        Assertions.assertEquals("foo/", URIs.asPrefix(URI.create("s3://bucket//foo//")));
        Assertions.assertNull(URIs.asPrefix(URI.create("/")));
        Assertions.assertNull(URIs.asPrefix(URI.create("/")));
        Assertions.assertNull(URIs.asPrefix(URI.create("s3://bucket")));
        Assertions.assertNull(URIs.asPrefix(URI.create("s3://bucket/")));
    }
}
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

    public static final String IS_NULL = null;

    @Test
    void normalize() {
        Assertions.assertEquals("/", URIs.normalize("/"));
        Assertions.assertEquals("/", URIs.normalize("//"));
        Assertions.assertEquals("foo/", URIs.normalize("foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo/"));
        Assertions.assertEquals("", URIs.normalize(IS_NULL));
    }

    @Test
    void normalizeAppend() {
        Assertions.assertEquals("/", URIs.normalize("/", IS_NULL));
        Assertions.assertEquals("/", URIs.normalize("//", IS_NULL));

        // this form is safe for requiring a root slash when generating a URI path
        Assertions.assertEquals("/foo/", URIs.normalize("/", "foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/", "/foo//"));

        Assertions.assertEquals("foo/", URIs.normalize("foo//", ""));
        Assertions.assertEquals("foo/bar", URIs.normalize("foo//", "bar"));
        Assertions.assertEquals("foo/bar/", URIs.normalize("foo//", "bar/"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo//"));
        Assertions.assertEquals("/foo/bar/", URIs.normalize("/foo/", "bar//"));
        Assertions.assertEquals("", URIs.normalize(IS_NULL, IS_NULL));
    }

    @Test
    void normalizeURI() {
        Assertions.assertEquals(URI.create("s3://bucket/"), URIs.normalizeURI(URI.create("s3://bucket/")));
        Assertions.assertEquals(URI.create("s3://bucket/"), URIs.normalizeURI(URI.create("s3://bucket//")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/"), URIs.normalizeURI(URI.create("s3://bucket/foo//")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/"), URIs.normalizeURI(URI.create("s3://bucket/foo/")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar"), URIs.normalizeURI(URI.create("s3://bucket/foo//bar")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar"), URIs.normalizeURI(URI.create("s3://bucket/foo/bar")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar/"), URIs.normalizeURI(URI.create("s3://bucket/foo//bar//")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar/"), URIs.normalizeURI(URI.create("s3://bucket/foo/bar/")));
    }

    @Test
    void prefix() {
        Assertions.assertEquals("foo", URIs.asKeyPath(URI.create("/foo")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("/foo/")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("/foo//")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("s3://bucket/foo//")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("s3://bucket//foo//")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("/")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("/")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("s3://bucket")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("s3://bucket/")));
    }
}

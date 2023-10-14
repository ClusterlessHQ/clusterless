/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PathMatcherTest {
    @Test
    void excludeAbsolute() {
        PathMatcher matcher = PathMatcher.builder()
                .withPath("/foo/")
                .withExcludes(List.of("/**/_*"))
                .build();

        Assertions.assertTrue(matcher.keep("/foo/bar.parquet"));
        Assertions.assertFalse(matcher.keep("/foo/_SUCCESS"));
        Assertions.assertFalse(matcher.keep("/foo/bar/_SUCCESS"));
    }

    @Test
    void excludeRelative() {
        PathMatcher matcher = PathMatcher.builder()
                .withPath("/foo/")
                .withExcludes(List.of("_*"))
                .build();

        Assertions.assertTrue(matcher.keep("/foo/bar.parquet"));
        Assertions.assertFalse(matcher.keep("/foo/_SUCCESS"));
    }

    @Test
    void excludeRelativeDeep() {
        PathMatcher matcher = PathMatcher.builder()
                .withPath("/foo/")
                .withExcludes(List.of("**/_*"))
                .build();

        Assertions.assertTrue(matcher.keep("/foo/a/b/c/bar.parquet"));
        Assertions.assertFalse(matcher.keep("/foo/a/b/c/_SUCCESS"));
    }
}

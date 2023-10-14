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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AntPathMatcherTest {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Test
    public void match() {
        // test exact matching
        assertTrue(pathMatcher.match("test", "test"));
        assertTrue(pathMatcher.match("/test", "/test"));
        // SPR-14141
        assertTrue(pathMatcher.match("https://example.org", "https://example.org"));
        Assertions.assertFalse(pathMatcher.match("/test.jpg", "test.jpg"));
        Assertions.assertFalse(pathMatcher.match("test", "/test"));
        Assertions.assertFalse(pathMatcher.match("/test", "test"));

        // test matching with ?'s
        assertTrue(pathMatcher.match("t?st", "test"));
        assertTrue(pathMatcher.match("??st", "test"));
        assertTrue(pathMatcher.match("tes?", "test"));
        assertTrue(pathMatcher.match("te??", "test"));
        assertTrue(pathMatcher.match("?es?", "test"));
        Assertions.assertFalse(pathMatcher.match("tes?", "tes"));
        Assertions.assertFalse(pathMatcher.match("tes?", "testt"));
        Assertions.assertFalse(pathMatcher.match("tes?", "tsst"));

        // test matching with *'s
        assertTrue(pathMatcher.match("*", "test"));
        assertTrue(pathMatcher.match("test*", "test"));
        assertTrue(pathMatcher.match("test*", "testTest"));
        assertTrue(pathMatcher.match("test/*", "test/Test"));
        assertTrue(pathMatcher.match("test/*", "test/t"));
        assertTrue(pathMatcher.match("test/*", "test/"));
        assertTrue(pathMatcher.match("*test*", "AnothertestTest"));
        assertTrue(pathMatcher.match("*test", "Anothertest"));
        assertTrue(pathMatcher.match("*.*", "test."));
        assertTrue(pathMatcher.match("*.*", "test.test"));
        assertTrue(pathMatcher.match("*.*", "test.test.test"));
        assertTrue(pathMatcher.match("test*aaa", "testblaaaa"));
        Assertions.assertFalse(pathMatcher.match("test*", "tst"));
        Assertions.assertFalse(pathMatcher.match("test*", "tsttest"));
        Assertions.assertFalse(pathMatcher.match("test*", "test/"));
        Assertions.assertFalse(pathMatcher.match("test*", "test/t"));
        Assertions.assertFalse(pathMatcher.match("test/*", "test"));
        Assertions.assertFalse(pathMatcher.match("*test*", "tsttst"));
        Assertions.assertFalse(pathMatcher.match("*test", "tsttst"));
        Assertions.assertFalse(pathMatcher.match("*.*", "tsttst"));
        Assertions.assertFalse(pathMatcher.match("test*aaa", "test"));
        Assertions.assertFalse(pathMatcher.match("test*aaa", "testblaaab"));

        // test matching with ?'s and /'s
        assertTrue(pathMatcher.match("/?", "/a"));
        assertTrue(pathMatcher.match("/?/a", "/a/a"));
        assertTrue(pathMatcher.match("/a/?", "/a/b"));
        assertTrue(pathMatcher.match("/??/a", "/aa/a"));
        assertTrue(pathMatcher.match("/a/??", "/a/bb"));
        assertTrue(pathMatcher.match("/?", "/a"));

        // test matching with **'s
        assertTrue(pathMatcher.match("/**", "/testing/testing"));
        assertTrue(pathMatcher.match("/*/**", "/testing/testing"));
        assertTrue(pathMatcher.match("/**/*", "/testing/testing"));
        assertTrue(pathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla"));
        assertTrue(pathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla/bla"));
        assertTrue(pathMatcher.match("/**/test", "/bla/bla/test"));
        assertTrue(pathMatcher.match("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla"));
        assertTrue(pathMatcher.match("/bla*bla/test", "/blaXXXbla/test"));
        assertTrue(pathMatcher.match("/*bla/test", "/XXXbla/test"));
        Assertions.assertFalse(pathMatcher.match("/bla*bla/test", "/blaXXXbl/test"));
        Assertions.assertFalse(pathMatcher.match("/*bla/test", "XXXblab/test"));
        Assertions.assertFalse(pathMatcher.match("/*bla/test", "XXXbl/test"));

        Assertions.assertFalse(pathMatcher.match("/????", "/bala/bla"));
        Assertions.assertFalse(pathMatcher.match("/**/*bla", "/bla/bla/bla/bbb"));

        assertTrue(pathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"));
        assertTrue(pathMatcher.match("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"));
        assertTrue(pathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"));
        assertTrue(pathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"));

        assertTrue(pathMatcher.match("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing/"));
        assertTrue(pathMatcher.match("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing"));
        assertTrue(pathMatcher.match("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing"));
        Assertions.assertFalse(pathMatcher.match("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing/testing"));

        Assertions.assertFalse(pathMatcher.match("/x/x/**/bla", "/x/x/x/"));

        assertTrue(pathMatcher.match("/foo/bar/**", "/foo/bar"));

        assertTrue(pathMatcher.match("", ""));

        assertTrue(pathMatcher.match("/{bla}.*", "/testing.html"));
    }

    @Test
    public void matchWithNullPath() {
        Assertions.assertFalse(pathMatcher.match("/test", null));
        Assertions.assertFalse(pathMatcher.match("/", null));
        Assertions.assertFalse(pathMatcher.match(null, null));
    }

    @Test
    public void defaultCacheSetting() {
        match();
        assertTrue(pathMatcher.stringMatcherCache.size() > 20);

        for (int i = 0; i < 65536; i++) {
            pathMatcher.match("test" + i, "test");
        }
        // Cache turned off because it went beyond the threshold
        assertTrue(pathMatcher.stringMatcherCache.isEmpty());
    }

    @Test
    public void cachePatternsSetToTrue() {
        pathMatcher.setCachePatterns(true);
        match();
        assertTrue(pathMatcher.stringMatcherCache.size() > 20);

        for (int i = 0; i < 65536; i++) {
            pathMatcher.match("test" + i, "test" + i);
        }
        // Cache keeps being alive due to the explicit cache setting
        assertTrue(pathMatcher.stringMatcherCache.size() > 65536);
    }

    @Test
    public void preventCreatingStringMatchersIfPathDoesNotStartsWithPatternPrefix() {
        pathMatcher.setCachePatterns(true);
        Assertions.assertEquals(0, pathMatcher.stringMatcherCache.size());

        pathMatcher.match("test?", "test");
        Assertions.assertEquals(1, pathMatcher.stringMatcherCache.size());

        pathMatcher.match("test?", "best");
        pathMatcher.match("test/*", "view/test.jpg");
        pathMatcher.match("test/**/test.jpg", "view/test.jpg");
        pathMatcher.match("test/{name}.jpg", "view/test.jpg");
        Assertions.assertEquals(1, pathMatcher.stringMatcherCache.size());
    }

    @Test
    public void creatingStringMatchersIfPatternPrefixCannotDetermineIfPathMatch() {
        pathMatcher.setCachePatterns(true);
        Assertions.assertEquals(0, pathMatcher.stringMatcherCache.size());

        pathMatcher.match("test", "testian");
        pathMatcher.match("test?", "testFf");
        pathMatcher.match("test/*", "test/dir/name.jpg");
        pathMatcher.match("test/{name}.jpg", "test/lorem.jpg");
        pathMatcher.match("bla/**/test.jpg", "bla/test.jpg");
        pathMatcher.match("**/{name}.jpg", "test/lorem.jpg");
        pathMatcher.match("/**/{name}.jpg", "/test/lorem.jpg");
        pathMatcher.match("/*/dir/{name}.jpg", "/*/dir/lorem.jpg");

        Assertions.assertEquals(7, pathMatcher.stringMatcherCache.size());
    }

    @Test
    public void cachePatternsSetToFalse() {
        pathMatcher.setCachePatterns(false);
        match();
        assertTrue(pathMatcher.stringMatcherCache.isEmpty());
    }

    /**
     * <a href="https://github.com/azagniotov/ant-style-path-matcher/issues/3">...</a>
     */
    @Test
    public void greedyMatch() {
        Assertions.assertFalse(pathMatcher.match("/organizations/*/fields", "/organizations/1/farms/2/fields"));
    }

    /**
     * <a href="https://github.com/azagniotov/ant-style-path-matcher/issues/2">...</a>
     */
    @Test
    void another() {
        Assertions.assertFalse(pathMatcher.match("*.txt", "path/my.txt"));
    }
}

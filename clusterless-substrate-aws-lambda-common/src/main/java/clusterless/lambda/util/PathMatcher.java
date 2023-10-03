/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PathMatcher {
    protected String path;
    protected char pathSeparator;
    protected List<String> includes;
    protected List<String> excludes;
    protected boolean ignoreCase;
    protected final Predicate<String> predicate;

    public PathMatcher(String path, List<String> includes, List<String> excludes, char pathSeparator, boolean ignoreCase) {
        this.path = path;
        this.includes = includes == null ? new ArrayList<>() : includes;
        this.excludes = excludes == null ? new ArrayList<>() : excludes;
        this.pathSeparator = pathSeparator == 0 ? '/' : pathSeparator;
        this.ignoreCase = ignoreCase;

        AntPathMatcher matcher = new AntPathMatcher(this.pathSeparator);

        matcher.setCaseSensitive(!this.ignoreCase);

        Predicate<String> include = this.includes.stream()
                .filter(s -> !s.isEmpty())
                .map(s -> predicate(s, matcher))
                .reduce(Predicate::or).orElse(null);

        Predicate<String> exclude = this.excludes.stream()
                .filter(s -> !s.isEmpty())
                .map(s -> predicate(s, matcher))
                .reduce(Predicate::or).orElse(null);

        if (include == null && exclude == null) {
            predicate = p -> true;
        } else if (include != null && exclude != null) {
            predicate = include.and(exclude.negate());
        } else if (include != null & exclude == null) {
            predicate = include;
        } else {
            predicate = exclude.negate();
        }
    }

    public static Builder builder() {
        return Builder.builder();
    }

    @NotNull
    private Predicate<String> predicate(String pattern, AntPathMatcher matcher) {
        if (pattern.charAt(0) == pathSeparator) {
            return path -> matcher.match(pattern, path);
        }

        return path -> matcher.match(pattern, path.subSequence(this.path.length(), path.length()));
    }

    public boolean keep(String path) {
        return predicate.test(path);
    }


    public static final class Builder {
        protected String path;
        protected char pathSeparator;
        protected List<String> includes;
        protected List<String> excludes;
        protected boolean ignoreCase;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withPathSeparator(char pathSeparator) {
            this.pathSeparator = pathSeparator;
            return this;
        }

        public Builder withIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public Builder withExcludes(List<String> excludes) {
            this.excludes = excludes;
            return this;
        }

        public Builder withIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        public PathMatcher build() {
            return new PathMatcher(path, includes, excludes, pathSeparator, ignoreCase);
        }
    }
}

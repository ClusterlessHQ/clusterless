/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.uri;

import clusterless.cls.util.Optionals;

import java.util.regex.Pattern;

public class BaseURI {

    protected enum Format {
        full(4),
        path(2),
        key(1);

        final int offset;

        Format(int offset) {
            this.offset = offset;
        }

        public int offset() {
            return offset;
        }
    }

    private static final Pattern COMPILE = Pattern.compile("^.+=");

    protected static String value(String[] split, int index) {
        return Optionals.optional(index, split)
                .map(s -> COMPILE.matcher(s).replaceAll(""))
                .filter(BaseURI::isNotTemplate)
                .orElse(null);
    }

    protected static boolean isNotTemplate(String s) {
        return !s.startsWith("{") || !s.endsWith("}");
    }


    protected static Format isOnlyPath(String root, String template) {
        if (template.charAt(0) == '/') return Format.path;

        if (template.startsWith(root)) return Format.key;

        return Format.full;
    }
}

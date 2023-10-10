/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.uri;

import java.util.regex.Pattern;

import static clusterless.util.Optionals.optional;

public class BaseURI {
    private static final Pattern COMPILE = Pattern.compile("^.+=");

    protected static String value(String[] split, int index) {
        return optional(index, split)
                .map(s -> COMPILE.matcher(s).replaceAll(""))
                .filter(BaseURI::isNotTemplate)
                .orElse(null);
    }

    protected static boolean isNotTemplate(String s) {
        return !s.startsWith("{") || !s.endsWith("}");
    }


    protected static boolean isOnlyPath(String template) {
        return template.charAt(0) == '/';
    }
}

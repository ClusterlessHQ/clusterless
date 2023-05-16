/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.naming;

import java.io.Serializable;

/**
 * Fixed is a Label that retains the value formatting. It won't be coerced into camelCase or
 * an alternative text format.
 * <p>
 * Fixed can be subclassed to provide type values that may optionally have formatting rules.
 * <p>
 * It is also Serializable so that subclasses may be used as value types.
 */
public class Fixed implements Label, Serializable {
    private static final Fixed NULL_FIXED = new Fixed(null);
    String value;

    public static Fixed fixedNull() {
        return NULL_FIXED;
    }

    public static Fixed of(String value) {
        return new Fixed(value);
    }

    protected Fixed(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String camelCase() {
        return value();
    }

    @Override
    public String lowerHyphen() {
        return value();
    }

    @Override
    public String lowerHyphenPath() {
        return value();
    }

    public String lowerColonPath() {
        return value();
    }

    @Override
    public String lowerUnderscore() {
        return value();
    }

    @Override
    public String upperUnderscore() {
        return value();
    }

    @Override
    public String shortLowerHyphen() {
        return value();
    }

    @Override
    public String shortLowerUnderscore() {
        return value();
    }

    @Override
    public String toString() {
        return value();
    }
}

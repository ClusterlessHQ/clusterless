/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static clusterless.managed.LabelTest.Value.Case;
import static clusterless.managed.LabelTest.Value.Lower;

/**
 *
 */
public class LabelTest {
    @Test
    void string() {
        Assertions.assertEquals("Lower", Label.of("lower").camelCase());
        Assertions.assertEquals("Lower", Label.fromLowerHyphen("lower").camelCase());
        Assertions.assertEquals("lower", Label.fromLowerHyphen("lower").lowerHyphen());
        Assertions.assertEquals("lower", Label.fromLowerHyphen("lower").lowerUnderscore());

        Label with = Label.of("lower").with(Label.of("case"));
        Assertions.assertEquals("LowerCase", with.camelCase());
        Assertions.assertEquals("lower-case", with.lowerHyphen());
        Assertions.assertEquals("lower_case", with.lowerUnderscore());

        Label withNull = Label.of(null).with(Label.of("lower")).with(Label.of("case"));
        Assertions.assertEquals("LowerCase", withNull.camelCase());
        Assertions.assertEquals("lower-case", withNull.lowerHyphen());
        Assertions.assertEquals("lower_case", withNull.lowerUnderscore());

        Label withMidNull = Label.of("lower").with(Label.of(null)).with(Label.of("case"));
        Assertions.assertEquals("LowerCase", withMidNull.camelCase());
        Assertions.assertEquals("lower-case", withMidNull.lowerHyphen());
        Assertions.assertEquals("lower_case", withMidNull.lowerUnderscore());

        Label withEndNull = Label.of("lower").with(Label.of("case")).with(Label.of(null));
        Assertions.assertEquals("LowerCase", withEndNull.camelCase());
        Assertions.assertEquals("lower-case", withEndNull.lowerHyphen());
        Assertions.assertEquals("lower_case", withEndNull.lowerUnderscore());

        Label abbr = Label.of("lower").abbreviated(Label.of("lwr")).with(Label.of("case").abbreviated(Label.of("cs")));
        Assertions.assertEquals("LowerCase", abbr.camelCase());
        Assertions.assertEquals("LwrCs", abbr.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr.shortLowerUnderscore());

        Label abbr2 = Label.of("lower", "lwr").with(Label.of("case", "cs"));
        Assertions.assertEquals("LowerCase", abbr2.camelCase());
        Assertions.assertEquals("LwrCs", abbr2.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr2.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr2.shortLowerUnderscore());
    }

    enum Value implements Label.EnumLabel {
        Lower("Lwr"),
        Case("Cs");

        private final String abbr;

        Value(String abbr) {
            this.abbr = abbr;
        }

        @Override
        public Label abbreviated() {
            return Label.of(abbr);
        }
    }

    @Test
    void stringWithUpper() {
        Assertions.assertEquals("LOWER", Label.of("lower").upperOnly().camelCase());

        Label with = Label.of("lower").upperOnly().with(Label.of("case"));
        Assertions.assertEquals("LOWERCase", with.camelCase());
        Assertions.assertEquals("LOWER-case", with.lowerHyphen());
        Assertions.assertEquals("LOWER_case", with.lowerUnderscore());
    }

    @Test
    void enumeration() {
        Assertions.assertEquals("Lower", Label.of(Lower).camelCase());
        Assertions.assertEquals("Lwr", Label.of(Lower).shortCamelCase());

        Assertions.assertEquals("LowerCase", Label.of(Lower).with(Case).camelCase());
        Assertions.assertEquals("lwr-cs", Label.of(Lower).with(Case).shortLowerHyphen());
    }

    @Test
    void enumerationWithNull() {
        Assertions.assertEquals("LowerCase", Label.of(null).with(Lower).with(Case).camelCase());
        Assertions.assertEquals("lwr-cs", Label.of(null).with(Lower).with(Case).shortLowerHyphen());

        Assertions.assertEquals("LowerCase", Label.of(Lower).with(null).with(Case).camelCase());
        Assertions.assertEquals("lwr-cs", Label.of(Lower).with(null).with(Case).shortLowerHyphen());
    }
}

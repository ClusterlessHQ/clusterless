/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.naming;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static clusterless.cls.naming.LabelTest.Value.Case;
import static clusterless.cls.naming.LabelTest.Value.Lower;

/**
 *
 */
public class LabelTest {
    @Test
    void string() {
        Assertions.assertEquals("Lower", Label.of("lower").camelCase());
        Assertions.assertEquals("lower", Label.of("lower").lowerCamelCase());
        Assertions.assertEquals("Lower", Label.fromLowerHyphen("lower").camelCase());
        Assertions.assertEquals("lower", Label.fromLowerHyphen("lower").lowerHyphen());
        Assertions.assertEquals("lower", Label.fromLowerHyphen("lower").lowerHyphenPath());
        Assertions.assertEquals("lower/", Label.fromLowerHyphen("lower").lowerHyphenPath(true));
        Assertions.assertEquals("lower", Label.fromLowerHyphen("lower").lowerUnderscore());

        Label with = Label.of("lower").with(Label.of("case"));
        Assertions.assertEquals("LowerCase", with.camelCase());
        Assertions.assertEquals("lowerCase", with.lowerCamelCase());
        Assertions.assertEquals("lower-case", with.lowerHyphen());
        Assertions.assertEquals("lower/case", with.lowerHyphenPath());
        Assertions.assertEquals("lower/case/", with.lowerHyphenPath(true));
        Assertions.assertEquals("lower_case", with.lowerUnderscore());

        Label withCamel = Label.of("LowerCase").with(Label.of("Word"));
        Assertions.assertEquals("LowerCaseWord", withCamel.camelCase());
        Assertions.assertEquals("lowerCaseWord", withCamel.lowerCamelCase());
        Assertions.assertEquals("lower-case-word", withCamel.lowerHyphen());
        Assertions.assertEquals("lower-case/word", withCamel.lowerHyphenPath());
        Assertions.assertEquals("lower-case/word/", withCamel.lowerHyphenPath(true));
        Assertions.assertEquals("lower_case_word", withCamel.lowerUnderscore());

        Label withLongCamel = Label.of("Lower").with("Case").with(Label.of("Word"));
        Assertions.assertEquals("LowerCaseWord", withLongCamel.camelCase());
        Assertions.assertEquals("lowerCaseWord", withLongCamel.lowerCamelCase());
        Assertions.assertEquals("lower-case-word", withLongCamel.lowerHyphen());
        Assertions.assertEquals("lower/case/word", withLongCamel.lowerHyphenPath());
        Assertions.assertEquals("lower/case/word/", withLongCamel.lowerHyphenPath(true));
        Assertions.assertEquals("lower_case_word", withLongCamel.lowerUnderscore());

        Label withNull = Label.of(null).with(Label.of("lower")).with(Label.of("case"));
        Assertions.assertEquals("LowerCase", withNull.camelCase());
        Assertions.assertEquals("lowerCase", withNull.lowerCamelCase());
        Assertions.assertEquals("lower-case", withNull.lowerHyphen());
        Assertions.assertEquals("lower/case", withNull.lowerHyphenPath());
        Assertions.assertEquals("lower/case/", withNull.lowerHyphenPath(true));
        Assertions.assertEquals("lower_case", withNull.lowerUnderscore());

        Label withMidNull = Label.of("lower").with(Label.of(null)).with(Label.of("case"));
        Assertions.assertEquals("LowerCase", withMidNull.camelCase());
        Assertions.assertEquals("lowerCase", withMidNull.lowerCamelCase());
        Assertions.assertEquals("lower-case", withMidNull.lowerHyphen());
        Assertions.assertEquals("lower/case", withMidNull.lowerHyphenPath());
        Assertions.assertEquals("lower/case/", withMidNull.lowerHyphenPath(true));
        Assertions.assertEquals("lower_case", withMidNull.lowerUnderscore());

        Label withEndNull = Label.of("lower").with(Label.of("case")).with(Label.of(null));
        Assertions.assertEquals("LowerCase", withEndNull.camelCase());
        Assertions.assertEquals("lowerCase", withEndNull.lowerCamelCase());
        Assertions.assertEquals("lower-case", withEndNull.lowerHyphen());
        Assertions.assertEquals("lower/case", withEndNull.lowerHyphenPath());
        Assertions.assertEquals("lower/case/", withEndNull.lowerHyphenPath(true));
        Assertions.assertEquals("lower_case", withEndNull.lowerUnderscore());

        Label abbr = Label.of("lower").abbreviated(Label.of("lwr")).with(Label.of("case").abbreviated(Label.of("cs")));
        Assertions.assertEquals("LowerCase", abbr.camelCase());
        Assertions.assertEquals("lowerCase", abbr.lowerCamelCase());
        Assertions.assertEquals("LwrCs", abbr.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr.shortLowerUnderscore());

        Label abbr2 = Label.of("lower", "lwr").with(Label.of("case", "cs"));
        Assertions.assertEquals("LowerCase", abbr2.camelCase());
        Assertions.assertEquals("lowerCase", abbr2.lowerCamelCase());
        Assertions.assertEquals("LwrCs", abbr2.shortCamelCase());
        Assertions.assertEquals("lwr-cs", abbr2.shortLowerHyphen());
        Assertions.assertEquals("lwr_cs", abbr2.shortLowerUnderscore());
    }

    @Test
    void stringWithUpper() {
        Assertions.assertEquals("LOWER", Label.of("lower").upperOnly().camelCase());

        Label with = Label.of("lower").upperOnly().with(Label.of("case"));
        Assertions.assertEquals("LOWERCase", with.camelCase());
        Assertions.assertEquals("LOWER-case", with.lowerHyphen());
        Assertions.assertEquals("LOWER/case", with.lowerHyphenPath());
        Assertions.assertEquals("LOWER/case/", with.lowerHyphenPath(true));
        Assertions.assertEquals("LOWER_case", with.lowerUnderscore());
    }

    @Test
    void stringWithFixed() {
        Assertions.assertEquals("LoWeR", Label.fixed("LoWeR").camelCase());

        Label with = Label.fixed("LoWeR").with(Label.of("case"));
        Assertions.assertEquals("LoWeRCase", with.camelCase());
        Assertions.assertEquals("LoWeR-case", with.lowerHyphen());
        Assertions.assertEquals("LoWeR/case", with.lowerHyphenPath());
        Assertions.assertEquals("LoWeR/case/", with.lowerHyphenPath(true));
        Assertions.assertEquals("LoWeR_case", with.lowerUnderscore());

        with = Label.of("prefix").with(Label.fixed("LoWeR")).with(Label.of("case"));
        Assertions.assertEquals("PrefixLoWeRCase", with.camelCase());
        Assertions.assertEquals("prefix-LoWeR-case", with.lowerHyphen());
        Assertions.assertEquals("prefix/LoWeR/case", with.lowerHyphenPath());
        Assertions.assertEquals("prefix/LoWeR/case/", with.lowerHyphenPath(true));
        Assertions.assertEquals("prefix_LoWeR_case", with.lowerUnderscore());
    }

    @Test
    void having() {
        Label with = Label.of("lower").having("one", "two", "three");

        Assertions.assertEquals("LowerOneTwoThree", with.camelCase());
        Assertions.assertEquals("lower-one-two-three", with.lowerHyphen());
        Assertions.assertEquals("lower/one/two/three", with.lowerHyphenPath());
        Assertions.assertEquals("lower/one/two/three/", with.lowerHyphenPath(true));
        Assertions.assertEquals("lower_one_two_three", with.lowerUnderscore());
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

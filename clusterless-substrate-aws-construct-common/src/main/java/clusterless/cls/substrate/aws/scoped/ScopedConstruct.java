/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.scoped;

import clusterless.cls.naming.Ref;
import clusterless.cls.substrate.aws.naming.ArnRefs;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

import java.util.Optional;
import java.util.function.Function;

public class ScopedConstruct extends Construct {
    public ScopedConstruct(@NotNull Construct scope, @NotNull String id) {
        super(scope, id);
    }

    protected void addArnRefFor(Ref ref, Construct construct, String value, String description) {
        ScopedStack.scopedOf(this)
                .addArnRef(ref, construct, value, description);
    }

    protected void addIdRefFor(Ref ref, Construct construct, String value, String description) {
        ScopedStack.scopedOf(this).addIdRefFor(ref, construct, value, description);
    }

    protected void addNameRefFor(Ref ref, Construct construct, String value, String description) {
        ScopedStack.scopedOf(this)
                .addNameRefFor(ref, construct, value, description);
    }

    protected <T> T resolveArnRef(String ref, Function<String, T> resolver) {
        Construct construct = ScopedApp.stagedOf(this).resolveRef(ref);

        if (construct != null) {
            return (T) construct;
        }

        Optional<String> arn = ArnRefs.resolveArn(this, ref);

        return resolver.apply(arn.orElseThrow(() -> new IllegalArgumentException("ref or arn are required" + ref)));
    }
}

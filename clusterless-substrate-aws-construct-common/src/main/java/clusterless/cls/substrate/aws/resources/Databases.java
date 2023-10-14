/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import software.amazon.awscdk.Arn;
import software.amazon.awscdk.ArnComponents;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Databases {
    public static String catalogFrom(URI uri) {
        validate(uri);

        return uri.getHost();
    }

    public static String databaseFrom(URI uri) {
        Path path = validate(uri);

        return path.getName(0).getFileName().toString();
    }

    public static String tableFrom(URI uri) {
        Path path = validate(uri);

        return path.getName(1).getFileName().toString();
    }

    public static Path validate(URI uri) {
        if (!uri.getScheme().equals("glue")) {
            throw new IllegalArgumentException("URI must be a glue database URI: glue://[catalog]/database/table[/...], got: " + uri);
        }

        Path path = Paths.get(uri.getPath());

        if (path.getNameCount() < 2) {
            throw new IllegalArgumentException("URI must be a glue database URI: glue://[catalog]/database/table[/...], got: " + uri);
        }

        return path;
    }

    /**
     * "arn:aws:glue:us-west-2:123456789012:catalog",
     * arn:{partition}:{service}:{region}:{account}:{resource}{sep}{resource-name}
     *
     * @param uri
     * @return
     */
    public static String catalogARNFrom(Construct scope, URI uri) {
        Stack stack = Stack.of(scope);
        ArnComponents components = ArnComponents.builder()
                .service("glue")
                .resource(Optional.ofNullable(catalogFrom(uri)).orElse("catalog"))
                .build();

        return Arn.format(components, stack);
    }

    /**
     * "arn:aws:glue:us-west-2:123456789012:database/db1",
     *
     * @param glueAddPartitionsArcConstruct
     * @param glueUri
     * @return
     */
    public static String databaseARNFrom(Construct scope, URI uri) {
        Stack stack = Stack.of(scope);
        ArnComponents components = ArnComponents.builder()
                .service("glue")
                .resource("database")
                .resourceName(databaseFrom(uri))
                .build();

        return Arn.format(components, stack);
    }

    /**
     * "arn:aws:glue:us-west-2:123456789012:table/db1/tbl1",")
     *
     * @param glueAddPartitionsArcConstruct
     * @param glueUri
     * @return
     */
    public static String tableARNFrom(Construct scope, URI uri) {
        Stack stack = Stack.of(scope);
        ArnComponents components = ArnComponents.builder()
                .service("glue")
                .resource("table")
                .resourceName("%s/%s".formatted(databaseFrom(uri), tableFrom(uri)))
                .build();

        return Arn.format(components, stack);
    }
}

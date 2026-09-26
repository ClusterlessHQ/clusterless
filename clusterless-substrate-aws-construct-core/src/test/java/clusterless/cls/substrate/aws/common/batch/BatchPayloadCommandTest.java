/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.common.batch;

import com.jayway.jsonpath.PathNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pins how batch commands substitute {@code $}, {@code $.}-paths (arc exec context), and
 * {@code $$.}-paths (step context), so a json-path upgrade cannot change Batch job payloads silently.
 */
public class BatchPayloadCommandTest {
    static final List<String> DECLARED = List.of(
            "run",
            "$",
            "$.role.name",
            "$$.Execution.Id",
            "$HOME",
            "literal"
    );

    @Test
    void rewritesPathArgumentsToParameterRefs() {
        BatchPayloadCommand command = new BatchPayloadCommand(DECLARED);

        Assertions.assertEquals(DECLARED, command.declared());

        Assertions.assertEquals(
                List.of("run", "Ref::param_0", "Ref::param_1", "Ref::param_2", "$HOME", "literal"),
                command.command()
        );

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("param_0", "$");
        expected.put("param_1", "$.role.name");
        expected.put("param_2", "$$.Execution.Id");

        Assertions.assertEquals(List.copyOf(expected.entrySet()), List.copyOf(command.payload().entrySet()));
    }

    @Test
    void fillsFromArcAndStepContext() {
        String arcExecContext = "{\"role\":{\"name\":\"etl\"},\"lot\":\"20230101\"}";
        String stepContext = "{\"Execution\":{\"Id\":\"exec-1\"}}";

        BatchPayloadCommand command = new BatchPayloadCommand(DECLARED);

        Assertions.assertEquals(
                List.of("run", arcExecContext, "etl", "exec-1", "$HOME", "literal"),
                command.fillWithArcContext(stepContext, arcExecContext)
        );
    }

    @Test
    void missingPathThrows() {
        BatchPayloadCommand command = new BatchPayloadCommand(List.of("$.nope"));

        Assertions.assertThrows(PathNotFoundException.class, () -> command.fillWithArcContext("{}", "{}"));
    }
}

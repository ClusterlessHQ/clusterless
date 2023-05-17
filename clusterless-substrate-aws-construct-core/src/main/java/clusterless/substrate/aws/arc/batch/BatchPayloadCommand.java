/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import com.jayway.jsonpath.JsonPath;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class BatchPayloadCommand {
    private final List<String> declared;
    List<String> command = new LinkedList<>();
    Map<String, String> payload = new LinkedHashMap<>();

    public BatchPayloadCommand(List<String> declared) {
        this.declared = declared;
        int count = 0;

        init(declared, count);
    }

    public List<String> declared() {
        return declared;
    }

    public List<String> command() {
        return command;
    }

    public Map<String, String> payload() {
        return payload;
    }

    private void init(List<String> declared, int count) {
        for (String arg : declared) {
            if (arg.equals("$") || arg.startsWith("$.") || arg.startsWith("$$.")) {
                String parameter = String.format("param_%d", count++);

                payload.put(parameter, arg);

                arg = String.format("Ref::%s", parameter);
            }

            command.add(arg);
        }
    }

    public List<String> fill(String stepContext, String arcExecContext) {
        List<String> results = new LinkedList<>();

        for (String arg : declared()) {
            if (arg.equals("$")) {
                results.add(arcExecContext);
            } else if (arg.startsWith("$.")) {
                results.add(JsonPath.read(arcExecContext, arg));
            } else if (arg.startsWith("$$.")) {
                results.add(JsonPath.read(stepContext, arg.substring(1)));
            } else {
                results.add(arg);
            }
        }

        return results;
    }
}

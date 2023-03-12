/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import software.amazon.awscdk.services.stepfunctions.*;
import software.constructs.Construct;

import java.util.List;

/**
 *
 */
public interface Orchestration {

    default CatchProps catchAll() {
        return CatchProps.builder()
                .resultPath(JsonPath.DISCARD)
                .errors(List.of("States.ALL"))
                .build();
    }

    default Succeed succeed(String id) {
        return Succeed.Builder.create((Construct) this, id)
                .build();
    }

    default Pass pass(String id) {
        return Pass.Builder.create((Construct) this, id)
                .build();
    }

    default Pass pass(String id, Result result) {
        return Pass.Builder.create((Construct) this, id)
                .result(result)
                .build();
    }

    default Fail fail(String id, String cause) {
        return Fail.Builder.create((Construct) this, id)
                .cause(cause)
                .build();
    }
}

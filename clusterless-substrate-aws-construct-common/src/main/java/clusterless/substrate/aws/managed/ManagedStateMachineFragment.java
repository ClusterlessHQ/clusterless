/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.stepfunctions.INextable;
import software.amazon.awscdk.services.stepfunctions.State;
import software.amazon.awscdk.services.stepfunctions.StateMachineFragment;

import java.util.List;

/**
 *
 */
public class ManagedStateMachineFragment extends StateMachineFragment implements Managed {
    private final ManagedComponentContext context;

    private State startState;
    private List<INextable> endStates;

    public ManagedStateMachineFragment(@NotNull ManagedComponentContext context, Label baseId) {
        super(context.parentConstruct(), baseId.camelCase());
        this.context = context;
    }

    public ManagedComponentContext context() {
        return context;
    }

    public ManagedStateMachineFragment setStartState(State startState) {
        this.startState = startState;
        return this;
    }

    public ManagedStateMachineFragment setEndStates(List<INextable> endStates) {
        this.endStates = endStates;
        return this;
    }

    @Override
    public @NotNull State getStartState() {
        return startState;
    }

    @Override
    public @NotNull List<INextable> getEndStates() {
        return endStates;
    }
}

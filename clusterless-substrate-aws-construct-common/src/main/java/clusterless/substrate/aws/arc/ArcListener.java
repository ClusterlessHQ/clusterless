/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.model.deploy.Workload;
import clusterless.naming.Label;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.resources.Arcs;
import clusterless.substrate.aws.resources.Events;
import clusterless.util.OrderedSafeMaps;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.events.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class ArcListener extends ManagedConstruct {

    private final Label ruleName;
    private final Rule rule;

    public ArcListener(@NotNull ManagedComponentContext context, @NotNull Arc<? extends Workload<?>> arc, boolean enabled) {
        super(context, Label.of(arc.name()).with("Listener"));

        List<String> ids = arc.sources().values()
                .stream()
                .filter(SourceDataset::subscribe) // only listen for those subscribed too
                .map(Dataset::id)
                .collect(Collectors.toList());

        Map<String, Object> detail = OrderedSafeMaps.of(
                ArcNotifyEvent.DATASET_ID, ids
        );

        EventPattern eventPattern = EventPattern.builder()
                .source(List.of(ArcNotifyEvent.SOURCE))
                .detailType(List.of(ArcNotifyEvent.DETAIL))
                .detail(detail)
                .build();

        ruleName = Arcs.arcBaseName(context().deployable(), arc).with("Rule");

        String eventBusName = Events.arcEventBusNameRef(this);
        IEventBus eventBus = EventBus.fromEventBusName(this, "EventBus", eventBusName);

        rule = Rule.Builder.create(this, ruleName.camelCase())
                .eventBus(eventBus)
                .ruleName(ruleName.lowerHyphen())
                .eventPattern(eventPattern)
                .enabled(enabled)
                .build();
    }

    public Label ruleName() {
        return ruleName;
    }

    public Rule rule() {
        return rule;
    }

    public void addTarget(@NotNull IRuleTarget target) {
        rule().addTarget(target);
    }
}

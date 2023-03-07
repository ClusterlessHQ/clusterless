package clusterless.lambda.arc;

import clusterless.model.deploy.Dataset;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.sdk.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class ArcNotifyEventManager {
    private static final Logger LOG = LogManager.getLogger(ArcNotifyEventManager.class);
    protected static final EventBus eventBus = new EventBus();
    private final String eventBusName;
    private final Dataset dataset;

    public ArcNotifyEventManager(String eventBusName, Dataset dataset) {
        this.eventBusName = eventBusName;
        this.dataset = dataset;
    }

    public void publishEvent(String lotId, URI manifestURI) {
        // publish notification on event-bus
        ArcNotifyEvent notifyEvent = ArcNotifyEvent.Builder.builder()
                .withDataset(dataset)
                .withLotId(lotId)
                .withManifest(manifestURI)
                .build();

        LOG.info("publishing {} on {}", () -> notifyEvent.getClass().getSimpleName(), () -> eventBusName);

        EventBus.Response response = eventBus.put(eventBusName, notifyEvent);

        if (!response.isSuccess()) {
            String message = String.format("unable to publish event: %s, %s", eventBusName, response.errorMessage());
            LOG.error(message, response.errorMessage());

            throw new RuntimeException(message, response.exception());
        }
    }
}

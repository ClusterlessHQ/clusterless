package clusterless.lambda;

import com.amazonaws.services.lambda.runtime.Context;

public abstract class EventHandler<E, C extends EventContext> extends StreamHandler<E> {
    public EventHandler(Class<E> type) {
        super(type);
    }

    protected abstract C createEventContext();

    @Override
    public void handleRequest(E event, Context context) {

        logObject("incoming event: {}", event);

        C eventContext = createEventContext();

        handleEvent(event, context, eventContext);
    }

    protected abstract void handleEvent(E event, Context context, C eventContext);
}

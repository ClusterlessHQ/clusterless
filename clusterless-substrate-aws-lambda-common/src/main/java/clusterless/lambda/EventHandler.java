package clusterless.lambda;

import com.amazonaws.services.lambda.runtime.Context;

public abstract class EventHandler<E, O extends EventObserver> extends StreamHandler<E> {
    public EventHandler(Class<E> type) {
        super(type);
    }

    protected abstract O observer();

    @Override
    public void handleRequest(E event, Context context) {
        logObject("incoming event: {}", event);

        handleEvent(event, context, observer());
    }

    protected abstract void handleEvent(E event, Context context, O eventObserver);
}

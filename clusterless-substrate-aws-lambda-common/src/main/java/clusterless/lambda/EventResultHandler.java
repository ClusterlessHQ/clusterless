package clusterless.lambda;

import clusterless.json.JSONUtil;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.event.ArcStateContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.MapType;

import java.util.LinkedHashMap;

public abstract class EventResultHandler<E, R, O extends EventObserver> extends StreamResultHandler<E, R> {
    public EventResultHandler(Class<E> incoming, Class<R> outgoing) {
        super(incoming, outgoing);
    }

    public EventResultHandler(Class<ArcStateContext> incoming, JavaType outgoing) {
        super(incoming, outgoing);
    }

    public static MapType getMapTypeFor(Class<String> keyClass, Class<ManifestState> valueClass) {
        return JSONUtil.OBJECT_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, keyClass, valueClass);
    }

    protected abstract O observer();

    @Override
    public R handleRequest(E event, Context context) {
        logObject("incoming event: {}", event);

        R r = null;
        try {
            r = handleEvent(event, context, observer());
        } catch (Exception e) {
            logErrorAndThrow(m -> new RuntimeException(m, e), "failed executing handler: {}, with: {}", getClass().getName(), e.getMessage());
        }

        logObject("outgoing object: {}", r);

        return r;
    }

    protected abstract R handleEvent(E event, Context context, O eventObserver);
}

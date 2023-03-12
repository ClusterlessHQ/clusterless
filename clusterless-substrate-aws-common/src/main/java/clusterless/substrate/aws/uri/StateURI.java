package clusterless.substrate.aws.uri;

import clusterless.model.State;
import clusterless.model.Struct;
import clusterless.model.deploy.Placement;
import clusterless.substrate.aws.store.StateStore;
import clusterless.substrate.aws.store.Stores;
import clusterless.util.Lazy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static clusterless.util.Optionals.optional;

public abstract class StateURI<S extends State, T extends StateURI<S, T>> implements Struct {
    private static final Pattern COMPILE = Pattern.compile("^.+=");
    protected StateStore stateStore;
    protected Placement placement;
    protected String lotId;
    protected S state;
    protected Supplier<String> storeName = Lazy.of(this::storeName);

    public StateURI(StateURI<S, T> other) {
        this.stateStore = other.stateStore;
        this.placement = other.placement;
        this.lotId = other.lotId;
        this.state = other.state;
        this.storeName = other.storeName;
    }

    protected StateURI(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    protected static void require(boolean wrong, String message) {
        if (wrong) {
            throw new IllegalStateException(message);
        }
    }

    protected abstract T copy();

    protected T setStoreName(String storeName) {
        this.storeName = () -> storeName;
        return self();
    }

    protected T setPlacement(Placement placement) {
        this.placement = placement;
        return self();
    }

    protected T setLotId(String lotId) {
        this.lotId = lotId;
        return self();
    }

    protected T setState(S state) {
        this.state = state;
        return self();
    }

    protected abstract T self();

    public T withLot(String lotId) {
        return copy().setLotId(lotId);
    }

    public T withState(S state) {
        return copy().setState(state);
    }

    protected String storeName() {
        require(stateStore, "stateBucket");
        require(placement, "placement");

        return Stores.bootstrapStoreName(stateStore, placement);
    }

    public abstract boolean isPath();

    public boolean isIdentifier() {
        return !isPath();
    }

    public abstract URI uri();


    public abstract String template();

    protected static String value(String[] split, int index) {
        return optional(index, split)
                .map(s -> COMPILE.matcher(s).replaceAll(""))
                .filter(ArcURI::isNotTemplate)
                .orElse(null);
    }

    protected static boolean isNotTemplate(String s) {
        return !s.startsWith("{") || !s.endsWith("}");
    }

    protected URI createUri(String path) {
        try {
            return new URI("s3", storeName.get(), path, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to create uri", e);
        }
    }

    protected void require(Object object, String message) {
        if (object == null) {
            throw new IllegalStateException(message + " is required");
        }
    }

    @Override
    public String toString() {
        return uri().toString();
    }
}

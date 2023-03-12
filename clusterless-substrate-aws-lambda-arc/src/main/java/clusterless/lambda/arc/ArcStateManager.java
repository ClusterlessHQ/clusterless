package clusterless.lambda.arc;

import clusterless.model.state.ArcState;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.aws.uri.ArcURI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArcStateManager {
    private static final Logger LOG = LogManager.getLogger(ArcStateManager.class);
    private final S3 s3 = new S3();

    final ArcURI arcStatePath;

    public ArcStateManager(ArcURI arcStatePath) {
        Objects.requireNonNull(arcStatePath, "arcStatePath");
        this.arcStatePath = arcStatePath;

        if (this.arcStatePath.isIdentifier()) {
            throw new IllegalStateException("arc state path must be a path uri, got: " + arcStatePath.uri());
        }
    }

    /**
     * @param lotId
     * @return
     */
    public Optional<ArcState> setStateFor(String lotId, ArcState newState) {
        Optional<ArcState> currentState = findStateFor(lotId);

        if (currentState.isPresent() && currentState.get() == newState) {
            throw new IllegalStateException("already in current state: " + newState);
        }

        LOG.info("found current state: {}", currentState.orElse(null));

        //  s3://state_bucket/{project}/{version}/{arc}/{lot}/{state}.txt
        URI currentStateIdentifier = currentState.map(s -> arcStatePath.withLot(lotId).withState(s).uri()).orElse(null);
        URI newStateIdentifier = arcStatePath.withLot(lotId).withState(newState).uri();

        LOG.info("setting arc state to: {}", newStateIdentifier);

        if (currentStateIdentifier == null) {
            s3.put(newStateIdentifier, "application/txt", "").
                    isSuccessOrThrowRuntime();
        } else {
            s3.move(currentStateIdentifier, newStateIdentifier).
                    isSuccessOrThrowRuntime();
        }

        return currentState;
    }

    public Optional<ArcState> findStateFor(String lotId) {
        //  s3://state_bucket/{project}/{version}/{arc}/{lot}/
        URI path = arcStatePath.withLot(lotId).uri();

        LOG.info("listing state paths for: {}", path);

        S3.Response response = s3.listPath(path);

        if (!response.isSuccess()) {
            return Optional.empty();
        }

        List<String> paths = s3.listPath(response);

        LinkedList<ArcState> states = paths.stream()
                .map(this::resolve)
                .collect(Collectors.toCollection(LinkedList::new));

        LOG.info("found states: {}", states);

        if (states.size() > 1) {
            throw new IllegalStateException("found more than one arc state: " + states);
        }

        if (states.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(states.getFirst());
    }

    private ArcState resolve(String s) {
        for (ArcState value : ArcState.values()) {
            if (s.contains(value.name())) {
                return value;
            }
        }
        return null;
    }
}

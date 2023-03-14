package clusterless.lambda;

import clusterless.lambda.arc.ArcStateManager;
import clusterless.lambda.arc.ArcStateProps;
import clusterless.model.state.ArcState;
import clusterless.substrate.aws.uri.ArcURI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class ArcStateManagerTest extends LocalStackBase {

    @Override
    protected ArcStateProps getProps() {
        return ArcStateProps.builder()
                .withArcStatePath(
                        ArcURI.builder()
                                .withPlacement(defaultPlacement())
                                .withProject(defaultProject())
                                .withArcName("test-arc")
                                .build()
                )
                .build();
    }

    /**
     * Embedded in a single test as we don't need to initialize the state between runs
     */
    @Test
    void states() {
        ArcStateManager arcStateManager = new ArcStateManager(getProps().arcStatePath());

        new TestLots().lotStream(5).forEach(lot -> {
            testStateTransition(arcStateManager, null, ArcState.running, ArcState.complete, lot);
            testStateTransition(arcStateManager, ArcState.complete, ArcState.partial, ArcState.missing, lot);
            testStateTransition(arcStateManager, ArcState.missing, ArcState.running, ArcState.partial, lot);
        });
    }

    private static void testStateTransition(ArcStateManager arcStateManager, ArcState originalState, ArcState initialState, ArcState nextState, String lotId) {
        Optional<ArcState> oldState = arcStateManager.setStateFor(lotId, initialState);
        Assertions.assertEquals(originalState, oldState.orElse(null));

        Optional<ArcState> resultState = arcStateManager.findStateFor(lotId);

        Assertions.assertTrue(resultState.isPresent());
        Assertions.assertEquals(initialState, resultState.get());

        resultState = arcStateManager.setStateFor(lotId, nextState);
        Assertions.assertTrue(resultState.isPresent());
        Assertions.assertEquals(initialState, resultState.get());
    }
}

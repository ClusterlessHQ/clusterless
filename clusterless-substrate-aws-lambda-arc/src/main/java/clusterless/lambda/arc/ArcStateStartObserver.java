package clusterless.lambda.arc;

import clusterless.lambda.EventObserver;
import clusterless.model.state.ArcState;

import java.util.List;

public interface ArcStateStartObserver extends EventObserver {
    void applyCurrentState(String lotId, ArcState currentState);

    void applyFinalArcStates(ArcState previous, ArcState current);

    void applyRoles(List<String> roles);
}

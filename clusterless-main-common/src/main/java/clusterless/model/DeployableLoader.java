package clusterless.model;

import clusterless.model.deploy.Deployable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DeployableLoader extends Loader {
    public DeployableLoader(List<File> projectFiles) throws IOException {
        super(projectFiles);
    }

    public List<Deployable> readObjects(String provider) {
        return super.readObjects(provider, Deployable.PROVIDER_POINTER, Deployable.class, Deployable::setSourceFile);
    }
}

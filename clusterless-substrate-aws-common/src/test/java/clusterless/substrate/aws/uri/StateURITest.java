package clusterless.substrate.aws.uri;

import clusterless.json.JSONUtil;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Project;
import clusterless.model.manifest.ManifestState;
import clusterless.model.state.ArcState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class StateURITest {
    @Test
    void arcState() {
        ArcURI arcState = ArcURI.builder()
                .withPlacement(Placement.Builder.builder()
                        .withAccount("00000000")
                        .withRegion("us-west-2")
                        .withProvider("aws")
                        .withStage("prod")
                        .build())
                .withProject(Project.Builder.builder()
                        .withName("test-project")
                        .withVersion("20230101")
                        .build())
                .withArcName("arc1")
                .build();

        Assertions.assertTrue(arcState.isPath());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/"), arcState.uri());

        String template = arcState.template();
        Assertions.assertEquals("s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/lot={lot}/{state}.arc", template);

        ArcURI parsedArcState = ArcURI.parse(template);
        Assertions.assertEquals(arcState.uri(), parsedArcState.uri());

        arcState = arcState.withLot("20211112PT5M000");

        Assertions.assertTrue(arcState.isPath());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/lot=20211112PT5M000/"), arcState.uri());

        arcState = arcState.withState(ArcState.running);

        Assertions.assertTrue(arcState.isIdentifier());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/lot=20211112PT5M000/running.arc"), arcState.uri());

        arcState = arcState.withState(ArcState.complete);

        Assertions.assertTrue(arcState.isIdentifier());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/lot=20211112PT5M000/complete.arc"), arcState.uri());

        template = arcState.template();
        Assertions.assertEquals("s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/lot=20211112PT5M000/complete.arc", template);

        parsedArcState = ArcURI.parse(template);
        Assertions.assertEquals(arcState.uri(), parsedArcState.uri());

        String jsonFromRaw = JSONUtil.writeAsStringSafe(arcState);

        Assertions.assertEquals("\"s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/lot=20211112PT5M000/complete.arc\"", jsonFromRaw);

        String jsonFromParsed = JSONUtil.writeAsStringSafe(parsedArcState);
        Assertions.assertEquals("\"s3://prod-clusterless-arc-state-00000000-us-west2/arcs/name=test-project/version=20230101/arc=arc1/lot=20211112PT5M000/complete.arc\"", jsonFromParsed);
    }

    @Test
    void arcManifest() {
        ManifestURI manifestState = ManifestURI.builder()
                .withPlacement(Placement.Builder.builder()
                        .withAccount("00000000")
                        .withRegion("us-west-2")
                        .withProvider("aws")
                        .withStage("prod")
                        .build())
                .withDataset(Dataset.Builder.builder()
                        .withName("test-dataset")
                        .withVersion("20230101")
                        .build())
                .build();

        String attempt = manifestState.attemptId();

        Assertions.assertTrue(manifestState.isPath());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/"), manifestState.uri());

        String template = manifestState.template();
        Assertions.assertEquals("s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot={lot}/state={state}/attempt={attempt}/manifest.json", template);

        ManifestURI parsedArcState = ManifestURI.parse(template);
        Assertions.assertEquals(manifestState.uri(), parsedArcState.uri());

        manifestState = manifestState.withLot("20211112PT5M000");

        Assertions.assertTrue(manifestState.isPath());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot=20211112PT5M000/"), manifestState.uri());

        manifestState = manifestState.withState(ManifestState.complete);

        Assertions.assertTrue(manifestState.isIdentifier());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot=20211112PT5M000/state=complete/manifest.json"), manifestState.uri());

        // has an attempt
        manifestState = manifestState.withState(ManifestState.removed);

        Assertions.assertTrue(manifestState.isIdentifier());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot=20211112PT5M000/state=removed/attempt=" + attempt + "/manifest.json"), manifestState.uri());

        manifestState = manifestState.withState(ManifestState.partial);

        Assertions.assertTrue(manifestState.isIdentifier());
        Assertions.assertEquals(URI.create("s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot=20211112PT5M000/state=partial/attempt=" + attempt + "/manifest.json"), manifestState.uri());

        template = manifestState.template();
        Assertions.assertEquals("s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot=20211112PT5M000/state=partial/attempt=" + attempt + "/manifest.json", template);

        parsedArcState = ManifestURI.parse(template);
        Assertions.assertEquals(manifestState.uri(), parsedArcState.uri());

        String jsonFromRaw = JSONUtil.writeAsStringSafe(manifestState);

        Assertions.assertEquals("\"s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot=20211112PT5M000/state=partial/attempt=" + attempt + "/manifest.json\"", jsonFromRaw);

        String jsonFromParsed = JSONUtil.writeAsStringSafe(parsedArcState);
        Assertions.assertEquals("\"s3://prod-clusterless-manifest-00000000-us-west2/datasets/name=test-dataset/version=20230101/lot=20211112PT5M000/state=partial/attempt=" + attempt + "/manifest.json\"", jsonFromParsed);
    }
}
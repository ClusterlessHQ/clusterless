package clusterless.scenario.conductor.runner;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.WorkflowManager;
import clusterless.scenario.conductor.task.aws.S3Ingress;
import clusterless.scenario.conductor.task.aws.S3Watcher;
import clusterless.scenario.conductor.task.cli.Deployer;
import clusterless.scenario.conductor.task.cli.Destroyer;
import clusterless.scenario.model.IngressStore;
import clusterless.scenario.model.Scenario;
import clusterless.scenario.model.WatchedStore;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScenarioRunner {
    private static final Logger LOG = LogManager.getLogger(ScenarioRunner.class);
    private final Options options;
    private final WorkflowManager workflowManager;
    private final Scenario scenario;
    private String workflowId;

    public ScenarioRunner(Options options, WorkflowManager workflowManager, Scenario scenario) {
        this.options = options;
        this.workflowManager = workflowManager;
        this.scenario = scenario;
    }

    public String workflowId() {
        return workflowId;
    }

    public String exec() {
        WorkflowDef workflowDefinition = new WorkflowDef();
        workflowDefinition.setName(scenario.name());
        workflowDefinition.setDescription(scenario.description());
        workflowDefinition.setOwnerEmail("sample@sample.com");
        List<WorkflowTask> tasks = workflowDefinition.getTasks();

        applyDeployer(tasks);
//        applyIngressWithWait(tasks); // waits never return
        applyIngress(tasks);
//        applyWatchedForkJoin(tasks); // forks never join
        applyWatchedSequential(tasks);
        applyDestroyer(tasks);

        StartWorkflowRequest workflowRequest =
                new StartWorkflowRequest()
                        .withName(scenario.name())
                        .withWorkflowDef(workflowDefinition);

        workflowId = workflowManager.workflowClient().startWorkflow(workflowRequest);

        return workflowId;
    }

    private void applyDeployer(List<WorkflowTask> tasks) {
        if (scenario.projectFiles().isEmpty()) {
            LOG.info("scenario: {}, no project files, skipping deployer", scenario.name());
            return;
        }

        LOG.info("scenario: {}, adding deployer: {}", scenario.name(), scenario.projectFiles());
        tasks.addAll(new Deployer("clsDeployer", scenario.projectDirectory(), scenario.projectFiles()).getWorkflowDefTasks());
    }

    private void applyIngress(List<WorkflowTask> tasks) {
        if (scenario.ingressStores().isEmpty()) {
            return;
        }

        LOG.info("scenario: {}, adding ingress stores: {}", scenario.name(), scenario.ingressStores().size());

        for (IngressStore ingressStore : scenario.ingressStores()) {
            DoWhile doWhile = new DoWhile("ingressWhile", ingressStore.objectCount());

            // the wait task within a choice has massive delays
            // the choice itself requires the javascript interpreter which is not available in jdk 17
            SimpleTask ingress = new S3Ingress("s3IngressDelayed", ingressStore)
                    .input("iteration", "${ingressWhile.output.iteration}");

            doWhile.loopOver(ingress);

            tasks.addAll(doWhile.getWorkflowDefTasks());
        }
    }

    /**
     * Wait tasks never return on the specified duration.
     * <p>
     * This could be because it's in an iteration or behind a switch. A previous version simply had the wait in
     * the switch, but that didn't work either.
     * <p>
     * This needs to be resolved as we can't have the ingress workers blocking, we have limited workers to allocate and
     * need to push any waiting to the conductor app.
     */
    private void applyIngressWithWait(List<WorkflowTask> tasks) {
        if (scenario.ingressStores().isEmpty()) {
            return;
        }

        LOG.info("scenario: {}, adding ingress stores: {}", scenario.name(), scenario.ingressStores().size());

        for (IngressStore ingressStore : scenario.ingressStores()) {
            DoWhile doWhile = new DoWhile("ingressWhile", ingressStore.objectCount());

            String condition = "if ( $.iteration != 1) { true; } else { false; }";
            Switch ifNotFirst = new Switch("firstIteration", condition, true);
            ifNotFirst.input("iteration", "${ingressWhile.output.iteration}");

            Wait delayObjectPut = new Wait("delayObjectPut", Duration.ofSeconds(ingressStore.uploadDelaySec()));
            ifNotFirst.decisionCases(Map.of(
                            "true", List.of(
                                    delayObjectPut,
                                    new S3Ingress("s3IngressDelayed", ingressStore)
                                            .input("iteration", "${ingressWhile.output.iteration}")
                            ),
                            "false", List.of(
                                    new S3Ingress("s3Ingress", ingressStore)
                                            .input("iteration", "${ingressWhile.output.iteration}"))
                    )
            );

            doWhile.loopOver(ifNotFirst);

            tasks.addAll(doWhile.getWorkflowDefTasks());
        }
    }

    /**
     * Join never exits IN_PROGRESS, so using sequential for now
     */
    private void applyWatchedForkJoin(List<WorkflowTask> tasks) {
        if (scenario.watchedStores().isEmpty()) {
            return;
        }

        LOG.info("scenario: {}, adding forked watched stores: {}", scenario.name(), scenario.watchedStores().size());

        Task<?>[][] forkedTasks = new Task[scenario.watchedStores().size()][];
        List<String> join = new LinkedList<>();
        int count = 0;
        for (WatchedStore watchedStore : scenario.watchedStores()) {
            String referenceName = "watcher_" + count;
            S3Watcher watcher = new S3Watcher(referenceName, watchedStore);
            forkedTasks[count++] = new Task<?>[]{watcher};
            join.add(referenceName);
        }

        ForkJoin forkJoin = new ForkJoin("watcherFork", forkedTasks);
        String[] array = join.toArray(new String[0]);
        forkJoin.joinOn(array);
        LOG.info("scenario: {}, joining on: {}", scenario.name(), array);

        tasks.addAll(forkJoin.getWorkflowDefTasks());
    }

    private void applyWatchedSequential(List<WorkflowTask> tasks) {
        if (scenario.watchedStores().isEmpty()) {
            return;
        }

        LOG.info("scenario: {}, adding sequential watched stores: {}", scenario.name(), scenario.watchedStores().size());
        int count = 0;
        for (WatchedStore watchedStore : scenario.watchedStores()) {
            String referenceName = "watcher_" + count;
            S3Watcher watcher = new S3Watcher(referenceName, watchedStore);
            tasks.addAll(watcher.getWorkflowDefTasks());
        }
    }

    private void applyDestroyer(List<WorkflowTask> tasks) {
        if (options.disableDestroy()) {
            LOG.info("scenario: {}, skipping destroyer by request", scenario.name());
            return;
        }

        if (scenario.projectFiles().isEmpty()) {
            LOG.info("scenario: {}, no project files, skipping destroyer", scenario.name());
            return;
        }

        LOG.info("scenario: {}, adding destroyer: {}", scenario.name(), scenario.projectFiles());
        tasks.addAll(new Destroyer("clsDestroyer", scenario.projectDirectory(), scenario.projectFiles()).getWorkflowDefTasks());
    }
}

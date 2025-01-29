package clusterless.cls.substrate.aws.activity.batch;

import clusterless.cls.managed.component.ActivityComponentService;
import clusterless.cls.managed.component.ActivityLocalExecutor;
import clusterless.cls.managed.component.ProvidesComponent;
import clusterless.cls.model.deploy.Activity;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;

@ProvidesComponent(
        type = "aws:core:batchExecActivity",
        synopsis = "Create an AWS Batch Exec Activity.",
        description = """
                Allows custom code to be run inside a Docker image on AWS Batch on a schedule.
                When using an aws:core:batchExecActivity, a compute environment is required to execute the Docker image.
                Currently only Fargate is supported.
                
                The "command" is the same as that would be sent to a docker image on execution.
                
                schedule: Fourths|Sixth|Twelfths|etc or a cron(...) or rate(...) expression
                    Either a lot unit can be provided, or a cron expression or rate expression.
                    See https://docs.aws.amazon.com/scheduler/latest/UserGuide/schedule-types.html
                
                enabled: true|false
                    Whether to enable the rule that schedules the activity.
                    Toggle this value to simply disable the schedule without deleting/removing the activity.
                
                imagePath: A relative path
                    The path to the Docker image to build.
                
                environment: {key: value, ...}
                    The environment variables to set in the Docker image.
                
                command: [command, ...]
                    The command to execute in the Docker image.
                
                batchRuntimeProps: These only apply to the AWS Batch job that executes the Docker image.
                """
)
public class BatchExecActivityProvider implements ActivityComponentService<ManagedComponentContext, BatchExecActivity, BatchExecActivityConstruct> {
    @Override
    public BatchExecActivityConstruct create(ManagedComponentContext context, BatchExecActivity model) {
        return new BatchExecActivityConstruct(context, model);
    }


    @Override
    public ActivityLocalExecutor executor(Placement placement, Activity activity) {
        return new BatchExecActivityLocalExecutor((BatchExecActivity) activity);
    }

    @Override
    public Class<BatchExecActivity> modelClass() {
        return BatchExecActivity.class;
    }
}

# Core Constructs

## batchExecArc

## Environment Variables

### AWS

https://docs.aws.amazon.com/batch/latest/userguide/job_env_vars.html

- `AWS_BATCH_JOB_ID` - unique id for the batch attempts
- `AWS_BATCH_JOB_ATTEMPT` - the attempt number for a given job id

  part:
  guid:
  enabled: true
  value: ${AWS_BATCH_JOB_ID:no_job_id}-${AWS_BATCH_JOB_ATTEMPT:1}-${currentTimeMillis}-sh${schemaHash}-ss${schemaSize}
  partition:
  manifest:
  commit:
  uri: ${MANIFEST_PREFIX:output/manifest/}${INPUT_KEY:none}/state=commit/job_id=${AWS_BATCH_JOB_ID:
  local}/attempt=${AWS_BATCH_JOB_ATTEMPT:1}/manifest.txt
  rollback:
  uri: ${MANIFEST_PREFIX:output/manifest/}${INPUT_KEY:none}/state=rollback/job_id=${AWS_BATCH_JOB_ID:
  local}/attempt=${AWS_BATCH_JOB_ATTEMPT:1}/manifest.txt

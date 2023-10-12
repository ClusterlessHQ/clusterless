local stage = std.extVar('scenario.stage');
local account = std.extVar('scenario.aws.account');
local region = std.extVar('scenario.aws.region');
local bucketName = 'clusterless-frequent-filter-copy-test-' + account + '-' + region;
local bucketPrefix = 's3://' + bucketName;
local unit = 'Twelfths';

{
  project: {
    name: 'S3FreqFiltCp',
    version: '20230101-00',
  },
  placement: {
    stage: stage,
    provider: 'aws',
    account: account,
    region: region,
  },
  resources: [
    {
      type: 'aws:core:s3Bucket',
      name: 'bucket',
      bucketName: bucketName,
    },
  ],
  boundaries: [
    {
      type: 'aws:core:s3PutListenerBoundary',
      name: 'FreqPutListener',
      eventArrival: 'frequent',
      dataset: {
        name: 'ingress-frequent-copy',
        version: '20220101',
        pathURI: bucketPrefix + '/ingress/',
      },
      lotUnit: unit,
    },
  ],
  arcs: [
    {
      type: 'aws:core:s3CopyArc',
      name: 'copyA',
      sources: {
        main: {
          name: 'ingress-frequent-copy',
          version: '20220101',
        },
      },
      sinks: {
        main: {
          name: 'copy-a-frequent-copy',
          version: '20230101',
          pathURI: bucketPrefix + '/copy-a/',
        },
      },
      workload: {
        workloadProps: {
          filter: {
            excludes: ['**/_*'],
          },
        },
      },
    },
  ],
}

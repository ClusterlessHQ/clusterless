local stage = std.extVar('scenario.stage');
local account = std.extVar('scenario.aws.account');
local region = std.extVar('scenario.aws.region');
local bucketName = 'clusterless-simple-test-' + account + '-' + region;
local bucketPrefix = 's3://' + bucketName;
local unit = 'Twelfths';

{
  project: {
    name: 'S3Simple',
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
      name: 'IngressPutListener',
      dataset: {
        name: 'ingress-simple',
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
          name: 'ingress-simple',
          version: '20220101',
          pathURI: bucketPrefix + '/ingress/',
        },
      },
      sinks: {
        main: {
          name: 'copy-a-simple',
          version: '20230101',
          pathURI: bucketPrefix + '/copy-a/',
        },
      },
    },
  ],
}

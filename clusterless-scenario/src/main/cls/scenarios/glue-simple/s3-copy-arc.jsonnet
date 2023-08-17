local stage = std.extVar('scenario.stage');
local account = std.extVar('scenario.aws.account');
local region = std.extVar('scenario.aws.region');
local bucketName = 'clusterless-glue-test-' + account + '-' + region;
local bucketPrefix = 's3://' + bucketName;
local unit = 'Twelfths';
local databaseName = 'clusterless-glue-test-db';
local tableName = 'clusterless-glue-test-table';

{
  project: {
    name: 'GlueSimple',
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
    {
      type: 'aws:core:glueDatabase',
      name: 'database',
      databaseName: databaseName,
    },
    {
      type: 'aws:core:glueTable',
      name: 'table',
      databaseRef: 'database',
      tableName: tableName,
      pathURI: bucketPrefix + '/ingress/',
      schema: {
        columns: [
          {
            name: 'id',
            type: 'int',
          },
          {
            name: 'value',
            type: 'string',
          },
        ],
        partitions: [
          {
            name: 'partition',
            type: 'string',
          },
        ],
        dataFormat: 'csv',
      },
    },
  ],
  boundaries: [
    {
      type: 'aws:core:s3PutListenerBoundary',
      name: 'IngressPutListener',
      dataset: {
        name: 'ingress-glue',
        version: '20220101',
        pathURI: bucketPrefix + '/ingress/',
      },
      lotUnit: unit,
    },
  ],
  arcs: [
    {
      type: 'aws:core:glueAddPartitionsArc',
      name: 'addPartitions',
      sources: {
        main: {
          name: 'ingress-glue',
          version: '20220101',
          pathURI: bucketPrefix + '/ingress/',
        },
      },
      sinks: {
        main: {
          name: 'partitions',
          version: '20220101',
          pathURI: 'glue:///'+databaseName+'/'+tableName,
        },
      },
    },
  ],
}

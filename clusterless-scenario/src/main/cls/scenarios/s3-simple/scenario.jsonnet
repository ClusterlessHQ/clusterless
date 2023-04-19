local account = std.extVar('aws.account');
local region = std.extVar('aws.region');
local bucketName = 'clusterless-simple-test-' + account + '-' + region;

{
  name: 's3-simple',
  description: 'simple copy workload',
  projectFiles: [
    's3-copy-arc.json',
  ],
  ingressStores: [
    {
      region: region,
      path: 's3://' + bucketName + '/ingress/',
      uploadDelaySec: 60 * 5,
      objectCount: 3,
    },
  ],
  watchedStores: [
    {
      region: region,
      path: 's3://' + bucketName + '/copy-a/',
      objectCount: 3,
    },
  ],
}

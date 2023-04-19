local account = std.extVar('aws.account');
local region = std.extVar('aws.region');
local bucketName = 'clusterless-chain-test-' + account + '-' + region;

{
  name: 'copy-chain',
  description: 'chained copy workloads',
  projectFiles: [
    'test-s3-copy-arc-project-chain.json',
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
      path: 's3://' + bucketName + '/copy-c/',
      objectCount: 3,
    },
  ],
}

// Keeps the persistent Spot requests of an aws:core:ec2Instance from outliving its instances, see
// SpotRequestCanceller.java. Inline behind the cdk provider framework, using only the runtime's sdk.
const ec2sdk = require('@aws-sdk/client-ec2');
const { CloudFormationClient, DescribeStacksCommand } = require('@aws-sdk/client-cloudformation');

const ec2 = new ec2sdk.EC2Client({});
const cfn = new CloudFormationClient({});

const LIVE = ['pending', 'running', 'stopping', 'stopped'];
const UNCANCELLED = ['open', 'active', 'disabled'];
const LEAVING = ['DELETE_IN_PROGRESS', 'ROLLBACK_IN_PROGRESS'];

exports.handler = async (event) => {
  const props = event.ResourceProperties;
  const tag = { Key: props.RequestTagKey, Value: props.RequestTagValue };

  if (event.RequestType !== 'Delete') {
    const keep = [props.InstanceId, event.OldResourceProperties?.InstanceId].filter(Boolean);

    try {
      await sweep(tag, keep);
    } catch (error) {
      // a failed create or update rolls back, which is how a request is left behind
      console.log('sweep failed, left for the next deploy', error);
    }

    return { PhysicalResourceId: props.InstanceId };
  }

  const own = (await describe([props.InstanceId])).map((i) => i.SpotInstanceRequestId).filter(Boolean);

  await cancel(own, [props.InstanceId]);

  if (LEAVING.includes(await stackStatus(event.StackId))) {
    await sweep(tag, []);
  }

  return {};
};

// cancels the tagged requests of no instance in keep, keep[0] being the current instance
async function sweep(tag, keep) {
  const instances = await describe(keep);

  if (keep.length > 0 && !instances.some((i) => i.InstanceId === keep[0] && i.SpotInstanceRequestId)) {
    console.log(`no spot request found for ${keep[0]}, not sweeping`);
    return;
  }

  const kept = instances.map((i) => i.SpotInstanceRequestId).filter(Boolean);
  const tagged = await requests([{ Name: `tag:${tag.Key}`, Values: [tag.Value] }, { Name: 'state', Values: UNCANCELLED }]);
  const orphans = tagged
    .filter((r) => !kept.includes(r.SpotInstanceRequestId) && !keep.includes(r.InstanceId))
    .map((r) => r.SpotInstanceRequestId);

  if (orphans.length > 0) {
    console.log(`cancelling requests of no instance in the stack: ${orphans.join(', ')}`);
    await cancel(orphans, keep);
  }
}

// once cancelled a request launches nothing more, then terminates what it already launched
async function cancel(requestIds, keep) {
  if (requestIds.length === 0) {
    return;
  }

  const byId = { Name: 'spot-instance-request-id', Values: requestIds };
  const uncancelled = (await requests([byId, { Name: 'state', Values: UNCANCELLED }])).map((r) => r.SpotInstanceRequestId);

  if (uncancelled.length > 0) {
    await ec2.send(new ec2sdk.CancelSpotInstanceRequestsCommand({ SpotInstanceRequestIds: uncancelled }));
  }

  const launched = (await requests([byId])).map((r) => r.InstanceId).filter((id) => id && !keep.includes(id));
  const strays = (await describe(launched)).filter((i) => LIVE.includes(i.State.Name)).map((i) => i.InstanceId);

  if (strays.length > 0) {
    console.log(`terminating instances launched outside the stack: ${strays.join(', ')}`);
    await ec2.send(new ec2sdk.TerminateInstancesCommand({ InstanceIds: strays }));
  }
}

// filters, unlike ids, do not fail on an instance or request no longer described
async function describe(instanceIds) {
  if (instanceIds.length === 0) {
    return [];
  }

  const { Reservations = [] } = await ec2.send(new ec2sdk.DescribeInstancesCommand({ Filters: [{ Name: 'instance-id', Values: instanceIds }] }));

  return Reservations.flatMap((r) => r.Instances ?? []);
}

async function requests(filters) {
  const { SpotInstanceRequests = [] } = await ec2.send(new ec2sdk.DescribeSpotInstanceRequestsCommand({ Filters: filters }));

  return SpotInstanceRequests;
}

async function stackStatus(stackId) {
  const { Stacks = [] } = await cfn.send(new DescribeStacksCommand({ StackName: stackId }));

  return Stacks[0]?.StackStatus;
}

// Cancels the persistent Spot request behind an aws:core:ec2Instance when the instance leaves the
// stack, and terminates any instance the request launched outside it. Run inline by the cdk custom
// resource provider framework, which answers cloudformation; uses only the runtime's aws sdk.
//
// The physical id is the instance id, so a replaced instance's resource is deleted with the old
// id's properties. Create and update make no calls, so neither can fail and leave a rollback with
// no request id to cancel.
const {
  EC2Client,
  DescribeInstancesCommand,
  CancelSpotInstanceRequestsCommand,
  DescribeSpotInstanceRequestsCommand,
  TerminateInstancesCommand,
} = require('@aws-sdk/client-ec2');

const ec2 = new EC2Client({});

exports.handler = async (event) => {
  const instanceId = event.ResourceProperties.InstanceId;

  if (event.RequestType !== 'Delete') {
    return { PhysicalResourceId: instanceId };
  }

  const requestIds = await spotRequestIds(instanceId);

  if (requestIds.length === 0) {
    return {};
  }

  // once cancelled, a request launches nothing more
  await ec2.send(new CancelSpotInstanceRequestsCommand({ SpotInstanceRequestIds: requestIds }));

  // a request reopened by an earlier termination may already have launched another instance
  const { SpotInstanceRequests = [] } = await ec2.send(new DescribeSpotInstanceRequestsCommand({ SpotInstanceRequestIds: requestIds }));
  const strays = SpotInstanceRequests
    .map((request) => request.InstanceId)
    .filter((id) => id && id !== instanceId);

  if (strays.length > 0) {
    console.log(`terminating instances launched outside the stack: ${strays.join(', ')}`);
    await ec2.send(new TerminateInstancesCommand({ InstanceIds: strays }));
  }

  return {};
};

async function spotRequestIds(instanceId) {
  try {
    const { Reservations = [] } = await ec2.send(new DescribeInstancesCommand({ InstanceIds: [instanceId] }));

    return Reservations
      .flatMap((reservation) => reservation.Instances ?? [])
      .map((instance) => instance.SpotInstanceRequestId)
      .filter(Boolean);
  } catch (error) {
    // terminated long enough ago to no longer be described, there is nothing left to find
    if (error.name === 'InvalidInstanceID.NotFound') {
      return [];
    }

    throw error;
  }
}

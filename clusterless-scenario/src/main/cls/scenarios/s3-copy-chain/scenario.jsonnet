local project = import 's3-copy-arc-project-chain.jsonnet';

{
  name: 'copy-chain',
  description: 'chained copy workloads',
  projectFiles: [
    's3-copy-arc-project-chain.json',
  ],
  placements: [
    project.placement,
  ],
  ingressStores: [
    {
      region: project.placement.region,
      path: project.boundaries[0].dataset.pathURI,
      uploadDelaySec: 60 * 5,
      objectCount: 3,
    },
  ],
  watchedStores: [
    {
      region: project.placement.region,
      path: project.arcs[2].sinks.main.pathURI,
      objectCount: 3,
    },
  ],
}

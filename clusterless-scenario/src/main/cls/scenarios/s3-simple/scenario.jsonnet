local project = import 's3-copy-arc.jsonnet';

{
  name: 's3-simple',
  description: 'simple copy workload',
  projectFiles: [
    's3-copy-arc.json',
  ],
  placements: [
    project.placement,
  ],
  ingressStores: [
    {
      region: project.placement.region,
      path: project.arcs[0].sources.main.pathURI,
      uploadDelaySec: 60 * 5,
      objectCount: 3,
    },
  ],
  watchedStores: [
    {
      region: project.placement.region,
      path: project.arcs[0].sinks.main.pathURI,
      objectCount: 3,
    },
  ],
}

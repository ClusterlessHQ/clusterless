local project = import 's3-copy-arc.jsonnet';

{
  name: 's3-frequent',
  description: 'copy workload with frequent arrivals',
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
      uploadDelaySec: 15,
      objectCount: 60/15 * 5 * 3,
    },
    {
      region: project.placement.region,
      path: project.arcs[0].sources.main.pathURI,
      uploadDelaySec: 15,
      objectCount: 60/15 * 5 * 3,
      objectName: '_SUCCESS-%04d-%d.txt',
    },
  ],
  watchedStores: [
    {
      region: project.placement.region,
      path: project.arcs[0].sinks.main.pathURI,
      objectCount: 60/15 * 5 * 3,
    },
  ],
}

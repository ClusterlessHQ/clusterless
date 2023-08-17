local project = import 's3-copy-arc.jsonnet';

{
  name: 'glue-simple',
  description: 'simple glue workload',
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
      objectName: 'partition=%1$04d/data-%1$04d-%2$d.txt',
    },
  ],
  watchedStores: [
    {
      region: project.placement.region,
      path: project.arcs[0].sinks.main.pathURI,
      watchType: 'glue',
      objectCount: 3,
    },
  ],
}

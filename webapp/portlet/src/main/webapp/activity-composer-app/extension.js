
export let activityComposerApplications;

function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('ActivityComposer', type);
}

export function installExtensions() {
  activityComposerApplications = getExtensionsByType('activity-composer-application');
  activityComposerApplications.forEach(app => {
    app.install();
  });
}
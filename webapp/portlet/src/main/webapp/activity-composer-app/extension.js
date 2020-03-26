export let activityComposerActions;

function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('ActivityComposer', type);
}

export function installExtensions() {
  activityComposerActions = getExtensionsByType('activity-composer-action');
}
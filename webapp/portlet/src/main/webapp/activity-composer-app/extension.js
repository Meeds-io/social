export const ACTIVITY_COMPOSER_APPS = [
  {
    key: 'file',
    rank: 20,
    labelKey: 'Add a File',
    iconClass: 'uiIconAddFileComposer',
    component: '',
  }
];

export let activityComposerApplications;

function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('ActivityComposer', type);
}
export function installExtensions() {
  extensionRegistry.registerExtension('ActivityComposer', 'activity-composer-application', ACTIVITY_COMPOSER_APPS);
  activityComposerApplications = getExtensionsByType('activity-composer-application');
}
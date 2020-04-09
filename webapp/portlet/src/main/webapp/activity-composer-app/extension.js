let activityComposerActions = null;

export function getActivityComposerExtensions() {
  if(activityComposerActions == null) {
    const allExtensions = getExtensionsByType('activity-composer-action');
    activityComposerActions = allExtensions.filter(extension => isExtensionEnabled(extension));
  }

  return activityComposerActions;
}

export function executeExtensionAction(extension, component) {
  if(extension.hasOwnProperty('onExecute') && isFunction(extension.onExecute)) {
    extension.onExecute(component);
  }
}

function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('ActivityComposer', type);
}

function isExtensionEnabled(extension) {
  if(extension.hasOwnProperty('enabled')) {
    if(typeof extension.enabled === 'boolean') {
      return extension.enabled;
    } else if(isFunction(extension.enabled)) {
      return extension.enabled.call();
    }
  }

  return true;
}

function isFunction(object) {
  return object && {}.toString.call(object) === '[object Function]';
}

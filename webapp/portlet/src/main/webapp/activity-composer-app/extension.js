let activityComposerActions = null;
let activityComposerHintAction = null;

export function getActivityComposerActionExtensions() {
  if(activityComposerActions == null) {
    const allExtensions = getExtensionsByType('activity-composer-action');
    activityComposerActions = allExtensions.filter(extension => isExtensionEnabled(extension));
  }

  return activityComposerActions;
}

export function getActivityComposerHintActionExtensions() {
    activityComposerHintAction = getExtensionsByType('activity-composer-hint-action');
    if (activityComposerHintAction.length === 0) {
      activityComposerHintAction = null;
  }
  return activityComposerHintAction;
}

export function executeExtensionAction(extension, component) {
  if(extension.hasOwnProperty('onExecute') && isFunction(extension.onExecute)) {
    if(component) {
      extension.onExecute(component[0]);
    } else {
      extension.onExecute();
    }
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

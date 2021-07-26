export function getActivityComposerActionExtensions() {
  const allExtensions = getExtensionsByType('activity-composer-action');
  return allExtensions.filter(extension => isExtensionEnabled(extension));
}

export function getActivityComposerHintActionExtensions() {
  const activityComposerHintAction = getExtensionsByType('activity-composer-hint-action');
  return activityComposerHintAction && activityComposerHintAction.length && activityComposerHintAction[0] || null;
}

export function executeExtensionAction(extension, component, attachments) {
  if (extension.hasOwnProperty('onExecute') && isFunction(extension.onExecute)) {
    if (component) {
      extension.onExecute(component[0]);
    } else {
      extension.onExecute(attachments);
    }
  }
}

function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('ActivityComposer', type);
}

function isExtensionEnabled(extension) {
  if (extension.hasOwnProperty('enabled')) {
    if (typeof extension.enabled === 'boolean') {
      return extension.enabled;
    } else if (isFunction(extension.enabled)) {
      return extension.enabled.call();
    }
  }

  return true;
}

function isFunction(object) {
  return object && {}.toString.call(object) === '[object Function]';
}

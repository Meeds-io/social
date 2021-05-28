let activityComposerHintAction = null;
export function getActivityComposerActionExtensions() {
  return getExtensionsByType('activity-composer-action');
}

export function getActivityComposerHintActionExtensions() {
  if (activityComposerHintAction === null){
    activityComposerHintAction = getExtensionsByType('activity-composer-hint-action');
    if (activityComposerHintAction) {
      activityComposerHintAction = activityComposerHintAction.sort(compare);
      activityComposerHintAction = activityComposerHintAction[0];
    } else {
      activityComposerHintAction = null;
    }
  }
  return activityComposerHintAction;
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

function isFunction(object) {
  return object && {}.toString.call(object) === '[object Function]';
}

function compare(a, b) {
  if (a.rank < b.rank) {
    return -1;
  }
  if (a.rank > b.rank) {
    return 1;
  }
  return 0;
}

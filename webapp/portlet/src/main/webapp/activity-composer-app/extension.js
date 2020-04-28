/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
let activityComposerActions = null;
let activityComposerHintAction = null;
export function getActivityComposerActionExtensions() {
  const allExtensions = getExtensionsByType('activity-composer-action');
  activityComposerActions = allExtensions.filter(extension => isExtensionEnabled(extension));

  return activityComposerActions;
}

export function getActivityComposerHintActionExtensions() {
  if(activityComposerHintAction === null){
    activityComposerHintAction = getExtensionsByType('activity-composer-hint-action');
    if (activityComposerHintAction) {
      activityComposerHintAction = activityComposerHintAction.sort(compare);
      activityComposerHintAction = activityComposerHintAction[0];
    }else {
      activityComposerHintAction = null;
    }
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

function compare(a, b) {
  if (a.rank < b.rank) {
    return -1;
  }
  if (a.rank > b.rank) {
    return 1;
  }
  return 0;
}

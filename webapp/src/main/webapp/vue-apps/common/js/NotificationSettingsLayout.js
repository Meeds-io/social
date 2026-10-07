/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


// Left to right; a channel not listed here goes leftmost
const CHANNELS_ORDER = ['SPACE_WEB_CHANNEL', 'WEB_CHANNEL', 'MAIL_CHANNEL'];

// Icons of the settings groups owned by the social notification configuration
const SOCIAL_GROUPS_ICONS = {
  general: 'fa-cog',
  connections: 'fa-user-friends',
  spaces: 'fa-layer-group',
  activity_stream: 'fa-stream',
  other: 'fa-ellipsis-h',
};

let pendingSaves = 0;
let savesQueue = Promise.resolve();

/**
 * Runs a notification setting save once every save queued before it has
 * settled: the user settings endpoint rewrites the whole stored setting on
 * each save, so two concurrent saves could lose one of the changes.
 *
 * @param {Function} save returns the promise of the save request
 * @return {Promise} the promise of this save
 */
export function queueSettingSave(save) {
  pendingSaves++;
  const result = savesQueue.then(save).finally(() => pendingSaves--);
  savesQueue = result.catch(() => null);
  return result;
}

/**
 * @return {boolean} whether a queued save has not settled yet
 */
export function hasPendingSettingSaves() {
  return pendingSaves > 0;
}

/**
 * Computes the channel columns of a notification settings group: the channels
 * at least one plugin of the group allows, in the fixed display order.
 *
 * @param {Object} settings notification settings REST object
 * @param {Object} group notification settings group
 * @return {Array} the ordered channel ids
 */
export function getChannelColumns(settings, group) {
  const pluginTypes = (group?.pluginInfos || []).map(plugin => plugin.type);
  const allowedChannels = new Set((settings?.channelCheckBoxList || [])
    .filter(choice => choice.allowed && pluginTypes.includes(choice.pluginId))
    .map(choice => choice.channelId));
  return (settings?.channels || [])
    .filter(channelId => allowedChannels.has(channelId))
    .map((channelId, index) => ({channelId, index, rank: CHANNELS_ORDER.indexOf(channelId) + 1}))
    .sort((a, b) => a.rank - b.rank || a.index - b.index)
    .map(item => item.channelId);
}

/**
 * Computes the inline style of a channel column.
 *
 * @param {boolean} mobile whether the viewport is below the md breakpoint
 * @return {string} the width style
 */
export function getColumnStyle(mobile) {
  const width = mobile ? 64 : 96;
  return `width: ${width}px; min-width: ${width}px;`;
}

/**
 * Resolves the icon of a notification settings group: the extension named as
 * the group id, then the social groups map, then the extension whose plugins
 * overlap this group only.
 *
 * @param {Object} settings notification settings REST object
 * @param {Object} group notification settings group
 * @param {Array} extensions the WebNotification group extensions
 * @return {string} the icon, or null
 */
export function getGroupIcon(settings, group, extensions) {
  const groupId = group?.groupId;
  const exactExtension = extensions.find(ext => ext.name === groupId);
  if (exactExtension?.icon) {
    return exactExtension.icon;
  }
  if (SOCIAL_GROUPS_ICONS[groupId]) {
    return SOCIAL_GROUPS_ICONS[groupId];
  }
  // A bell group spanning several settings groups must not paint them all alike
  const pluginTypes = (group?.pluginInfos || []).map(plugin => plugin.type);
  const overlaps = (settingsGroup, extension) => settingsGroup?.pluginInfos?.some(plugin => extension.plugins?.includes(plugin.type));
  const overlappingExtension = extensions.find(ext => ext.plugins?.some(type => pluginTypes.includes(type)));
  if (overlappingExtension && (settings?.groups || []).filter(settingsGroup => overlaps(settingsGroup, overlappingExtension)).length === 1) {
    return overlappingExtension.icon;
  }
  return null;
}

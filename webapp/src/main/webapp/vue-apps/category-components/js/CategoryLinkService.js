/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import { getCategory } from './CategoryService.js';

export function link(id, object) {
  return fetch(`/social/rest/category/links/${id}`, {
    credentials: 'include',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(object),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when linking object to the category');
    }
  });
}

export function unlink(id, object) {
  return fetch(`/social/rest/category/links/${id}`, {
    credentials: 'include',
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(object),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when unlinking object to the category');
    }
  });
}

export async function updateCategories({
  objectType,
  objectId,
  spaceId,
  oldCategories,
  newCategories,
  dropExisting
}) {
  const object = {
    type: objectType,
    id: objectId,
    spaceId,
  };
  const linkedIds = [];
  const failedLinkIds = [];
  const unlinkedIds = [];
  const failedUnlinkIds = [];
  // Sequentially update links
  const linkIds = (newCategories || []).filter(id => (oldCategories || []).indexOf(id) < 0);
  for (const id of linkIds) {
    // eslint-disable-next-line no-await-in-loop
    const linked = await link(id, object).then(() => true).catch(() => false);
    (linked ? linkedIds : failedLinkIds).push(id);
  }
  if (dropExisting && oldCategories?.length) {
    const unlinkIds = oldCategories.filter(id => (newCategories || []).indexOf(id) < 0);
    for (const id of unlinkIds) {
      // eslint-disable-next-line no-await-in-loop
      const unlinked = await unlink(id, object).then(() => true).catch(() => false);
      (unlinked ? unlinkedIds : failedUnlinkIds).push(id);
    }
  }
  if (failedLinkIds.length || failedUnlinkIds.length) {
    const error = new Error('Error while updating categories');
    error.linkedIds = linkedIds;
    error.failedLinkIds = failedLinkIds;
    error.unlinkedIds = unlinkedIds;
    error.failedUnlinkIds = failedUnlinkIds;
    throw error;
  }
}

// Computes the categories an object is really linked to after a partial
// update, from the error thrown by updateCategories.
export function getCategoryIdsAfterFailure(oldCategories, error) {
  const unlinkedIds = error?.unlinkedIds || [];
  const linkedIds = error?.linkedIds || [];
  return (oldCategories || [])
    .filter(id => unlinkedIds.indexOf(id) < 0)
    .concat(linkedIds);
}

async function getCategoryNames(ids) {
  const names = await Promise.all(ids.map(id => getCategory(id)
    .then(category => category?.name || String(id))
    .catch(() => String(id))));
  return names.join(', ');
}

// Builds the user message naming the categories that could not be linked
// and/or unlinked, from the error thrown by updateCategories.
// `t` is the component's $t function.
export async function getUpdateErrorMessage(error, t) {
  const messages = [];
  if (error?.failedLinkIds?.length) {
    messages.push(t('categoryInputDrawer.updated.linkError', {0: await getCategoryNames(error.failedLinkIds)}));
  }
  if (error?.failedUnlinkIds?.length) {
    messages.push(t('categoryInputDrawer.updated.unlinkError', {0: await getCategoryNames(error.failedUnlinkIds)}));
  }
  return messages.join(' ');
}

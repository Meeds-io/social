/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

export async function updateCategories(spaceId, oldCategories, newCategories, dropExisting) {
  let result = true;
  if (newCategories.length) {
    const linkIds = newCategories.filter(id => oldCategories.indexOf(id) < 0);
    if (linkIds?.length) {
      for (let i = 0; i < linkIds.length; i++) {
        const id = linkIds[i];
        // Sequentially update links
        // eslint-disable-next-line no-await-in-loop
        const linkResult = await Vue.prototype.$categoryLinkService.link(id, {
          type: 'space',
          id: spaceId,
          spaceId,
        }).then(() => true).catch(() => false);
        result = result || linkResult;
      }
    }
  }
  if (dropExisting && oldCategories?.length) {
    const unlinkIds = oldCategories.filter(id => newCategories.indexOf(id) < 0);
    if (unlinkIds?.length) {
      for (let i = 0; i < unlinkIds.length; i++) {
        const id = unlinkIds[i];
        // Sequentially update links
        // eslint-disable-next-line no-await-in-loop
        const unlinkResult = await Vue.prototype.$categoryLinkService.unlink(id, {
          type: 'space',
          id: spaceId,
          spaceId,
        }).then(() => true).catch(() => false);
        result = result || unlinkResult;
      }
    }
  }
  if (!result) {
    throw new Error('Error while updating space categories');
  }
}

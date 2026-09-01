/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

// Keeps the timezone the platform knows for the user aligned on the one his
// browser lives in, so that everything sent to him on a schedule goes out at
// the right local hour. This synchronization used to live in the agenda addon.
(function() {
  const timeZoneId = new window.Intl.DateTimeFormat().resolvedOptions().timeZone;
  if (eXo.env.portal.userName && timeZoneId && eXo.env.portal.userTimezone !== timeZoneId) {
    fetch('/social/rest/timezone', {
      headers: {
        'Content-Type': 'text/plain',
      },
      method: 'POST',
      credentials: 'include',
      body: timeZoneId,
    }).then(resp => {
      if (!resp || !resp.ok) {
        throw new Error('Server Request Error: Cannot update user TimeZone');
      }
    });
  }
})();

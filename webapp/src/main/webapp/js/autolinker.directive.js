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
Vue.directive('autolinker', (el, binding) => {
  if (!binding.value) {
    el.innerHTML = '';
    return;
  }

  const isPhone = ['phone', 'call', 'messaging'].includes(binding.arg);
  const isEmail = binding.arg === 'email';

  const autolinker = new Autolinker({
    sanitizeHtml: true,
    phone: isPhone,
    email: isEmail,
    hashtag: false,
    mention: false,
    replaceFn(match) {
      if (match.getType() === 'url') {
        const url = match.getUrl();
        if (url.indexOf('/') === 0 ||
            url.indexOf(window.location.origin) === 0) {
          return true;
        } else {
          const tag = match.buildTag();
          tag.setAttr('target', '_blank');
          tag.setAttr('rel', 'nofollow noopener noreferrer');
          return tag;
        }
      }
      return true;
    }
  });

  let html = autolinker.link(binding.value);

  if (isPhone) {
    html = html.replaceAll(/([*#+]?\d[\d\s().-]{0,20}#?)/g, match => {
      if (
        html.includes(`href="tel:${match}`) ||
          /<\/?a\b[^>]*>/i.test(match) ||
          /^(?:https?:\/\/|www\.)[^\s]+$|^[^@\s]+@[^@\s]+\.[A-Za-z]{2,}$/i.test(match)
      ) {
        return match;
      }

      const safeNumber = match
        .replaceAll(/&/g, '&amp;')
        .replaceAll(/</g, '&lt;')
        .replaceAll(/>/g, '&gt;');

      return `<a href="tel:${safeNumber}">${safeNumber}</a>`;
    });
  }

  el.innerHTML = html;
});

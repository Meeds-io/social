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

  // $utils comes with commonVueComponents: without it, numbers Autolinker left as text stay text
  const utils = Vue.prototype.$utils;
  if (isPhone && utils) {
    // Links the numbers Autolinker left as text, never inside one of its anchors, and only
    // a run the profile accepts as a phone number. A run holds only digits, separators and
    // +*#: nothing to escape.
    html = html.split(/(<a\b[^>]*>[\s\S]*?<\/a>)/gi)
      .map(part => (/^<a\b/i.test(part) ? part : part.replaceAll(/[*#+]?\d(?:[\d\s().-]*\d)?#?/g,
        match => (utils.isValidPhone(match) ? `<a href="tel:${utils.normalizePhone(match)}">${match}</a>` : match))))
      .join('');
  }

  el.innerHTML = html;
});

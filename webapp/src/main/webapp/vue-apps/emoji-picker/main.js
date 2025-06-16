/*
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2025 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import './initComponents.js';

const lang = eXo?.env?.portal?.language || 'en';
const url = `/social/i18n/locale.portlet.EmojiPicker?lang=${lang}`;
const emojiBankUrl = '/social/json/emojiBank.json?v=1';
const vuetify = Vue.prototype.vuetifyOptions;
const appId = 'emojiPicker';

const emojiAppElement = document.createElement('div');
emojiAppElement.setAttribute('id', appId);
document.querySelector('#vuetify-apps').append(emojiAppElement);

Promise.all([
  exoi18n.loadLanguageAsync(lang, url),
  fetch(emojiBankUrl).then(res => res.json())
]).then(([i18n, emojiBank]) => {
  Object.defineProperty(Vue.prototype, '$emojiBank', {value: emojiBank,});
  Vue.createApp({
    data() {
      return {
        emojiBank
      };
    },
    template: '<emoji-picker />',
    vuetify,
    i18n
  }, `#${appId}`, 'Emoji picker');
});


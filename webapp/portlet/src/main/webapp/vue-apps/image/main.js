/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

import './initComponents.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('Image');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const lang = eXo.env.portal.language;
const url = `/social-portlet/i18n/locale.portlet.Image?lang=${lang}`;

export function init(appId, name, canEdit, files) {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      Vue.createApp({
        data: {
          objectType: 'imagePortlet',
          imageDisplayFormats: {
            custom: {
              width: 0,
              height: 0,
            },
            landscape: {
              width: 1280,
              height: 160,
            },
            portrait: {
              width: 960,
              height: 1280,
            },
            square: {
              width: 1280,
              height: 1280,
            }
          },
          name,
          canEdit,
          files,
          offsetWidth: 0,
        },
        computed: {
          hasImages() {
            return this.files?.length;
          },
          imageUrl() {
            if (this.hasImages) {
              const fileId = this.files?.[0]?.id;
              return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectType}/${this.name}/${fileId}`;
            } else {
              return null;
            }
          },
          imageFormatName() {
            return this.files?.[0]?.format || 'landscape';
          },
          imageAltText() {
            return this.files?.[0]?.altText || '';
          },
          imageFormat() {
            return this.imageDisplayFormats[this.imageFormatName];
          },
          imageWidth() {
            return this.imageFormat?.width;
          },
          imageHeight() {
            return this.fixedHeight || this.imageFormat?.height;
          },
          imageAspectRatio() {
            return this.imageWidth && this.imageWidth / this.imageHeight || 0;
          },
          formatAspectRatio() {
            return this.imageFormat && (this.imageFormat.width / this.imageFormat.height);
          },
        },
        watch: {
          fixedHeight: {
            immediate: true,
            handler(newVal, oldVal) {
              if (newVal && !oldVal) {
                window.addEventListener('resize', this.updateWidth);
              } else if (oldVal && !newVal) {
                this.resizeListenerInstalled = false;
                window.removeEventListener('resize', this.updateWidth);
              }
            },
          },
        },
        methods: {
          updateWidth() {
            this.offsetWidth = this.$el?.offsetWidth;
          },
        },
        template: `<image-app id="${appId}"></image-app>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, `#${appId}`, `Image Application - ${name}`);
    });
}
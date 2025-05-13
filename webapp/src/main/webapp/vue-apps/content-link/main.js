/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
import './initComponents.js';
import './services.js';

const lang = window.eXo?.env?.portal?.language || 'en';
const url = `/social/i18n/locale.portlet.ContentLink?lang=${lang}`;
const appId = 'contentLinkParent';
let contentLinkApp;

export async function openDrawer(editor) {
  await init();
  document.dispatchEvent(new CustomEvent('content-link-drawer', {
    detail: editor,
  }));
}

export async function openCommandMenu(eventDetail) {
  await init();
  document.dispatchEvent(new CustomEvent('content-link-menu-open', {
    detail: eventDetail,
  }));
}

export function closeMenu() {
  document.dispatchEvent(new CustomEvent('content-link-menu-close'));
}

export async function init() {
  if (contentLinkApp) {
    return;
  }
  const i18n = await exoi18n.loadLanguageAsync(lang, url);

  const element = document.createElement('div');
  element.setAttribute('id', appId);
  document.querySelector('#vuetify-apps').append(element);

  return new Promise(resolve => {
    contentLinkApp = Vue.createApp({
      data: {
        editor: null,
        plugins: null,
        initPhase: 0,
        collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
      },
      watch: {
        initPhase(val) {
          if (val === 2) {
            resolve();
          }
        },
      },
      async created() {
        document.addEventListener('content-link-drawer', this.openDrawer);
        const plugins = await this.$contentLinkService.getExtensions();
        this.plugins = plugins.sort(this.comparator);
        this.initPhase++;
      },
      beforeDestroy() {
        document.removeEventListener('content-link-drawer', this.openDrawer);
      },
      mounted() {
        this.initPhase++;
      },
      methods: {
        openDrawer(event) {
          this.editor = event?.detail;
          this.$refs.drawer.open();
        },
        async selectLink(link) {
          const selection = this.editor.getSelection();
          const range = selection.getRanges()[0];
          const element = new CKEDITOR.dom.element('a'); // eslint-disable-line new-cap
          element.setAttribute('data-object', `${link.objectType}:${link.objectId}`);
          element.setAttribute('data-content-link', 'true');
          element.setAttribute('contenteditable', 'false');
          element.setAttribute('class', 'content-link');
          element.setAttribute('href', link.uri);
          element.setAttribute('target', '_blank');
          element.appendHtml(`<i aria-hidden="true" class="v-icon notranslate fa ${link.icon} theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>${link.title}`);
          range.insertNode(element);
          range.moveToElementEditablePosition(element);
          await this.$nextTick();
          this.$refs?.drawer?.close?.();
          this.$refs?.menu?.close?.();
          this.editor.focus();

          window.startContainer = range.startContainer;
          const startContainer = range.startContainer?.find?.(`.content-link[data-object='${link.objectType}:${link.objectId}']`)?.getItem?.(0);
          if (startContainer) {
            const newRange = this.editor.createRange();
            newRange.setStartAfter(startContainer);
            newRange.select();
          }
          this.editor.insertHtml(''); // On purpose empty to force loading CKEditor content
        },
        comparator(a, b) {
          return this.collator.compare(this.$t(a.titleKey), this.$t(b.titleKey));
        },
      },
      template: `
      <div id="${appId}">
        <content-link-drawer
          ref="drawer" />
        <content-link-search-drawer
          @select="selectLink" />
        <content-link-command-menu
          ref="menu"
          @select="selectLink" />
      </div>
      `,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, 'Content Link');
  });
}

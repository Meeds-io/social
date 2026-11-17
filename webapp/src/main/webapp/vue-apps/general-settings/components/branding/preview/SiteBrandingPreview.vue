<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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

-->
<template>
  <div class="d-none d-lg-flex flex-column">
    <div class="d-flex align-center">
      <div class="text-header">
        {{ $t('generalSettings.preview.title') }}
      </div>
      <v-spacer />
      <portal-general-settings-theme-preview-modes
        v-if="!singleMode"
        v-model="selectedMode" />
    </div>
    <div
      ref="iframeParent"
      class="mt-4 position-relative border-color full-width aspect-ratio-1 overflow-hidden border-radius"
      :style="`height: ${iframeParentHeight}`">
      <iframe
        v-if="initialized && parentWidth"
        id="previewIframe"
        :title="$t('generalSettings.preview.title')"
        :src="pageHomeLink"
        :width="`${iframeWidthPercentage}%`"
        :height="`${iframeHeightPercentage}%`"
        :style="`transform: scale(${zoom}); transform-origin: 0 0;`"
        name="pageHomeLink"
        class="no-border"
        @load="applyPreview">
      </iframe>
    </div>
  </div>
</template>
<script>
import {buildPreviewStyle} from '../../../js/ThemePreviewStyle.js';
import {toResolvePayload} from '../../../js/ThemeSettingsMapper.js';

const STYLE_ID = 'brandingPreviewStyle';

export default {
  props: {
    // the page's working copy and the themes as published
    working: {
      type: Object,
      default: null,
    },
    stored: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    initialized: false,
    sites: null,
    parentWidth: 0,
    zoom: 0.5,
    iframeParentHeight: '600px',
    // AUTOMATIC, LIGHT or DARK: the preview's own choice, never the admin's display mode
    selectedMode: 'AUTOMATIC',
    deviceDark: false,
    deviceQuery: null,
    // the theme Theme Edition is editing and its resolved values, else the theme in use is previewed
    editing: null,
    inUseResolved: null,
    timer: null,
  }),
  computed: {
    modesOffered() {
      return this.working?.modesOffered;
    },
    singleMode() {
      return this.modesOffered === 'LIGHT' || this.modesOffered === 'DARK';
    },
    version() {
      if (this.singleMode) {
        return this.modesOffered.toLowerCase();
      }
      return this.selectedMode === 'AUTOMATIC' && (this.deviceDark && 'dark' || 'light') || this.selectedMode.toLowerCase();
    },
    inUseTheme() {
      return this.working?.themes.find(theme => theme.key === this.working.themeInUseKey) || null;
    },
    previewed() {
      return this.editing || {theme: this.inUseTheme, resolved: this.inUseResolved};
    },
    pageHomeLink() {
      return `${eXo.env.portal.context}/${this.sites?.[0]?.name || eXo.env.portal.metaPortalName}?sticky=false`;
    },
    iframeWidthPercentage() {
      return parseInt((1 / this.zoom) * 100);
    },
    iframeHeightPercentage() {
      return parseInt((1 / this.zoom) * 100);
    },
  },
  watch: {
    inUseTheme: {
      deep: true,
      immediate: true,
      handler() {
        this.resolveInUse();
      },
    },
    'working.companyName'() {
      this.schedulePreview();
    },
    'working.identity'() {
      this.schedulePreview();
    },
    previewed() {
      this.schedulePreview();
    },
    version() {
      this.schedulePreview();
    },
  },
  created() {
    this.inUseTimer = null;
    this.resolveToken = 0;
    this.$root.$on('refresh-iframe', this.reloadURL);
    this.$root.$on('refresh-company-name', this.refreshCompanyName);
    this.$root.$on('refresh-body-class', this.setBodyClass);
    this.$root.$on('theme-edit-resolved', this.setEditing);
    this.$root.$on('theme-edit-closed', this.stopEditing);
    this.deviceQuery = window.matchMedia?.('(prefers-color-scheme: dark)');
    this.deviceDark = !!this.deviceQuery?.matches;
    this.deviceQuery?.addEventListener?.('change', this.deviceChanged);
    this.init();
  },
  mounted() {
    this.computeWidth();
  },
  beforeDestroy() {
    this.$root.$off('refresh-iframe', this.reloadURL);
    this.$root.$off('refresh-company-name', this.refreshCompanyName);
    this.$root.$off('refresh-body-class', this.setBodyClass);
    this.$root.$off('theme-edit-resolved', this.setEditing);
    this.$root.$off('theme-edit-closed', this.stopEditing);
    this.deviceQuery?.removeEventListener?.('change', this.deviceChanged);
    window.clearTimeout(this.timer);
    window.clearTimeout(this.inUseTimer);
  },
  methods: {
    init() {
      return Promise.all([
        this.$siteService.getSites('PORTAL', null, 'global', true, true, false, true, true, true, true, true, ['displayed', 'temporal'])
          .then(data => this.sites = data || []),
        // the preview opens on the admin's own display mode
        this.$brandingService.getDisplayMode()
          .then(display => this.selectedMode = display?.mode || 'AUTOMATIC')
          .catch(() => this.selectedMode = 'AUTOMATIC'),
      ]).then(this.computeWidth)
        .finally(() => this.initialized = true);
    },
    computeWidth() {
      if (this.$refs.iframeParent) {
        this.parentWidth = this.$refs.iframeParent.clientWidth;
      }
    },
    deviceChanged(event) {
      this.deviceDark = event.matches;
    },
    reloadURL() {
      const iframe = this.iframe();
      if (iframe) {
        iframe.src = this.pageHomeLink;
      }
    },
    setEditing({theme, resolved}) {
      this.editing = {theme, resolved};
    },
    stopEditing() {
      this.editing = null;
    },
    // the theme in use, as the page holds it, resolved by the server (debounced on edit)
    resolveInUse() {
      window.clearTimeout(this.inUseTimer);
      this.inUseTimer = window.setTimeout(() => {
        const theme = this.inUseTheme;
        if (theme) {
          const token = ++this.resolveToken;
          const stored = this.stored.find(item => item.id === theme.id)?.settings;
          this.$themeService.resolve(toResolvePayload(theme, stored))
            .then(resolved => token === this.resolveToken && (this.inUseResolved = resolved))
            .catch(() => token === this.resolveToken && (this.inUseResolved = null));
        }
      }, 400);
    },
    schedulePreview() {
      window.clearTimeout(this.timer);
      this.timer = window.setTimeout(this.applyPreview, 200);
    },
    iframe() {
      return document.getElementById('previewIframe');
    },
    // writes the version, the injected style, the identity and the display mode event into the iframe's document
    applyPreview() {
      const doc = this.iframe()?.contentDocument;
      const root = doc?.documentElement;
      const {theme, resolved} = this.previewed;
      if (!root || !doc.head) {
        return;
      }
      root.setAttribute('data-display-mode', this.version);
      if (resolved) {
        let style = doc.getElementById(STYLE_ID);
        if (!style) {
          style = doc.createElement('style');
          style.id = STYLE_ID;
        }
        // always the last of the head, after the page's own skin: equal specificity resolves to it
        doc.head.appendChild(style);
        style.textContent = buildPreviewStyle(resolved, this.version, theme, theme?.id || theme?.imageSourceId);
      }
      this.refreshCompanyName(this.working?.companyName);
      this.refreshCompanyLogo();
      doc.dispatchEvent(new CustomEvent('displayModeChanged', {detail: this.version}));
    },
    setBodyClass(event) {
      const body = this.iframe()?.contentWindow?.document?.body;
      if (body && event?.name) {
        body.classList.toggle(event.name, !!event.enabled);
      }
    },
    refreshCompanyName(name) {
      const element = name && this.iframe()?.contentWindow?.document?.getElementsByClassName?.('logoTitle')[0];
      if (element) {
        element.textContent = name;
      }
    },
    // the logo of the previewed version: the unsaved upload, else the stored one (the dark one falls back to the light one);
    // a logo the drawer removed is no logo, and the top bar renders none of its logo then: the preview hides it too
    refreshCompanyLogo() {
      const identity = this.working?.identity;
      const shown = file => file && !file.remove && (file.data || file.fileId) && file;
      const logo = this.version === 'dark' && shown(identity?.darkLogo) && 'darkLogo' || 'logo';
      const file = shown(identity?.[logo]);
      const src = file?.data && this.$utils.convertImageDataAsSrc(file.data)
        || file?.fileId && `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/${logo}?v=${file.fileId}`;
      // the company logo of the top bar, never the site icon or the space banner, which share its id
      const link = this.iframe()?.contentWindow?.document?.querySelector?.('.top-bar-company-logo');
      if (link?.style) {
        link.style.display = src ? '' : 'none';
      }
      const element = src && link?.getElementsByTagName('img')[0];
      if (element) {
        element.src = src;
        // a <picture> keeps its published dark <source>, which wins in a dark preview
        element.parentElement?.querySelectorAll?.('source')?.forEach(source => source.srcset = src);
      }
    },
  },
};
</script>

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
  <v-row class="ma-0">
    <!-- the page fills the window: the row takes the height the window leaves it (fitHeight), the settings column
         stretches to the row and the preview fills its own column. That column is hidden below lg with the preview it
         holds: on a line of its own, it would share the row's height with the settings column -->
    <v-col
      cols="12"
      lg="8"
      class="d-none d-lg-flex flex-column pa-0">
      <portal-general-settings-branding-site-preview
        :working="working"
        :stored="view && view.themes || []"
        class="flex-grow-1" />
    </v-col>
    <!-- Cancel and Apply close the settings column, at its bottom: beside the preview, the column is at least as high as
         it, and a row of their own would sit under the preview -->
    <v-col
      cols="12"
      lg="4"
      class="d-flex flex-column pa-0 ps-lg-6 border-box-sizing">
      <template v-if="working">
        <portal-general-settings-branding-identity
          :company-name="working.companyName"
          @edit="$refs.brandingDrawer.open()" />
        <portal-general-settings-themes-list
          :themes="working.themes"
          :theme-in-use-key="working.themeInUseKey"
          @select="working.themeInUseKey = $event"
          @add="$refs.themeAddDrawer.open()"
          @edit="$refs.themeEditionDrawer.open($event, false)"
          @restore="restoreTheme"
          @delete="deleteTheme" />
        <portal-general-settings-modes-offered v-model="working.modesOffered" />
      </template>
      <portal-general-settings-custom-style-input
        v-if="customStylesheetEnabled"
        v-model="customCss" />
      <div class="d-flex justify-end mt-auto pt-4">
        <v-btn
          :aria-label="$t('generalSettings.cancel')"
          :disabled="loading"
          class="btn cancel-button me-4"
          elevation="0"
          @click="$emit('close')">
          <span class="text-none">
            {{ $t('generalSettings.cancel') }}
          </span>
        </v-btn>
        <v-btn
          :aria-label="$t('generalSettings.apply')"
          :disabled="!changed"
          :loading="loading"
          color="primary"
          class="btn btn-primary register-button"
          elevation="0"
          @click="save">
          <span class="text-capitalize">
            {{ $t('generalSettings.apply') }}
          </span>
        </v-btn>
      </div>
    </v-col>
    <portal-general-settings-branding-drawer
      v-if="working"
      ref="brandingDrawer"
      :value="working"
      @apply="applyBranding" />
    <portal-general-settings-theme-add-drawer
      ref="themeAddDrawer"
      :themes="themes"
      :theme-in-use-key="working && working.themeInUseKey"
      @next="startTheme" />
    <portal-general-settings-theme-edition-drawer
      ref="themeEditionDrawer"
      :themes="themes"
      :stored="view && view.themes || []"
      :modes-offered="working && working.modesOffered"
      @apply="applyTheme" />
  </v-row>
</template>
<script>
import {createWorkingCopy, createWorkingTheme, createRestoredTheme, buildPublication, publicationHasChanges} from '../../js/ThemePublicationBuilder.js';

export default {
  props: {
    // the branding as the general settings loaded it: the custom CSS, the supported fonts
    branding: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    // the themes view of the server as the page loaded it, and the working copy edited until Apply
    view: null,
    working: null,
    snapshot: null,
    loading: false,
    customCss: null,
    // the custom CSS as saved, once this page saved it: the loaded branding is stale until it reloads
    savedCustomCss: null,
    // a custom CSS the server refused after a committed publication, kept in the field while the page reloads
    keptCustomCss: null,
    customStylesheetEnabled: eXo.env.portal.customStylesheetEnabled,
  }),
  computed: {
    themes() {
      return this.working?.themes || [];
    },
    // the page's working copy differs from what is published
    changed() {
      return !!this.working
        && (JSON.stringify(this.working) !== this.snapshot || this.customCss !== this.customCssBaseline);
    },
    customCssBaseline() {
      return this.savedCustomCss ? this.savedCustomCss.value : this.branding?.customCss || null;
    },
  },
  watch: {
    branding() {
      this.savedCustomCss = null;
      this.customCss = this.keptCustomCss ? this.keptCustomCss.value : this.branding?.customCss || null;
      this.keptCustomCss = null;
    },
    changed() {
      this.$emit('changed', this.changed);
    },
    'working.companyName'() {
      this.$root.$emit('refresh-company-name', this.working?.companyName);
    },
  },
  created() {
    this.fitFrame = null;
    this.$root.$on('general-settings-page-shown', this.scheduleFit);
    this.init();
  },
  mounted() {
    this.scheduleFit();
    window.addEventListener('resize', this.scheduleFit);
  },
  beforeDestroy() {
    this.$root.$off('general-settings-page-shown', this.scheduleFit);
    window.removeEventListener('resize', this.scheduleFit);
    window.cancelAnimationFrame(this.fitFrame);
  },
  methods: {
    scheduleFit() {
      window.cancelAnimationFrame(this.fitFrame);
      this.fitFrame = window.requestAnimationFrame(this.fitHeight);
    },
    // the row takes the height the window leaves it, so that the page fills the window without scrolling, Cancel and
    // Apply at its bottom: set higher than the height the page scrolls in (the window below the top bar) and than its
    // content, the row makes the page overflow by what surrounds it, and that height less that part is the row's least
    // height. A column taller than that makes the row taller, and the page scrolls
    fitHeight() {
      const row = this.$el;
      // the page scrolls in its site body (BodyScrollListener), else in the document
      const page = row?.closest?.('.site-scroll-parent') || document.scrollingElement;
      if (!row?.style || !page) {
        return;
      }
      const probe = page.clientHeight + row.offsetHeight;
      row.style.minHeight = `${probe}px`;
      const around = Math.max(page.scrollHeight - probe, 0);
      // written on the element only: a bound value, unchanged at a refit, would not render again over the probe
      row.style.minHeight = `${Math.max(page.clientHeight - around, 0)}px`;
    },
    init() {
      this.$root.loading = true;
      return this.$themeService.getThemes()
        .then(view => {
          const first = !this.view;
          this.view = view;
          this.working = createWorkingCopy(view);
          this.snapshot = JSON.stringify(this.working);
          // a reload after a conflict keeps what the administrator typed
          if (first) {
            this.customCss = this.customCssBaseline;
          }
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('generalSettings.themes.loadingError'), 'error'))
        .finally(() => this.$root.loading = false);
    },
    applyBranding(companyName, identity) {
      this.working.companyName = companyName;
      this.working.identity = identity;
    },
    startTheme(name, source) {
      this.$refs.themeEditionDrawer.open(createWorkingTheme(source, name, this.view?.themes), true);
    },
    // an edited theme replaces its working copy; a new one is added to the list, unselected
    applyTheme(theme, creating) {
      if (!creating && !theme.dirty) {
        // nothing changed: the draft comes back in another key order, which must not mark the page as changed
        return;
      }
      const index = this.working.themes.findIndex(item => item.key === theme.key);
      if (creating) {
        this.working.themes.push(theme);
      } else {
        this.working.themes.splice(index, 1, theme);
      }
    },
    restoreTheme(theme) {
      const index = this.working.themes.findIndex(item => item.key === theme.key);
      this.working.themes.splice(index, 1, createRestoredTheme(theme));
      this.$root.$emit('refresh-iframe');
    },
    deleteTheme(theme) {
      this.working.themes = this.working.themes.filter(item => item.key !== theme.key);
      if (theme.id) {
        this.working.deletedThemeIds.push(theme.id);
      }
      if (this.working.themeInUseKey === theme.key) {
        // the platform switches to the Default Theme
        this.working.themeInUseKey = this.working.themes.find(item => item.origin === 'DEFAULT')?.key;
      }
    },
    save() {
      this.$root.$emit('close-alert-message');
      this.$root.loading = true;
      this.loading = true;
      this.keptCustomCss = null;
      const publication = buildPublication(this.working, this.view);
      // the custom CSS is saved by its own request, after the publication: saving it moves the stylesheet version the
      // publication is checked against, so before it the publication would be refused as a conflict
      const customCss = this.customCss;
      const customCssChanged = customCss !== this.customCssBaseline;
      let published = false;
      return Promise.resolve(publicationHasChanges(publication))
        .then(changes => changes && this.$themeService.publish(publication).then(() => published = true))
        .then(() => !customCssChanged || this.$themeService.saveCustomCss(customCss)
          .then(() => {
            this.savedCustomCss = {value: customCss};
            return true;
          }, error => this.handleCustomCssError(error, published)))
        .then(saved => {
          if (saved) {
            this.$emit('saved');
            this.$root.$emit('alert-message', this.$t('generalSettings.savedSuccessfully'), 'success');
            this.$root.$emit('refresh-iframe');
            return this.init();
          }
        })
        .catch(error => this.handlePublishError(error))
        .finally(() => this.loading = this.$root.loading = false);
    },
    // a refusal of the custom CSS leaves the publication committed: the page, the preview and the view reload for the
    // version the next Apply is checked against, the CSS typed stays in its field, and the message names the CSS
    handleCustomCssError(error, published) {
      if (published) {
        this.keptCustomCss = {value: this.customCss};
        this.$emit('saved');
        this.$root.$emit('refresh-iframe');
      }
      return this.init().then(() => {
        const defaultLabel = published ? 'generalSettings.publish.error.customCssAfterPublication' : 'generalSettings.publish.error.customCss';
        this.$root.$emit('alert-message', this.errorMessage(error, defaultLabel), 'error');
        return false;
      });
    },
    handlePublishError(error) {
      if (error?.status === 409) {
        // someone published since this page loaded: show what is published now
        return this.init().then(() => this.$root.$emit('alert-message', this.$t('generalSettings.publish.error.conflict'), 'error'));
      }
      this.$root.$emit('alert-message', this.errorMessage(error, 'generalSettings.publish.error.default'), 'error');
    },
    // a 400 carries the code of its refusal, shown by its own label; any other failure by the default label given
    errorMessage(error, defaultLabel) {
      const code = error?.status === 400 && error.code || null;
      const label = code && `generalSettings.publish.error.${code.split(':')[0]}`;
      return label && this.$te(label) && this.$t(label, {0: code.split(':')[1] || ''})
        || this.$t(defaultLabel);
    },
  },
};
</script>

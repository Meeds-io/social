<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <div>
    <exo-drawer
      id="themeEditionDrawer"
      ref="drawer"
      v-model="drawer"
      allow-expand
      right>
      <template #title>
        {{ creating && $t('generalSettings.theme.add.title') || $t('generalSettings.theme.edition.title') }}
      </template>
      <template v-if="drawer" #content>
        <v-card class="pa-4" flat>
          <div v-if="creating" class="text-header mb-4">
            {{ $t('generalSettings.theme.add.step2') }}
          </div>
          <div class="text-header mb-2">
            {{ $t('generalSettings.theme.name.label') }}
          </div>
          <v-tooltip :disabled="!isDefault" bottom>
            <template #activator="{ on }">
              <div v-on="on">
                <v-text-field
                  id="themeEditionName"
                  v-model="name"
                  :disabled="isDefault"
                  :placeholder="$t('generalSettings.theme.name.placeholder')"
                  :error-messages="nameError"
                  class="border-box-sizing pt-0"
                  type="text"
                  aria-required="true"
                  outlined
                  dense />
              </div>
            </template>
            {{ $t('generalSettings.theme.default.rename.tooltip') }}
          </v-tooltip>
          <portal-general-settings-branding-options />
        </v-card>
      </template>
      <template #footer>
        <div class="d-flex justify-end">
          <v-btn
            class="themeEditionCancelButton btn ms-2"
            @click="cancel">
            {{ $t('generalSettings.button.cancel') }}
          </v-btn>
          <v-btn
            :disabled="!valid"
            class="themeEditionApplyButton btn btn-primary ms-2"
            @click="apply">
            {{ creating && $t('generalSettings.button.save') || $t('generalSettings.apply') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
    <portal-general-settings-branding-update-colors-drawer
      :theme-colors="{
        'primaryColor': primaryColor,
        'secondaryColor': secondaryColor
      }" />
    <portal-general-settings-branding-update-font-drawer
      :font-family="fontFamily"
      :font-families="supportedFontFamilies" />
    <portal-general-settings-branding-top-bar-styling-drawer :top-bar-styling-properties="topBarStylingProperties" />
    <portal-general-settings-branding-sidebar-styling-drawer :side-bar-styling-properties="sideBarStylingProperties" />
    <portal-general-settings-branding-drawer-styling :drawer-styling-properties="drawerStylingProperties" />
    <portal-general-settings-branding-page-styling-drawer :page-styling-properties="pageStylingProperties" />
  </div>
</template>
<script>
import ThemeStylingMixin from '../../../js/ThemeStylingMixin.js';
import ThemeEditSessionMixin from '../../../js/ThemeEditSessionMixin.js';
import {sameSettings} from '../../../js/ThemeSettingsMapper.js';

export default {
  mixins: [ThemeStylingMixin, ThemeEditSessionMixin],
  props: {
    // the page's themes, to keep a theme name unique
    themes: {
      type: Array,
      default: () => [],
    },
    // the themes as published, the base of the resolve call
    stored: {
      type: Array,
      default: () => [],
    },
    modesOffered: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    theme: null,
    creating: false,
    name: null,
    applied: false,
    // the edited theme's branding-shaped style, the model the area drawers start from
    branding: null,
  }),
  computed: {
    isDefault() {
      return this.theme?.origin === 'DEFAULT';
    },
    trimmedName() {
      return this.name?.trim() || '';
    },
    nameError() {
      const exists = this.themes.some(theme => theme.key !== this.theme?.key
        && theme.name.trim().toLowerCase() === this.trimmedName.toLowerCase());
      return exists && this.$t('generalSettings.theme.name.alreadyExists') || null;
    },
    valid() {
      return this.trimmedName && !this.nameError;
    },
  },
  watch: {
    // the preview shows the edited theme while this drawer is open
    'session.resolved'() {
      if (this.session.resolved && this.drawer) {
        this.$root.$emit('theme-edit-resolved', {theme: this.draft, resolved: this.session.resolved});
      }
    },
    drawer() {
      if (!this.drawer) {
        this.$root.$emit('theme-edit-closed');
        if (!this.applied) {
          // cancelled or closed: the area drawers previewed their edits, reload the preview without them
          this.$root.$emit('refresh-iframe');
        }
      }
    },
  },
  methods: {
    open(theme, creating) {
      this.theme = theme;
      this.creating = !!creating;
      this.name = theme.name;
      this.applied = false;
      return this.startSession(theme, this.modesOffered).then(() => {
        this.$refs.drawer.open();
        this.$root.$emit('theme-edit-resolved', {theme: this.draft, resolved: this.session.resolved});
      });
    },
    cancel() {
      this.$refs.drawer.close();
    },
    apply() {
      this.syncDraft();
      // a theme is published only when its name or its settings changed
      const dirty = this.theme.dirty || this.creating || this.trimmedName !== this.theme.name
        || !sameSettings(this.draft.settings, this.theme.settings);
      this.applied = true;
      this.$emit('apply', Object.assign({}, this.draft, {name: this.trimmedName, dirty}), this.creating);
      this.$refs.drawer.close();
    },
  },
};
</script>

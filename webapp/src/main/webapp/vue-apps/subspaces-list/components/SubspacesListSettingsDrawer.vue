<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <exo-drawer
    ref="drawer"
    :loading="loading"
    :right="!$vuetify.rtl">
    <template slot="title">
      <span class="text-color ma-auto">
        {{ $t('subspacesList.settings.drawer.title') }}
      </span>
    </template>
    <template slot="content">
      <div class="d-flex flex-column pa-4">
        <translation-text-field
          ref="headerTitleInput"
          :id="`${$root.settings.appId}HeaderTitleInput`"
          v-model="settings.headerTranslations"
          :default-language="$root.defaultLanguage"
          :placeholder="$t('subspacesList.settings.updateHeader.label')"
          drawer-title="subspacesList.settings.updateHeader.translation"
          no-expand-icon
          autofocus>
          <template #title>
            <span class="text-header">{{ $t('subspacesList.settings.updateHeader.label') }}</span>
          </template>
        </translation-text-field>
        <div class="d-flex align-center justify-space-between mt-6">
          <span class="text-header my-auto">
            {{ $t('subspacesList.settings.showHiddenSubspaces.label') }}
          </span>
          <v-switch
            v-model="settings.showHiddenSubspaces"
            :aria-label="$t('subspacesList.settings.showHiddenSubspaces.label')"
            color="primary"
            class="my-auto ms-4"
            hide-details />
        </div>
        <div class="d-flex align-center justify-space-between mt-6">
          <span class="text-header my-auto">
            {{ $t('subspacesList.settings.limit.label') }}
          </span>
          <number-input
            :key="openCount"
            v-model="settings.subspacesLimit"
            :label="$t('subspacesList.settings.limit.label')"
            :min="minLimit"
            :max="maxLimit"
            :step="1"
            class="ms-4" />
        </div>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex justify-end">
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('subspacesList.settings.drawer.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!canSave"
          :loading="loading"
          class="btn btn-primary"
          @click="save">
          {{ $t('subspacesList.settings.drawer.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
/**
 * The three preferences the portlet stores. The editable copy carries only
 * these, so the action URL receives nothing else — the server ignores any
 * other name anyway, but the URL-encoded body stays what the portlet reads.
 */
const PREFERENCE_NAMES = ['headerTranslations', 'showHiddenSubspaces', 'subspacesLimit'];

export default {
  data: () => ({
    // the same bounds as SubspacesListPortlet.MIN/MAX_SUBSPACES_LIMIT: the
    // portlet refuses a value outside them with a PortletException the
    // portal answers with a 200, so the drawer must not let one through.
    // The shared <number-input> steps within them (no free typing, as
    // designed); isValidLimit is the guard behind it
    minLimit: 1,
    maxLimit: 25,
    // the number input reads its value once, at creation: re-keyed per open
    // so a reopened drawer shows the current limit, not the first one
    openCount: 0,
    settings: {
      headerTranslations: {},
      showHiddenSubspaces: false,
      subspacesLimit: 4,
    },
    originalSettings: null,
    loading: false,
  }),
  computed: {
    hasHeaderTitle() {
      return Object.values(this.settings.headerTranslations || {})
        .some(value => typeof value === 'string' && value.trim().length > 0);
    },
    changed() {
      return JSON.stringify(this.settings) !== JSON.stringify(this.originalSettings);
    },
    canSave() {
      return !this.loading
        && this.changed
        && this.hasHeaderTitle
        && this.isValidLimit(this.settings.subspacesLimit);
    },
  },
  methods: {
    isValidLimit(value) {
      return Number.isInteger(value) && value >= this.minLimit && value <= this.maxLimit;
    },
    /**
     * Opens the drawer on a copy of the current preferences. An empty header
     * translation is seeded with the default label in the portal's default
     * language, so the field shows the value the widget actually renders and
     * the translate option has a base to translate from.
     *
     * @returns {void}
     */
    open() {
      const current = this.$root.settings || {};
      const headerTranslations = current.headerTranslations && Object.keys(current.headerTranslations).length
        ? JSON.parse(JSON.stringify(current.headerTranslations))
        : {[this.$root.defaultLanguage || this.$root.language]: this.$t('subspacesList.header.label')};
      this.settings = {
        headerTranslations,
        showHiddenSubspaces: !!current.showHiddenSubspaces,
        subspacesLimit: Number.isInteger(current.subspacesLimit) ? current.subspacesLimit : 4,
      };
      this.originalSettings = JSON.parse(JSON.stringify(this.settings));
      this.openCount++;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    /**
     * Posts the three preferences, then refreshes the root settings the
     * widget renders from and tells the list to reload with the new limit
     * and hidden flag.
     *
     * @returns {Promise<void>|undefined} the save request, or nothing when the
     *          form is not savable
     */
    save() {
      if (!this.canSave) {
        return;
      }
      this.loading = true;
      const toSave = {};
      PREFERENCE_NAMES.forEach(name => toSave[name] = this.settings[name]);
      return this.$subspacesListService.saveSettings(this.$root.settings.saveSettingsUrl, toSave)
        .then(() => {
          // the root settings are what the widget renders from: the header
          // title, the displayed slice and the next resource call's limit
          PREFERENCE_NAMES.forEach(name => this.$set(this.$root.settings, name, toSave[name]));
          this.$root.$emit('alert-message', this.$t('subspacesList.settings.saved.success'), 'success');
          this.$emit('saved', toSave);
          this.close();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('subspacesList.settings.saved.error'), 'error'))
        .finally(() => this.loading = false);
    },
  },
};
</script>

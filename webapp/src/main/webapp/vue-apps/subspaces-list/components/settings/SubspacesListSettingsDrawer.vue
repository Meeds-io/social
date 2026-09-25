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
      <span class="text-color ma-auto">{{ $t('subspacesList.settings.drawer.title') }}</span>
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
          <span class="text-header my-auto">{{ $t('subspacesList.settings.showHiddenSubspaces.label') }}</span>
          <v-switch
            v-model="settings.showHiddenSubspaces"
            :aria-label="$t('subspacesList.settings.showHiddenSubspaces.label')"
            color="primary"
            class="my-auto ms-4"
            hide-details />
        </div>
        <div class="d-flex align-center justify-space-between mt-6">
          <span class="text-header my-auto">{{ $t('subspacesList.settings.limit.label') }}</span>
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
        <v-btn class="btn me-2" @click="close">
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
// the three preferences the portlet stores: the posted body carries no other name
const PREFERENCE_NAMES = ['headerTranslations', 'showHiddenSubspaces', 'subspacesLimit'];

export default {
  props: {
    // reloads the widget for the given number of rows and re-seats
    // $root.settings on the stored preferences: awaited after a save, since it
    // tells whether the save was applied (Vue 2 calls no factory for a Function)
    refresh: {
      type: Function,
      default: () => Promise.resolve(),
    },
  },
  data: () => ({
    // SubspacesListPortlet.MIN/MAX_SUBSPACES_LIMIT: a value outside them is a
    // PortletException the portal answers with a 200, so none may go through
    minLimit: 1,
    maxLimit: 25,
    // <number-input> reads its value once: re-keyed per open
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
    changed() {
      return JSON.stringify(this.settings) !== JSON.stringify(this.originalSettings);
    },
    // an empty header is a valid choice (the widget has a default label):
    // only the limit can be out of what the portlet accepts
    canSave() {
      return !this.loading
        && this.changed
        && this.isValidLimit(this.settings.subspacesLimit);
    },
  },
  methods: {
    isValidLimit(value) {
      return Number.isInteger(value) && value >= this.minLimit && value <= this.maxLimit;
    },
    /**
     * Opens the drawer on a copy of the current preferences; an empty header
     * is seeded with the default label in the default language, so the field
     * shows what the widget renders and the translate option has a base.
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
     * Posts the three preferences, reloads the widget and reports what the
     * server stored. The action URL cannot say so: a refusal by the portlet
     * (role revoked, value rejected) is a PortletException the portal answers
     * with a 200. The snackbar follows the reloaded preferences, never the
     * HTTP status.
     *
     * @returns {Promise<void>|undefined} the save request, if the form is savable
     */
    save() {
      if (!this.canSave) {
        return;
      }
      this.loading = true;
      const toSave = {};
      PREFERENCE_NAMES.forEach(name => toSave[name] = this.settings[name]);
      return this.$subspacesListService.saveSettings(this.$root.settings.saveSettingsUrl, toSave)
        // the posted limit: the reload must bring back enough rows for it
        .then(() => this.refresh(toSave.subspacesLimit))
        .then(() => {
          if (this.isStored(toSave)) {
            this.$root.$emit('alert-message', this.$t('subspacesList.settings.saved.success'), 'success');
            this.close();
          } else {
            this.$root.$emit('alert-message', this.$t('subspacesList.settings.saved.error'), 'error');
          }
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('subspacesList.settings.saved.error'), 'error'))
        .finally(() => this.loading = false);
    },
    // whether the server stored every posted preference
    isStored(toSave) {
      const stored = this.$root.settings || {};
      return PREFERENCE_NAMES.every(name => this.canonical(stored[name]) === this.canonical(toSave[name]));
    },
    // a key-order-independent rendering: the stored translations come back
    // from a Java map in another order than they were posted
    canonical(value) {
      if (value === null || typeof value !== 'object' || Array.isArray(value)) {
        return JSON.stringify(value);
      }
      return JSON.stringify(Object.keys(value).sort().map(key => [key, this.canonical(value[key])]));
    },
  },
};
</script>

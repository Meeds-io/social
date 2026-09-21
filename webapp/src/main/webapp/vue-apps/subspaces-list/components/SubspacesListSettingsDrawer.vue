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
  props: {
    /**
     * Reloads the widget from the portlet resource and re-seats
     * {@code $root.settings} on the stored preferences, asking for the number
     * of rows given as its argument. Awaited after a save, because it is what
     * tells whether the save was applied.
     *
     * The default is the function itself, not a factory: Vue 2 only calls a
     * default when the declared type is not Function.
     */
    refresh: {
      type: Function,
      default: () => Promise.resolve(),
    },
  },
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
    changed() {
      return JSON.stringify(this.settings) !== JSON.stringify(this.originalSettings);
    },
    /**
     * An empty header is a valid choice, not an incomplete form: the board
     * gives the header a default value rather than making it mandatory, and
     * the widget falls back to the 'Subspaces' label when the translations
     * hold nothing. Only the limit can actually be out of what the portlet
     * accepts.
     *
     * @returns {boolean} whether the form may be posted
     */
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
     * Posts the three preferences, then reloads the widget and reports what
     * the server actually stored.
     *
     * The action URL cannot be trusted to say so: a refusal by the portlet
     * (role revoked since the envelope was computed, or a value the portlet
     * rejects) is a PortletException the portal logs and answers with a 200,
     * so a save that changed nothing is indistinguishable from a save that
     * worked — at the transport. The reload's envelope carries the stored
     * preferences, and those are compared with what was posted: the snackbar
     * follows the preferences, never the HTTP status.
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
        // the posted limit, not the stored one: the reload must bring back
        // enough rows for the limit this save is establishing
        .then(() => this.refresh(toSave.subspacesLimit))
        .then(() => {
          // $root.settings now holds what the reload read back from the
          // preferences, whatever was posted
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
    /**
     * Whether the reloaded preferences are the ones that were posted.
     *
     * @param {object} toSave the posted preference values, by name
     * @returns {boolean} true when the server stored every one of them
     */
    isStored(toSave) {
      const stored = this.$root.settings || {};
      return PREFERENCE_NAMES.every(name => this.canonical(stored[name]) === this.canonical(toSave[name]));
    },
    /**
     * A value as a string that does not depend on key order: the stored
     * translations come back from a Java map, whose iteration order is not
     * the one they were posted in, and a plain JSON.stringify would then call
     * an applied save a failure.
     *
     * @param {*} value the value to render comparable
     * @returns {string} its order-independent representation
     */
    canonical(value) {
      if (value === null || typeof value !== 'object' || Array.isArray(value)) {
        return JSON.stringify(value);
      }
      return JSON.stringify(Object.keys(value).sort().map(key => [key, this.canonical(value[key])]));
    },
  },
};
</script>

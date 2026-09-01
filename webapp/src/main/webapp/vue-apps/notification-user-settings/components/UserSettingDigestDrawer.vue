<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    class="userSettingDigestDrawer"
    right
    @closed="reset">
    <template #title>
      {{ $t('UserSettings.drawer.title.digest') }}
    </template>
    <template #content>
      <v-flex v-if="!loading" class="pa-4">
        <div class="d-flex align-center">
          <span class="text-color">{{ $t('UserSettings.drawer.label.dailyDigest') }}</span>
          <v-switch
            v-model="daily"
            :aria-label="$t('UserSettings.drawer.label.dailyDigest')"
            class="ms-auto my-auto me-0"
            hide-details />
        </div>
        <user-setting-digest-categories
          v-if="daily"
          v-model="dailyCategories"
          :categories="categories" />

        <div class="d-flex align-center mt-4">
          <span class="text-color">{{ $t('UserSettings.drawer.label.weeklyDigest') }}</span>
          <v-switch
            v-model="weekly"
            :aria-label="$t('UserSettings.drawer.label.weeklyDigest')"
            class="ms-auto my-auto me-0"
            hide-details />
        </div>
        <user-setting-digest-categories
          v-if="weekly"
          v-model="weeklyCategories"
          :categories="categories" />
      </v-flex>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-btn
          :disabled="saving"
          class="btn ms-auto me-2"
          @click="close">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disabled"
          :loading="saving"
          class="btn btn-primary"
          elevation="0"
          @click="save">
          {{ $t('UserSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    categories: [],
    daily: false,
    dailyCategories: [],
    weekly: false,
    weeklyCategories: [],
    loadedSettings: null,
  }),
  computed: {
    allCategoryIds() {
      return this.categories.map(category => category.id);
    },
    // The comparison is on the effective state: the categories of a frequency
    // that is off don't count, unchecking a frequency after having played with
    // its categories is going back to the initial state
    changed() {
      return this.loadedSettings
        && (this.daily !== this.loadedSettings.daily
          || this.weekly !== this.loadedSettings.weekly
          || this.daily && !this.sameCategories(this.dailyCategories, this.loadedSettings.dailyCategories)
          || this.weekly && !this.sameCategories(this.weeklyCategories, this.loadedSettings.weeklyCategories));
    },
    // Apply stays disabled until the user changes something, then follows the
    // server rule: an enabled frequency with no category would send an empty
    // digest, the server refuses it as well. Unchecking everything stays a
    // change to apply: it is how the user switches his digest off
    disabled() {
      return this.saving
        || this.loading
        || !this.changed
        || this.daily && !this.dailyCategories.length
        || this.weekly && !this.weeklyCategories.length;
    },
  },
  watch: {
    daily() {
      if (this.daily && !this.dailyCategories.length) {
        // A frequency enabled for the first time proposes everything the user
        // can receive, he then unchecks what he doesn't want. A frequency he
        // already tuned keeps his own choices
        this.dailyCategories = this.allCategoryIds.slice();
      }
    },
    weekly() {
      if (this.weekly && !this.weeklyCategories.length) {
        this.weeklyCategories = this.allCategoryIds.slice();
      }
    },
  },
  methods: {
    open() {
      this.reset();
      this.$refs.drawer.open();
      this.loading = true;
      return this.$digestService.getDigestSettings()
        .then(settings => {
          this.categories = settings?.categories || [];
          this.dailyCategories = settings?.dailyCategories || [];
          this.weeklyCategories = settings?.weeklyCategories || [];
          // The frequencies are set last, their watcher must see the
          // categories already loaded to leave the choices of the user alone
          this.daily = settings?.daily || false;
          this.weekly = settings?.weekly || false;
          // What the server holds right now: Apply wakes up when the choices
          // differ from it
          this.loadedSettings = {
            daily: this.daily,
            dailyCategories: this.dailyCategories.slice(),
            weekly: this.weekly,
            weeklyCategories: this.weeklyCategories.slice(),
          };
        })
        .catch(() => {
          this.$root.$emit('alert-message', this.$t('UserSettings.digest.error.load'), 'error');
          this.close();
        })
        .finally(() => this.loading = false);
    },
    close() {
      this.$refs.drawer.close();
    },
    reset() {
      this.categories = [];
      this.daily = false;
      this.dailyCategories = [];
      this.weekly = false;
      this.weeklyCategories = [];
      this.loadedSettings = null;
      this.saving = false;
    },
    sameCategories(categories, loadedCategories) {
      return categories.length === loadedCategories.length
        && categories.every(id => loadedCategories.includes(id));
    },
    save() {
      this.saving = true;
      const dailyCategories = this.daily && this.dailyCategories || [];
      const weeklyCategories = this.weekly && this.weeklyCategories || [];
      return this.$digestService.saveDigestSettings(this.daily, dailyCategories, this.weekly, weeklyCategories)
        .then(() => {
          this.$root.$emit('alert-message', this.$t('UserSettings.digest.success.save'), 'success');
          this.close();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('UserSettings.digest.error.save'), 'error'))
        .finally(() => this.saving = false);
    },
  },
};
</script>

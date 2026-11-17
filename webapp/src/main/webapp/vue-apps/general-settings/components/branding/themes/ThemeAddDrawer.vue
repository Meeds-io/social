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
  <exo-drawer
    id="themeAddDrawer"
    ref="drawer"
    v-model="drawer"
    allow-expand
    right>
    <template #title>
      {{ $t('generalSettings.theme.add.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <div class="text-header mb-4">
          {{ $t('generalSettings.theme.add.step1') }}
        </div>
        <div class="text-header mb-2">
          {{ $t('generalSettings.theme.name.label') }}
        </div>
        <v-text-field
          id="themeName"
          v-model="name"
          :placeholder="$t('generalSettings.theme.name.placeholder')"
          :error-messages="nameError"
          class="border-box-sizing pt-0"
          type="text"
          aria-required="true"
          autofocus
          outlined
          dense />
        <div class="text-header mt-4 mb-2">
          {{ $t('generalSettings.theme.startFrom.label') }}
        </div>
        <v-select
          id="themeStartFrom"
          v-model="startFromKey"
          :items="themes"
          item-text="name"
          item-value="key"
          class="border-box-sizing pt-0"
          hide-details
          outlined
          dense />
        <div class="text-subtitle mt-2">
          {{ $t('generalSettings.theme.startFrom.help') }}
        </div>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex justify-end">
        <v-btn
          class="themeAddCancelButton btn ms-2"
          @click="close">
          {{ $t('generalSettings.button.cancel') }}
        </v-btn>
        <v-tooltip :disabled="!!trimmedName" top>
          <template #activator="{ on }">
            <span v-on="on">
              <v-btn
                :disabled="!valid"
                class="themeAddNextButton btn btn-primary ms-2"
                @click="next">
                {{ $t('generalSettings.theme.next') }}
              </v-btn>
            </span>
          </template>
          {{ $t('generalSettings.theme.next.tooltip') }}
        </v-tooltip>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    themes: {
      type: Array,
      default: () => [],
    },
    themeInUseKey: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    name: null,
    startFromKey: null,
  }),
  computed: {
    trimmedName() {
      return this.name?.trim() || '';
    },
    nameExists() {
      return this.themes.some(theme => theme.name.trim().toLowerCase() === this.trimmedName.toLowerCase());
    },
    nameError() {
      return this.trimmedName && this.nameExists && this.$t('generalSettings.theme.name.alreadyExists') || null;
    },
    valid() {
      return this.trimmedName && !this.nameExists;
    },
  },
  methods: {
    open() {
      this.name = null;
      this.startFromKey = this.themeInUseKey;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    next() {
      const source = this.themes.find(theme => theme.key === this.startFromKey);
      this.$emit('next', this.trimmedName, source);
      this.close();
    },
  },
};
</script>

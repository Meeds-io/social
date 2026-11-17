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
  <div v-if="session && visibleRows.length" class="themeAutomaticRows mt-6">
    <div class="text-header mb-2">
      {{ $t('generalSettings.version.colors') }}
    </div>
    <v-list-item
      v-for="row in visibleRows"
      :key="row.key"
      :class="`themeColorRow themeColorRow-${row.key}`"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        <v-list-item-title>
          {{ $t(`generalSettings.themeKey.${row.key}`) }}
        </v-list-item-title>
        <v-list-item-subtitle v-if="row.ratio" :class="row.warning && 'error--text'">
          {{ row.ratio }}:1
          <span v-if="row.warning" class="themeContrastWarning">
            {{ $t('generalSettings.contrast.low') }}
          </span>
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action class="d-flex flex-row align-center my-auto">
        <span
          :style="{background: row.value}"
          class="themeColorSwatch border-color border-radius me-2"></span>
        <span v-if="!row.customized && !row.configured" class="themeAutomaticLabel text-subtitle me-2">
          {{ $t(dark && 'generalSettings.version.automatic' || 'generalSettings.version.auto') }}
        </span>
        <v-tooltip v-if="!row.customized && !row.configured && dark" bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              :aria-label="$t('generalSettings.version.customize')"
              class="themeCustomizeButton"
              icon
              @click="emitRowChange('theme-edit-customize', row.key)">
              <v-icon size="16" class="icon-default-color">fa-edit</v-icon>
            </v-btn>
          </template>
          {{ $t('generalSettings.version.customize') }}
        </v-tooltip>
        <v-tooltip v-else-if="row.customized" bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              :aria-label="$t(revertLabel(row))"
              class="themeBackToAutomaticButton"
              icon
              @click="emitRowChange('theme-edit-revert', row.key)">
              <v-icon size="16" class="icon-default-color">fa-redo</v-icon>
            </v-btn>
          </template>
          {{ $t(revertLabel(row)) }}
        </v-tooltip>
      </v-list-item-action>
    </v-list-item>
  </div>
</template>
<script>
export default {
  inject: {
    session: {
      from: 'themeEditSession',
      default: null,
    },
  },
  props: {
    // the color keys of the area
    keys: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    emitRowChange(event, key) {
      this.$root.$emit('theme-edit-commit');
      this.$root.$emit(event, key);
    },
    // a value set on this page over a configured one goes back to the configured value, not to Automatic or Auto
    revertLabel(row) {
      return row.configured && 'generalSettings.version.backToConfigured' || this.backLabel;
    },
  },
  computed: {
    dark() {
      return this.session?.version === 'dark';
    },
    backLabel() {
      return this.dark && 'generalSettings.version.backToAutomatic' || 'generalSettings.version.auto';
    },
    // a text or icon color reads Auto in both versions; every dark color reads Automatic
    visibleRows() {
      const version = this.session.version;
      const resolved = this.session.resolved;
      const ratios = resolved?.[`${version}Ratios`] || {};
      const warnings = resolved?.[`${version}Warnings`] || [];
      return this.keys
        .filter(key => this.dark || key in ratios || /(Text.*Color|IconColor)$/.test(key))
        .map(key => ({
          key,
          value: resolved?.[version]?.[key] || null,
          ratio: ratios[key] || null,
          warning: warnings.includes(key),
          customized: this.session.settings?.[version]?.[key] != null,
          configured: this.session.configSettings?.[version]?.[key] != null,
        }));
    },
  },
};
</script>

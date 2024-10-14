<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <div
    v-if="!hasSettings && isAdmin"
    class="d-flex">
    <v-btn
      class="btn ma-auto btn-primary"
      flat
      outlined
      @click="openChartSettingsDrawer">
      <v-icon
        class="me-1"
        size="16">
        fas fa-sitemap
      </v-icon>
      {{ $t('organizationalChart.configure.label') }}
    </v-btn>
  </div>
  <v-sheet
    v-else
    height="28"
    class="d-flex mb-2">
    <p
      v-if="hasHeaderTitle"
      class="my-auto widget-text-header text-truncate">
      {{ configuredTitle }}
    </p>
    <div
      class="ms-auto d-flex">
      <div>
        <v-btn
          v-if="hover || isMobile"
          class="ms-auto my-0 icon-default-color me-5"
          :loading="isPrintingPdf"
          :title="$t('organizationalChart.download.label')"
          small
          icon
          @click="downloadChart">
          <v-icon
            size="20">
            fas fa-download
          </v-icon>
        </v-btn>
      </div>
      <div v-if="isAdmin">
        <v-btn
          v-if="hover || isMobile"
          class="ms-auto my-0 icon-default-color"
          small
          icon
          @click="openChartSettingsDrawer">
          <v-icon
            size="20">
            fas fa-cog
          </v-icon>
        </v-btn>
      </div>
    </div>
  </v-sheet>
</template>

<script>
export default {
  props: {
    hasSettings: {
      type: Boolean,
      default: false
    },
    configuredTitle: {
      type: String,
      default: null
    },
    isAdmin: {
      type: Boolean,
      default: false
    },
    hover: {
      type: Boolean,
      default: false
    },
    isMobile: {
      type: Boolean,
      default: false
    },
    hasHeaderTitle: {
      type: Boolean,
      default: false
    },
    isPrintingPdf: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    openChartSettingsDrawer() {
      this.$emit('open-chart-settings');
    },
    downloadChart() {
      this.$emit('download-chart');
    }
  }
};
</script>

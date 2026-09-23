<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    <div class="d-flex align-center">
      <div
        class="text-header me-auto">
        {{ $t('layout.border') }}
      </div>
      <v-switch
        v-model="enabled"
        class="ms-auto my-auto me-n2" />
    </div>
    <v-list-item
      v-if="enabled"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('layout.borderColor') }}
      </v-list-item-content>
      <v-list-item-action class="my-auto me-0 ms-auto">
        <styling-color-picker
          v-model="borderColor"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
    <v-list-item
      v-if="enabled"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('layout.borderSize') }}
      </v-list-item-content>
      <v-list-item-action class="my-auto me-0 ms-auto">
        <number-input
          v-model="borderSize"
          :step="1"
          :min="0"
          :max="8"
          class="me-n3" />
      </v-list-item-action>
    </v-list-item>
    <v-list-item
      v-if="enabled"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('layout.boxShadow') }}
      </v-list-item-content>
      <v-list-item-action class="my-auto me-0 ms-auto">
        <v-checkbox v-model="boxShadow" />
      </v-list-item-action>
    </v-list-item>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    pageStyle: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    container: null,
    initialized: false,
    enabled: true,
    borderColor: '#FFFFFF',
    borderSize: 1,
    boxShadow: false,
  }),
  watch: {
    borderColor() {
      if (this.initialized) {
        this.$set(this.container, 'borderColor', this.borderColor);
        this.$emit('refresh');
      }
    },
    borderSize() {
      if (this.initialized) {
        this.$set(this.container, 'borderSize', this.borderSize);
        this.$emit('refresh');
      }
    },
    boxShadow() {
      if (this.initialized) {
        this.$set(this.container, 'boxShadow', this.boxShadow);
        this.$emit('refresh');
      }
    },
    enabled(val) {
      if (val) {
        if (!this.borderColor) {
          this.borderColor = '#FFFFFF';
          this.borderSize = 1;
        }
      } else {
        this.boxShadow = null;
        this.borderColor = null;
        this.borderSize = 0;
      }
    },
  },
  created() {
    this.container = this.value;
    this.borderColor = this.container.borderColor;
    this.borderSize = this.container.borderSize || 0;
    this.boxShadow = this.container.boxShadow === 'true';
    this.enabled = !!this.borderColor;
    this.$nextTick().then(() => this.initialized = true);
  },
};
</script>
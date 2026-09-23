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
    <slot name="title"></slot>
    <div v-if="!$slots.title" class="d-flex align-center mb-2">
      <div
        :class="textBold && 'font-weight-bold' || 'text-header'"
        class="me-auto">
        {{ label || $t('layout.margins') }}
      </div>
      <v-switch
        v-model="enabled"
        class="ms-auto my-auto me-n2" />
    </div>
    <div
      v-if="canUpdateValue"
      class="d-flex flex-column">
      <v-list-item
        v-if="top"
        class="pa-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.top') }}
        </v-list-item-content>
        <number-input
          v-model="marginTop"
          :max="max"
          :min="min"
          class="my-auto"
          editable />
      </v-list-item>
      <v-list-item
        v-if="right"
        class="pa-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.right') }}
        </v-list-item-content>
        <number-input
          v-model="marginRight"
          :max="max"
          :min="min"
          class="my-auto"
          editable />
      </v-list-item>
      <v-list-item
        v-if="bottom"
        class="pa-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.bottom') }}
        </v-list-item-content>
        <number-input
          v-model="marginBottom"
          :max="max"
          :min="min"
          class="my-auto"
          editable />
      </v-list-item>
      <v-list-item
        v-if="left"
        class="pa-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.left') }}
        </v-list-item-content>
        <number-input
          v-model="marginLeft"
          :max="max"
          :min="min"
          class="my-auto"
          editable />
      </v-list-item>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    max: {
      type: Number,
      default: () => 60,
    },
    min: {
      type: Number,
      default: () => -60,
    },
    top: {
      type: Boolean,
      default: true,
    },
    right: {
      type: Boolean,
      default: false,
    },
    bottom: {
      type: Boolean,
      default: true,
    },
    left: {
      type: Boolean,
      default: false,
    },
    textBold: {
      type: Boolean,
      default: false,
    },
    // Header text; the generic "Margins" label when not set
    label: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    container: null,
    initialized: false,
    enabled: true,
    marginTop: null,
    marginRight: null,
    marginBottom: null,
    marginLeft: null,
  }),
  computed: {
    canUpdateValue() {
      return this.enabled || this.$slots.title;
    },
  },
  watch: {
    value() {
      if (!this.container) {
        this.container = this.value;
      }
    },
    marginTop() {
      if (this.initialized) {
        this.$set(this.container, 'marginTop', this.canUpdateValue ? this.marginTop || 0 : null);
        this.$emit('refresh');
      }
    },
    marginRight() {
      if (this.initialized && this.right) {
        this.$set(this.container, 'marginRight', this.canUpdateValue ? this.marginRight || 0 : null);
        this.$emit('refresh');
      }
    },
    marginBottom() {
      if (this.initialized) {
        this.$set(this.container, 'marginBottom', this.canUpdateValue ? this.marginBottom || 0 : null);
        this.$emit('refresh');
      }
    },
    marginLeft() {
      if (this.initialized && this.left) {
        this.$set(this.container, 'marginLeft', this.canUpdateValue ? this.marginLeft || 0 : null);
        this.$emit('refresh');
      }
    },
    canUpdateValue() {
      if (this.initialized) {
        this.marginTop = this.canUpdateValue ? 0 : null;
        this.marginRight = this.canUpdateValue ? 0 : null;
        this.marginBottom = this.canUpdateValue ? 0 : null;
        this.marginLeft = this.canUpdateValue ? 0 : null;
      }
    },
  },
  created() {
    this.container = this.value;
    this.marginTop = this.container.marginTop;
    this.marginRight = this.right && this.container.marginRight || 0;
    this.marginBottom = this.container.marginBottom;
    this.marginLeft = this.left && this.container.marginLeft || 0;
    this.enabled = (this.marginTop || this.container.marginTop === 0)
      || (this.marginBottom || this.container.marginBottom === 0)
      || (this.marginLeft || this.container.marginLeft === 0)
      || (this.marginRight || this.container.marginRight === 0) ? true : false;
    this.$nextTick().then(() => this.initialized = true);
  },
};
</script>
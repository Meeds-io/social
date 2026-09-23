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
        {{ $t('layout.borderRadius') }}
      </div>
      <v-switch
        v-model="enabled"
        class="ms-auto my-auto me-n2" />
    </div>
    <div
      v-if="enabled"
      :class="choice === 'same' && 'flex-row' || 'flex-column'"
      class="d-flex">
      <v-radio-group v-model="choice" class="my-auto text-no-wrap ms-n1">
        <v-radio
          :label="$t('layout.sameForAllCorners')"
          value="same"
          class="mx-0" />
        <v-radio
          :label="$t('layout.differentForEachCorner')"
          value="different"
          class="mx-0" />
      </v-radio-group>
      <v-list-item class="pe-0 ps-7 py-0" dense>
        <v-list-item-content v-if="choice === 'different'" class="my-auto">
          {{ $t('layout.topRight') }}
        </v-list-item-content>
        <number-input
          v-model="radiusTopRight"
          :class="choice === 'different' && 'my-auto' || 'mb-auto ms-auto'"
          class="me-n3" />
      </v-list-item>
      <v-list-item
        v-if="choice === 'different'"
        class="pe-0 ps-7 py-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.topLeft') }}
        </v-list-item-content>
        <number-input
          v-model="radiusTopLeft"
          class="my-auto me-n3" />
      </v-list-item>
      <v-list-item
        v-if="choice === 'different'"
        class="pe-0 ps-7 py-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.bottomRight') }}
        </v-list-item-content>
        <number-input
          v-model="radiusBottomRight"
          class="my-auto me-n3" />
      </v-list-item>
      <v-list-item
        v-if="choice === 'different'"
        class="pe-0 ps-7 py-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.bottomLeft') }}
        </v-list-item-content>
        <number-input
          v-model="radiusBottomLeft"
          class="my-auto me-n3" />
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
    pageStyle: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    container: null,
    initialized: false,
    enabled: true,
    choice: 'same',
    radiusTopRight: null,
    radiusTopLeft: null,
    radiusBottomRight: null,
    radiusBottomLeft: null,
  }),
  watch: {
    radiusTopRight() {
      if (this.initialized) {
        this.$set(this.container, 'radiusTopRight', this.radiusTopRight);
        this.$emit('refresh');
        if (this.choice === 'same') {
          this.radiusTopLeft = this.radiusTopRight;
          this.radiusBottomRight = this.radiusTopRight;
          this.radiusBottomLeft = this.radiusTopRight;
        }
      }
    },
    radiusTopLeft() {
      if (this.initialized) {
        this.$set(this.container, 'radiusTopLeft', this.radiusTopLeft);
        this.$emit('refresh');
      }
    },
    radiusBottomRight() {
      if (this.initialized) {
        this.$set(this.container, 'radiusBottomRight', this.radiusBottomRight);
        this.$emit('refresh');
      }
    },
    radiusBottomLeft() {
      if (this.initialized) {
        this.$set(this.container, 'radiusBottomLeft', this.radiusBottomLeft);
        this.$emit('refresh');
      }
    },
    enabled() {
      if (this.initialized) {
        const defaultBorderRadius = parseInt(this.$root.branding?.themeStyle?.borderRadius?.replace?.('px', '') || '4');
        this.choice = 'same';
        this.radiusTopRight = this.enabled && defaultBorderRadius || null;
        this.radiusTopLeft = this.radiusTopRight;
        this.radiusBottomRight = this.radiusTopRight;
        this.radiusBottomLeft = this.radiusTopRight;
      }
    },
    choice() {
      if (this.choice === 'same') {
        this.radiusTopLeft = this.radiusTopRight;
        this.radiusBottomRight = this.radiusTopRight;
        this.radiusBottomLeft = this.radiusTopRight;
      }
    },
  },
  created() {
    this.container = this.value;
    this.radiusTopRight = this.container.radiusTopRight;
    this.radiusTopLeft = this.container.radiusTopLeft;
    this.radiusBottomRight = this.container.radiusBottomRight;
    this.radiusBottomLeft = this.container.radiusBottomLeft;
    this.enabled = this.radiusBottomLeft === 0 || !!this.radiusBottomLeft;
    this.choice =
      this.radiusTopRight === this.radiusTopLeft
      && this.radiusBottomRight === this.radiusTopLeft
      && this.radiusTopLeft === this.radiusBottomLeft
      && this.radiusBottomLeft === this.radiusTopRight ? 'same' : 'different';
    this.$nextTick().then(() => this.initialized = true);
  },
};
</script>
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
  <v-dialog
    ref="dialog"
    v-model="modal"
    color="white"
    width="290px">
    <template #activator="{ on }">
      <v-list-item
        class="pa-0"
        dense>
        <v-list-item-action class="my-0 me-2 layout-color-picker-swatch">
          <v-card
            :color="displayedColor"
            :class="inheritedCaptionShown && 'opacity-4'"
            height="36px"
            width="36px"
            v-on="on" />
        </v-list-item-action>
        <v-list-item-content class="my-auto">
          <v-card
            v-if="label"
            :min-width="minTextWidth"
            flat>
            <div class="text-body">{{ label }}</div>
            <div
              v-if="inheritedCaptionShown"
              class="text-subtitle disabled--text">
              {{ $t('layout.inherited') }}
            </div>
            <div v-else class="text-subtitle">{{ displayedColor }}</div>
          </v-card>
          <v-card
            v-else-if="inheritedCaptionShown"
            :min-width="minTextWidth"
            class="text-body text-end disabled--text"
            flat>
            {{ $t('layout.inherited') }}
          </v-card>
          <v-card
            v-else
            :min-width="minTextWidth"
            class="text-body text-end"
            flat>
            {{ displayedColor }}
          </v-card>
        </v-list-item-content>
      </v-list-item>
    </template>
    <v-color-picker
      v-model="color"
      :swatches="swatches"
      mode="hexa"
      show-swatches />
    <v-row class="mx-0 white">
      <v-col class="center">
        <v-btn
          text
          color="primary"
          @click="cancel">
          {{ $t('layout.cancel') }}
        </v-btn>
      </v-col>
      <v-col class="center">
        <v-btn
          text
          color="primary"
          @click="save">
          {{ $t('layout.ok') }}
        </v-btn>
      </v-col>
    </v-row>
  </v-dialog>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    label: {
      type: String,
      default: null,
    },
    minTextWidth: {
      type: String,
      default: () => 'auto',
    },
    // Colour shown while no value is set: the one the element inherits, never stored as long as the user keeps it
    placeholder: {
      type: String,
      default: null,
    },
    // Renders the placeholder dimmed with an 'Inherited' caption instead of as a plain value
    inheritedCaption: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    modal: false,
    color: null,
    originalValue: null,
    swatches: [
      ['#FF0000', '#319ab3', '#f97575'],
      ['#98cc81', '#4273c8', '#cea6ac'],
      ['#bc99e7', '#9ee4f5', '#774ea9'],
      ['#ffa500', '#bed67e', '#0E100F'],
      ['#ffaacc', '#0000AA', '#000055'],
    ],
  }),
  computed: {
    inherited() {
      return !this.value && !!this.placeholder;
    },
    inheritedCaptionShown() {
      return this.inherited && this.inheritedCaption;
    },
    displayedColor() {
      return this.value || this.placeholder;
    },
  },
  watch: {
    modal() {
      if (this.modal) {
        this.originalValue = this.value;
        if (this.inherited) {
          this.color = this.placeholder;
        }
      }
    },
    value() {
      this.color = this.value;
    },
  },
  created() {
    this.color = this.value;
  },
  methods: {
    cancel() {
      this.$emit('input', this.originalValue);
      this.modal = false;
    },
    save() {
      // An inherited colour confirmed as it is stays inherited: only a colour the user picked is stored
      const unchangedPlaceholder = this.inherited && this.sameColor(this.color, this.placeholder);
      this.$emit('input', unchangedPlaceholder ? null : this.color);
      this.modal = false;
    },
    sameColor(first, second) {
      return this.normalizeColor(first) === this.normalizeColor(second);
    },
    normalizeColor(color) {
      // #RGB, #RRGGBB and #RRGGBBAA compared as #RRGGBBAA, case-insensitive
      let hex = String(color || '').replace('#', '').toUpperCase();
      if (hex.length === 3) {
        hex = hex.split('').map(c => c + c).join('');
      }
      return hex.length === 6 ? `${hex}FF` : hex;
    },
  }
};
</script>

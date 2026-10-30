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
    :disabled="disabled"
    color="white"
    width="290px">
    <template #activator="{ on, attrs }">
      <!-- A caller may draw its own activator, bound to on and attrs; color is the colour shown, or null -->
      <slot
        v-if="$scopedSlots.activator"
        name="activator"
        :on="on"
        :attrs="attrs"
        :color="displayedColor"></slot>
      <v-list-item
        v-else
        class="pa-0"
        dense>
        <v-list-item-action class="my-0 me-2 color-picker-swatch">
          <v-card
            :color="displayedColor || 'transparent'"
            :height="swatchSize"
            :width="swatchSize"
            :aria-label="ariaLabel || label || $t('colorPicker.change')"
            :aria-disabled="disabled"
            v-bind="attrs"
            v-on="on"
            @keydown.enter.prevent="$event.target.click()"
            @keydown.space.prevent="$event.target.click()" />
        </v-list-item-action>
        <v-list-item-content class="my-auto">
          <v-card
            v-if="label"
            :min-width="minTextWidth"
            flat>
            <div class="text-body">{{ label }}</div>
            <div class="text-subtitle">{{ displayedColor }}</div>
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
    <v-row
      v-if="resettable"
      class="mx-0 white">
      <v-col class="center pb-0">
        <v-btn
          text
          color="primary"
          @click="reset">
          {{ $t('colorPicker.reset') }}
        </v-btn>
      </v-col>
    </v-row>
    <v-row class="mx-0 white">
      <v-col class="center">
        <v-btn
          text
          color="primary"
          @click="cancel">
          {{ $t('colorPicker.cancel') }}
        </v-btn>
      </v-col>
      <v-col class="center">
        <v-btn
          text
          color="primary"
          @click="save">
          {{ $t('colorPicker.ok') }}
        </v-btn>
      </v-col>
    </v-row>
  </v-dialog>
</template>
<script>
// The shapes v-color-picker parses: any other string is re-emitted as garbage
const PICKABLE_COLOR = /^(#([0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8})|transparent)$/i;

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
    // The swatch's accessible name, for a screen holding several pickers. Defaults to the label
    ariaLabel: {
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
    swatchSize: {
      type: String,
      default: () => '36px',
    },
    swatches: {
      type: Array,
      default: () => [
        ['#FF0000', '#319ab3', '#f97575'],
        ['#98cc81', '#4273c8', '#cea6ac'],
        ['#bc99e7', '#9ee4f5', '#774ea9'],
        ['#ffa500', '#bed67e', '#0E100F'],
        ['#ffaacc', '#0000AA', '#000055'],
      ],
    },
    // Adds a 'Reset to default' action, which emits an empty value
    resettable: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    // Offers no transparency and emits #RRGGBB, for the screens that store opaque colors
    opaque: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    modal: false,
    color: null,
  }),
  computed: {
    inherited() {
      return !this.value && !!this.placeholder;
    },
    displayedColor() {
      return this.value || this.placeholder;
    },
  },
  watch: {
    modal() {
      if (this.modal) {
        this.color = this.initialColor();
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
    initialColor() {
      // Never null, which v-color-picker answers with a colour object instead of a string. From a #RRGGBB start it
      // returns #RRGGBB and hides its transparency slider; from any other start, #RRGGBBAA with the slider
      const color = [this.value, this.inherited && this.placeholder].find(color => color && PICKABLE_COLOR.test(color))
        || `#${this.normalizeColor(this.swatches?.[0]?.[0] || '#FFFFFF')}`;
      return this.opaque ? `#${this.normalizeColor(color).substring(0, 6)}` : color;
    },
    cancel() {
      // Nothing was emitted since the dialog opened: closing it leaves the value as it is
      this.modal = false;
    },
    save() {
      // Confirmed as the dialog opened, the value stays as it is: a stored colour is not given back in the shape the
      // picker normalised it to, and an inherited one is not stored. Only a colour the user picked is emitted
      const openedOnColor = [this.value, this.inherited && this.placeholder].some(color => color && PICKABLE_COLOR.test(color));
      if (!openedOnColor || !this.sameColor(this.color, this.initialColor())) {
        const color = this.opaque && this.color?.length === 9 ? this.color.substring(0, 7) : this.color;
        this.$emit('input', color);
      }
      this.modal = false;
    },
    reset() {
      this.$emit('input', '');
      this.modal = false;
    },
    sameColor(first, second) {
      return this.normalizeColor(first) === this.normalizeColor(second);
    },
    normalizeColor(color) {
      // Compared as the picker re-emits them (#RRGGBBAA, upper case): #RGB and #RGBA expanded, 'transparent' as #00000000
      if (String(color || '').trim().toLowerCase() === 'transparent') {
        return '00000000';
      }
      let hex = String(color || '').replace('#', '').toUpperCase();
      if (hex.length === 3 || hex.length === 4) {
        hex = hex.split('').map(c => c + c).join('');
      }
      return hex.length === 6 ? `${hex}FF` : hex;
    },
  }
};
</script>

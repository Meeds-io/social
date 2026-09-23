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
    <div class="d-flex align-center mb-2">
      <div
        :class="textBold && 'font-weight-bold' || 'text-header'"
        class="me-auto">
        {{ $t('layout.margins') }}
      </div>
      <v-switch
        v-model="enabled"
        class="ms-auto my-auto me-n2" />
    </div>
    <div
      v-if="enabled"
      :class="marginChoice === 'same' && 'flex-row' || 'flex-column'"
      class="d-flex">
      <v-radio-group v-model="marginChoice" class="my-auto text-no-wrap ms-n1">
        <v-radio
          :label="$t('layout.sameForAllSides')"
          value="same"
          class="mx-0" />
        <v-radio
          :label="$t('layout.differentForEachSide')"
          value="different"
          class="mx-0" />
      </v-radio-group>
      <v-list-item class="pe-0 ps-7 py-0" dense>
        <v-list-item-content
          v-if="marginChoice === 'different'"
          class="my-auto">
          {{ $t('layout.top') }}
        </v-list-item-content>
        <number-input
          v-model="marginTop"
          :diff="diff"
          :max="max"
          :min="min"
          :class="marginChoice === 'different' && 'my-auto' || 'mb-auto ms-auto'"
          class="me-n3" />
      </v-list-item>
      <v-list-item
        v-if="marginChoice === 'different'"
        class="pe-0 ps-7 py-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.right') }}
        </v-list-item-content>
        <number-input
          v-model="marginRight"
          :diff="diff"
          :max="max"
          :min="min"
          class="my-auto me-n3" />
      </v-list-item>
      <v-list-item
        v-if="marginChoice === 'different'"
        class="pe-0 ps-7 py-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.bottom') }}
        </v-list-item-content>
        <number-input
          v-model="marginBottom"
          :diff="diff"
          :max="max"
          :min="min"
          class="my-auto me-n3" />
      </v-list-item>
      <v-list-item
        v-if="marginChoice === 'different'"
        class="pe-0 ps-7 py-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('layout.left') }}
        </v-list-item-content>
        <number-input
          v-model="marginLeft"
          :diff="diff"
          :max="max"
          :min="min"
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
    // Offset between the stored value and the displayed number; 0 since eXIP 7.3.0.30, the stored
    // value being on the platform scale where 20 means "no extra margin" (a legacy container is
    // converted on read by the editors, see LayoutUtils.parseContainerStyle)
    diff: {
      type: Number,
      default: () => 0,
    },
    max: {
      type: Number,
      default: () => 80,
    },
    min: {
      type: Number,
      default: () => -40,
    },
    textBold: {
      type: Boolean,
      default: false,
    },
    // Prefix of the model fields to edit: '' for the container's own margins
    // (marginTop...), 'app' for the default margins of its applications (appMarginTop...)
    fieldPrefix: {
      type: String,
      default: '',
    },
    // Value meaning "no extra margin": switching the input on starts from it, all sides equal to it = off
    neutral: {
      type: Number,
      default: () => 20,
    },
  },
  data: () => ({
    container: null,
    initialized: false,
    enabled: true,
    marginChoice: 'same',
    marginTop: 20,
    marginRight: 20,
    marginBottom: 20,
    marginLeft: 20,
  }),
  watch: {
    marginTop() {
      if (this.initialized) {
        this.$set(this.container, this.field('marginTop'), this.enabled ? (this.marginTop ?? this.neutral) : null);
        this.$emit('refresh');
        if (this.enabled && this.marginChoice === 'same') {
          this.marginRight = this.marginTop;
          this.marginBottom = this.marginTop;
          this.marginLeft = this.marginTop;
        }
      }
    },
    marginRight() {
      if (this.initialized) {
        this.$set(this.container, this.field('marginRight'), this.enabled ? (this.marginRight ?? this.neutral) : null);
        this.$emit('refresh');
      }
    },
    marginBottom() {
      if (this.initialized) {
        this.$set(this.container, this.field('marginBottom'), this.enabled ? (this.marginBottom ?? this.neutral) : null);
        this.$emit('refresh');
      }
    },
    marginLeft() {
      if (this.initialized) {
        this.$set(this.container, this.field('marginLeft'), this.enabled ? (this.marginLeft ?? this.neutral) : null);
        this.$emit('refresh');
      }
    },
    enabled() {
      if (this.initialized) {
        this.marginChoice = 'same';
        this.marginTop = this.enabled ? this.neutral : null;
        this.marginRight = this.enabled ? this.neutral : null;
        this.marginBottom = this.enabled ? this.neutral : null;
        this.marginLeft = this.enabled ? this.neutral : null;
        this.$emit('refresh');
      }
    },
    marginChoice() {
      if (this.initialized && this.enabled) {
        this.marginTop = this.marginTop ?? this.neutral;
        this.marginRight = this.marginTop;
        this.marginBottom = this.marginTop;
        this.marginLeft = this.marginTop;
        this.container[this.field('marginTop')] = this.marginTop;
      }
    },
  },
  created() {
    this.container = this.value;
    this.marginTop = this.container[this.field('marginTop')] ?? this.neutral;
    this.marginRight = this.container[this.field('marginRight')] ?? this.neutral;
    this.marginBottom = this.container[this.field('marginBottom')] ?? this.neutral;
    this.marginLeft = this.container[this.field('marginLeft')] ?? this.neutral;
    this.marginChoice =
      this.marginTop === this.marginRight
      && this.marginRight === this.marginLeft
      && this.marginLeft === this.marginBottom ? 'same' : 'different';
    this.enabled = this.marginChoice !== 'same' || this.marginRight !== this.neutral;
    this.$nextTick().then(() => this.initialized = true);
  },
  methods: {
    field(name) {
      return this.fieldPrefix ? `${this.fieldPrefix}${name.charAt(0).toUpperCase()}${name.slice(1)}` : name;
    },
  },
};
</script>
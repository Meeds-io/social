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
  <v-card
    :max-width="unit && 'auto' || maxWidth"
    class="d-flex flex-row align-center justify-center"
    flat>
    <v-btn
      icon
      @click="decrementNumber">
      <v-icon class="icon-default-color">fa-minus fa-sm</v-icon>
    </v-btn>
    <input
      v-if="editable"
      v-model="num"
      :aria-label="label"
      :step="step"
      :min="min"
      :max="max"
      :class="valid && 'text-color' || 'error-color'"
      :style="inputStyle"
      type="text"
      class="pa-0 ma-0 text-center">
    <div v-else>{{ num }}</div>
    <div v-if="unit" class="ps-1">{{ unit }}</div>
    <v-btn
      icon
      @click="incrementNumber">
      <v-icon class="icon-default-color">fa-plus fa-sm</v-icon>
    </v-btn>
  </v-card>
</template>
<script>
export default {
  props: {
    value: {
      type: Number,
      default: () => 0,
    },
    label: {
      type: String,
      default: null,
    },
    min: {
      type: Number,
      default: () => 0,
    },
    max: {
      type: Number,
      default: () => 20,
    },
    step: {
      type: Number,
      default: () => 4,
    },
    editable: {
      type: Boolean,
      default: false,
    },
    diff: {
      type: Number,
      default: () => 0,
    },
    unit: {
      type: String,
      default: null,
    },
    maxWidth: {
      type: Number,
      default: () => 88,
    },
    inputWidth: {
      type: Number,
      default: () => 36,
    },
  },
  data: () => ({
    num: 20,
    valid: false,
    initialized: false,
  }),
  computed: {
    inputStyle() {
      return `width: ${this.inputWidth}px;`
    },
  },
  watch: {
    num() {
      if (!this.initialized) {
        return;
      } else if (this.min && Number(this.num) < Number(this.min)) {
        this.$emit('input', Number(this.min) + this.diff);
        this.valid = false;
      } else if (this.max && Number(this.num) > Number(this.max)) {
        this.$emit('input', Number(this.max) + this.diff);
        this.valid = false;
      } else {
        this.$emit('input', Number(this.num) + this.diff);
        this.valid = true;
      }
    },
    valid: {
      immediate: true,
      handler() {
        this.$emit('valid', this.valid);
      },
    },
  },
  created() {
    this.num = (this.value || 0) - this.diff;
    this.adjust();
  },
  mounted() {
    this.initialized = true;
  },
  methods: {
    adjust() {
      if (this.min && Number(this.num) < Number(this.min)) {
        this.num = Number(this.min);
      } else if (this.max && Number(this.num) > Number(this.max)) {
        this.num = Number(this.max);
      }
    },
    decrementNumber() {
      this.adjust();
      if (Number(this.num) > this.min) {
        this.num = Number(this.num) - this.step;
      }
    },
    incrementNumber() {
      this.adjust();
      if (Number(this.num) < this.max) {
        this.num = Number(this.num) + this.step;
      }
    },
  },
};
</script>

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
    <div class="text-header mb-2">{{ label || $t('nodeIconPickerDrawer.label') }}</div>
    <div class="d-flex align-center">
      <v-icon size="40" class="icon-default-color">{{ value }}</v-icon>
      <v-btn
        class="primary-border-color ms-4"
        color="primary"
        elevation="0"
        outlined
        @click="edit">
        {{ $t('nodeIconPickerDrawer.edit') }}
      </v-btn>
      <font-icon-drawer
        v-if="drawer"
        ref="drawer"
        v-model="icon"
        @closed="drawer = false" />
    </div>
  </div>
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
  },
  data: () => ({
    icon: null,
    drawer: false,
  }),
  watch: {
    value: {
      immediate: true,
      handler() {
        this.icon = this.value || 'fa-question-circle';
      },
    },
    icon() {
      if (this.icon !== this.value) {
        this.$emit('input', this.icon);
      }
    },
  },
  methods: {
    edit() {
      this.drawer = true;
      this.$nextTick().then(() => this.$refs.drawer.open());
    },
  },
};
</script>
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
  <v-col
    cols="4"
    class="pa-0 text-center">
    <v-btn
      :value="value"
      active-class="btn-border-active"
      :class="colorClass"
      class="border-radius border-color-primary-grey pa-0 d-flex flex-column align-center mx-auto"
      width="50"
      height="50"
      icon
      @click="$emit('select', value)">
      <v-icon size="20">
        {{ icon }}
      </v-icon>
    </v-btn>
    <v-sheet
      max-width="100"
      :class="{'text-color': isSelected}"
      class="mt-2 mx-auto text-subtitle text-center text-truncate">
      {{ label }}
    </v-sheet>
  </v-col>
</template>

<script>

export default {
  props: {
    icon: {
      type: String,
      default: null
    },
    label: {
      type: String,
      default: null
    },
    value: {
      type: String,
      default: null
    },
    colorClass: {
      type: String,
      default: null
    },
    selectedStatus: {
      type: String,
      default: null
    }
  },
  computed: {
    isSelected() {
      return this.selectedStatus === this.value;
    }
  }
};
</script>

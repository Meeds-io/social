<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <div class="d-flex mt-8">
    <v-btn 
      text
      small
      @click="bordeRadius = 0">
      {{ $t('generalSettings.widgetAndAppStyle.square') }}
    </v-btn>
    <v-slider
      v-model="bordeRadius"
      :thumb-size="24"
      :max="maxBorderRadius"
      tick-size="2"
      thumb-label="always"
      ticks
      min="0"
      step="4"
      color="primary" />
    <v-btn
      text
      small
      @click="bordeRadius = maxBorderRadius"> 
      {{ $t('generalSettings.widgetAndAppStyle.rounded') }} 
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    bordeRadius: 8,
    maxBorderRadius: 20
  }),
  watch: {
    value() {
      this.bordeRadius = this.value;
    },
    bordeRadius() {
      this.$emit('input', this.bordeRadius);
    },
  },
  created() {
    this.bordeRadius = this.value;
  }
};
</script>

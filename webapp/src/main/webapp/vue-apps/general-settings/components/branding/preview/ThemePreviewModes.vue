<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <v-btn-toggle
    :value="value"
    class="previewModes"
    mandatory
    @change="$emit('input', $event)">
    <v-btn
      v-for="mode in modes"
      :key="mode.value"
      :aria-label="$t(mode.label)"
      :title="$t(mode.label)"
      :class="mode.cssClass"
      :value="mode.value"
      small
      icon>
      <v-icon size="16">{{ mode.icon }}</v-icon>
    </v-btn>
  </v-btn-toggle>
</template>
<script>
export default {
  props: {
    // AUTOMATIC, LIGHT or DARK
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    modes: Object.freeze([
      {value: 'AUTOMATIC', icon: 'fas fa-desktop', cssClass: 'previewModeAutomatic', label: 'generalSettings.preview.mode.automatic'},
      {value: 'LIGHT', icon: 'fas fa-sun', cssClass: 'previewModeLight', label: 'generalSettings.preview.mode.light'},
      {value: 'DARK', icon: 'fas fa-moon', cssClass: 'previewModeDark', label: 'generalSettings.preview.mode.dark'},
    ]),
  }),
};
</script>

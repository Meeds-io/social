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
  <div class="modesOffered mb-6">
    <div class="text-header">
      {{ $t('generalSettings.modes.title') }}
    </div>
    <v-radio-group
      :value="value"
      class="mt-2"
      hide-details
      @change="$emit('input', $event)">
      <v-radio
        v-for="mode in modes"
        :key="mode.value"
        :value="mode.value"
        :class="`modeRadio ${mode.cssClass}`"
        class="my-1">
        <template #label>
          <div class="d-flex flex-column">
            <span>{{ $t(mode.label) }}</span>
            <span class="text-subtitle">{{ $t(mode.help) }}</span>
          </div>
        </template>
      </v-radio>
    </v-radio-group>
    <div class="text-subtitle mt-2">
      {{ $t('generalSettings.modes.appliesToThemeInUse') }}
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
  },
  data: () => ({
    modes: Object.freeze([
      {
        value: 'LIGHT_AND_DARK',
        cssClass: 'lightAndDarkMode',
        label: 'generalSettings.modes.lightAndDark',
        help: 'generalSettings.modes.lightAndDark.help',
      },
      {
        value: 'LIGHT',
        cssClass: 'lightOnlyMode',
        label: 'generalSettings.modes.light',
        help: 'generalSettings.modes.single.help',
      },
      {
        value: 'DARK',
        cssClass: 'darkOnlyMode',
        label: 'generalSettings.modes.dark',
        help: 'generalSettings.modes.single.help',
      },
    ]),
  }),
};
</script>

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
  <div v-if="session" class="themeVersionBar mb-4">
    <v-btn-toggle
      :value="session.version"
      class="themeVersionToggle"
      mandatory
      @change="$emit('change', $event)">
      <v-btn
        class="themeVersionLight"
        value="light"
        small>
        <v-icon size="16" class="me-2">fa-sun</v-icon>
        <span class="text-none">{{ $t('generalSettings.version.light') }}</span>
      </v-btn>
      <v-btn
        class="themeVersionDark"
        value="dark"
        small>
        <v-icon size="16" class="me-2">fa-moon</v-icon>
        <span class="text-none">{{ $t('generalSettings.version.dark') }}</span>
      </v-btn>
    </v-btn-toggle>
    <div v-if="session.version === 'dark'" class="themeVersionHelp text-subtitle mt-2">
      {{ $t('generalSettings.version.dark.help') }}
    </div>
    <div v-if="!offered" class="themeVersionNotOffered text-subtitle mt-2">
      <v-icon size="14" class="me-1">fa-info-circle</v-icon>
      {{ $t('generalSettings.version.notOffered') }}
    </div>
  </div>
</template>
<script>
export default {
  inject: {
    session: {
      from: 'themeEditSession',
      default: null,
    },
  },
  computed: {
    // a single mode offered leaves the other version unseen by users
    offered() {
      const modes = this.session?.modesOffered;
      return !modes || modes === 'LIGHT_AND_DARK' || modes === this.session.version.toUpperCase();
    },
  },
};
</script>

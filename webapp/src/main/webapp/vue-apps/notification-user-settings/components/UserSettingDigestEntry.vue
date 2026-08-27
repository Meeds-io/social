<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<template>
  <div>
    <v-list-item two-line>
      <v-list-item-content>
        <v-list-item-title class="text-color">
          {{ $t('UserSettings.title.digest') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('UserSettings.subtitle.digest') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-tooltip bottom>
          <template #activator="{on, bind}">
            <v-btn
              v-on="on"
              v-bind="bind"
              :aria-label="$t('UserSettings.title.digest')"
              small
              icon
              @click="openDigestDrawer">
              <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('UserSettings.subtitle.digest') }}</span>
        </v-tooltip>
      </v-list-item-action>
    </v-list-item>
    <v-divider class="mx-4" />
  </div>
</template>

<script>
export default {
  methods: {
    openDigestDrawer() {
      // The digest drawer (US03) lives in this same vue app and listens to
      // this root event
      this.$root.$emit('openDigestSettingsDrawer');
    },
  },
};
</script>

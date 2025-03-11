<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-app class="d-flex align-center">
    <widget-wrapper
      key="admins"
      ref="admins"
      :title="$t('social.admins.label')">
      <template #action>
        <div class="position-relative">
          <exo-user-avatars-list
            class="absolute-vertical-center"
            :class="$vuetify.rtl && 'l-0' || 'r-0'"
            clickable
            compact
            :default-length="adminsCount"
            :icon-size="33"
            :margin-left="admins.length > 1 && 'ml-n5' || ''"
            :max="3"
            :users="admins"
            @open-detail="$root.$emit('admins-drawer-open')" />
        </div>
      </template>
    </widget-wrapper>
    <admins-drawer />
  </v-app>
</template>
<script>
  export default {
    data: () => ({
      admins: [],
      adminsCount: 0,
    }),
    created () {
      this.$root.$on('platform-settings-admins-updated', this.refreshAdmins);
    },
    beforeUnmount () {
      this.$root.$off('platform-settings-admins-updated', this.refreshAdmins);
    },
    methods: {
      refreshAdmins (adminsCount, admins) {
        this.admins = admins;
        this.adminsCount = adminsCount;
      },
    },
  };
</script>

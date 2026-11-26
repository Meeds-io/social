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
      :title="$t('social.admins.label')"
      ref="admins"
      key="admins"
      extra-class="application-body">
      <template #action>
        <v-btn
          id="adminsWidgetAddButton"
          :title="$t('social.admins.drawer.adminsToAdd')"
          :aria-label="$t('social.admins.drawer.adminsToAdd')"
          small
          icon
          @click="$root.$emit('admins-add-drawer-open', true)">
          <v-icon size="18">fa-plus</v-icon>
        </v-btn>
      </template>
      <div v-if="admins?.length" class="d-flex justify-start mt-2">
        <platform-avatars-see-all
          id="adminsWidgetAvatarsButton"
          :users="admins"
          :count="adminsCount"
          :label="$t('social.admins.button.seeAdmins')"
          @open="$root.$emit('admins-drawer-open')" />
      </div>
    </widget-wrapper>
    <admins-drawer />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    admins: [],
    adminsCount: 0
  }),
  created() {
    this.$root.$on('platform-settings-admins-updated', this.refreshAdmins);
  },
  beforeDestroy() {
    this.$root.$off('platform-settings-admins-updated', this.refreshAdmins);
  },
  methods: {
    refreshAdmins(adminsCount, admins) {
      this.admins = admins;
      this.adminsCount = adminsCount;
    },
  }
};
</script>

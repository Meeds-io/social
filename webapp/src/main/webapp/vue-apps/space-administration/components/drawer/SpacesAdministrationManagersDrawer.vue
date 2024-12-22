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
  <exo-drawer
    id="SpaceManagersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    allow-expand
    no-x-scroll
    right>
    <template #title>
      {{ $t('social.spaces.administration.manageSpaces.admins.drawerTitle', {
        0: spaceName,
      }) }}
    </template>
    <template v-if="drawer && managers" #content>
      <div class="pa-4">
        <user-avatar
          v-for="m in managers"
          :key="m.username"
          :identity="m"
          class="mb-4" />
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    managers: null,
    spaceName: null,
  }),
  created() {
    this.$root.$on('space-administration-managers-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-managers-drawer-open', this.open);
  },
  methods: {
    open(managers, spaceName) {
      this.managers = managers || [];
      this.spaceName = spaceName;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>
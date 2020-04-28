<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
  <exo-drawer ref="managersDrawer" right>
    <template slot="title">
      {{ $t('spacesList.title.managers') }}
    </template>
    <template v-if="space && space.managers" slot="content">
      <v-layout column class="ma-3">
        <v-flex
          v-for="manager in space.managers"
          :key="manager.id"
          class="flex-grow-1 text-truncate mb-1">
          <exo-user-avatar
            :username="manager.username"
            :fullname="manager.fullname"
            :title="manager.fullname" />
        </v-flex>
      </v-layout>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    space: null,
  }),
  mounted() {
    this.$root.$on('displaySpaceManagers', space => {
      this.space = space;
      this.$refs.managersDrawer.open();
    });
  }
};
</script>
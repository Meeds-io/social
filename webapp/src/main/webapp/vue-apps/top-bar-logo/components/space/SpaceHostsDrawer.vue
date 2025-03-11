<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
    ref="managersDrawer"
    right>
    <template #title>
      {{ $t('spacesList.title.managers') }}
    </template>
    <template
      v-if="displaySpaceHosts"
      #content>
      <v-layout
        class="ma-3"
        column>
        <exo-user-avatar
          v-for="host in displaySpaceHosts"
          :key="host.id"
          :extra-class="'my-2'"
          popover
          :profile-id="host.userName" />
      </v-layout>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    data: () => ({
      displaySpaceHosts: null,
    }),
    mounted () {
      this.$root.$on('displaySpaceHosts', displaySpaceHosts => {
        this.displaySpaceHosts = displaySpaceHosts;
        this.$refs.managersDrawer.open();
      });
    },
  };
</script>
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
  <v-list
    dense
    min-width="90%"
    class="pb-0">
    <v-list-item-group
      aria-label="displayedSites"
      link>
      <div
        v-for="site in sites"
        :key="site.name">
        <site-hamburger-navigation
          v-if="site.metaSite"
          :extra-class="'pt-0'"
          :navigations="site.siteNavigations" 
          :site="site.name" />
        <site-hamburger-item
          v-else
          :key="site.name"
          :site="site"
          :opened-site="openedSite" />
      </div>
    </v-list-item-group> 
  </v-list>
</template>

<script>
export default {
  props: {
    sites: {
      type: Array,
      default: () => []
    },
    openedSite: {
      type: Object,
      default: null,
    },
  },
};
</script>
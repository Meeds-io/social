<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
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
  <v-app>
    <v-tabs
      class="mt-1"
      v-model="tab"
      fixed-tabs>
      <navigation-menu-item
        v-for="navigation in navigations"
        :key="navigation.id"
        :navigation="navigation" />
    </v-tabs>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
    navigations: [],
    scope: 'ALL',
    globalScope: 'children',
    visibility: 'displayed',
    siteType: 'PORTAL',
    tab: location.pathname
  }),
  created() {
    this.getNavigations();
  },
  methods: {
    getNavigations() {
      const siteName = eXo.env.portal.portalName;
      return this.$navigationService.getNavigations(siteName, this.siteType, this.globalScope, this.visibility)
        .then(navigations => {
          if (navigations.length) {
            const homeNavigation = navigations[0];
            return this.$navigationService.getNavigations(siteName, this.siteType, this.scope, this.visibility, homeNavigation.id)
              .then(navigations => {
                this.navigations = navigations || [];
                this.navigations.map(navigation => {
                  navigation.uri = homeNavigation.uri;
                  this.navigations.push(...navigation.children);
                  navigation.children = [];
                });
              });
          }
        });
    },
  }
};
</script>
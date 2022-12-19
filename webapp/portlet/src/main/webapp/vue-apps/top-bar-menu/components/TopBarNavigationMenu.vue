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
      v-model="tab"
      fixed-tabs
      center-active
      height="56"
      slider-size="3">
      <navigation-menu-item
        v-for="navigation in navigations"
        :key="navigation.id"
        :navigation="navigation"
        :base-site-uri="`${BASE_SITE_URI}${navigations[0].name}/`"
        @update-navigation-state="updateNavigationState" />
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
    tab: sessionStorage.getItem('topNavigationTabState')
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
                  this.navigations.push(...navigation.children);
                  navigation.children = [];
                });
              });
          }
        });
    },
    updateNavigationState(value) {
      sessionStorage.setItem('topNavigationTabState',  value);
    }
  }
};
</script>
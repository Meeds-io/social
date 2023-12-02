<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
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
  <v-treeview
    id="siteNavigationTree"
    :open.sync="openLevel"
    :items="navigations"
    :active="active"
    active-class="v-item--active v-list-item--active"
    class="treeView-item my-2"
    item-key="name"
    hoverable
    activatable
    open-on-click
    transition
    dense>
    <template #label="{ item }">
      <site-navigation-item :navigation="item" :enable-change-home="enableChangeHome" />
    </template>
  </v-treeview>
</template>

<script>
export default {
  props: {
    navigations: {
      type: Array,
      default: null,
    },
    siteName: {
      type: String,
      default: null,
    },
    enableChangeHome: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    selectedNodeUri: eXo.env.portal.selectedNodeUri,
    currentSite: eXo.env.portal.portalName,
  }),
  computed: {
    openLevel() {
      const ids = [];
      if (this.navigations?.length) {
        this.navigations.forEach(nav => {
          ids.push(nav.name);
          ids.push(...nav.children?.length && nav.children?.map(nav => nav.name) || []);
        });
      }
      const splittedCurrentUri = this.selectedNodeUri.split('/');
      ids.push (...splittedCurrentUri);
      return ids;
    },
    active() {
      if (this.siteName !== this.currentSite) {
        return [];
      }
      const splittedCurrentUri = this.selectedNodeUri.split('/');
      return [splittedCurrentUri[splittedCurrentUri.length -1]];
    },
  },
};
</script>
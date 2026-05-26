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
    v-if="navigationTree"
    id="siteNavigationTree"
    :open.sync="openLevel"
    :items="navigationTree"
    :active="active"
    active-class="v-list-item--active"
    class="treeView-item list-border-active my-2"
    item-key="uri"
    hoverable
    activatable
    open-on-click
    transition
    dense>
    <template #label="{ item }">
      <site-navigation-item
        :navigation="item"
        :enable-change-home="enableChangeHome"
        :enable-unread="firstNavigationId === item.id" />
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
    collapsed: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    selectedNodeUri: eXo.env.portal.selectedNodeUri,
    currentSite: eXo.env.portal.siteKeyName,
  }),
  computed: {
    openLevel() {
      if (this.currentSite === this.siteName) {
        const ids = [];
        const splittedCurrentUri = this.selectedNodeUri.split('/');
        for (let i = 1; i < splittedCurrentUri.length; i++) {
          ids.push(splittedCurrentUri.slice(0, i).join('/'));
        }
        return ids;
      } else if (this.collapsed) {
        return [];
      } else {
        const ids = [];
        if (this.navigations?.length) {
          this.navigations.forEach(nav => {
            ids.push(nav.name);
            ids.push(...nav.children?.length && nav.children?.map(nav => nav.uri) || []);
          });
        }
        const splittedCurrentUri = this.selectedNodeUri.split('/');
        ids.push (...splittedCurrentUri);
        return ids;
      }
    },
    navigationTree() {
      if (this.navigations?.length === 1) {
        const navigations = JSON.parse(JSON.stringify(this.navigations));
        const rootNavigation = navigations[0];
        const rootNavigationChildren = navigations[0]?.children || [];
        rootNavigation.children = [];
        return this.filterNodes([rootNavigation, ...rootNavigationChildren]);
      } else {
        return this.navigations;
      }
    },
    firstNavigationId() {
      return this.navigations?.[0]?.id;
    },
    active() {
      if (this.siteName !== this.currentSite) {
        return [];
      }
      return [this.selectedNodeUri];
    },
  },
  methods: {
    filterNodes(navigations) {
      if (navigations?.length) {
        return navigations.map(n => {
          n.children = this.filterNodes(n.children);
          if (n.children?.length
              || n.pageLink
              || n.pageKey) {
            return n;
          } else {
            return [];
          }
        }).filter(n => n.children?.length || n.pageLink || n.pageKey);
      } else {
        return [];
      }
    },
  },
};
</script>
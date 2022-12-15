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
  <v-menu
    v-model="showMenu"
    rounded
    offset-y>
    <template #activator="{ attrs, on }">
      <v-tab
        v-bind="attrs"
        :href="`${BASE_SITE_URI}${navigation.uri}`"
        link>
        {{ navigation.name }}
        <v-btn
          v-if="navigation.children.length"
          v-on="navigation.children.length && on"
          icon
          @click.stop.prevent>
          <v-icon size="20">
            fa-angle-down
          </v-icon>
        </v-btn>
      </v-tab>
    </template>
    <v-list>
      <v-list-item
        v-for="children in navigation.children"
        :key="children.id"
        :href="`${BASE_SITE_URI}${children.uri}`"
        link>
        <v-list-item-title v-text="children.name" />
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script>
export default {
  data () {
    return {
      BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
      showMenu: false,
    };
  },
  props: {
    navigation: {
      type: Object,
      default: null,
    }
  },
  created() {
    document.addEventListener('click', () => {
      if (this.showMenu) {
        setTimeout(() => {
          this.showMenu = false;
        },100);
      }
    });
  }
};
</script>
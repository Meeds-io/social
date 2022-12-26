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
  <v-list
    class="pa-0"
    dense>
    <v-list-item
      class="pt-0 pb-0"
      v-for="children in navigation"
      :key="children.id"
      :href="`${baseSiteUri}${children.uri}`"
      :disabled="!children.pageKey && !children.children?.length"
      @click.stop="checkLink(children, $event)"
      :link="!!children.pageKey">
      <v-menu
        content-class="topBar-navigation-drop-sub-menu"
        rounded
        offset-x>
        <template #activator="{ attrs, on }">
          <v-list-item-title
            class="pt-5 pb-5 text-caption"
            v-bind="attrs"
            v-text="children.label" />
          <v-list-item-icon
            v-if="children.children?.length"
            class="ms-0 me-n2 ma-auto full-height">
            <v-btn
              v-on="children.children?.length && on"
              icon
              @click.stop.prevent>
              <v-icon
                size="18">
                {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
              </v-icon>
            </v-btn>
          </v-list-item-icon>
        </template>
        <navigation-menu-sub-item
          :navigation="children.children"
          :base-site-uri="baseSiteUri" />
      </v-menu>
    </v-list-item>
  </v-list>
</template>

<script>
export default {
  data() {
    return {
      showMenu: false,
    };
  },
  props: {
    navigation: {
      type: Object,
      default: null
    },
    baseSiteUri: {
      type: String,
      default: null
    },
    parentNavigationUri: {
      type: String,
      default: null
    }
  },
  methods: {
    checkLink(navigation, e) {
      if (!navigation.pageKey) {
        e.preventDefault();
      } else {
        this.$emit('update-navigation-state', `${this.parentNavigationUri}`);
      }
    }
  }
};
</script>
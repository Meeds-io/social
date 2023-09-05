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
  <v-list-item
    :href="uri"
    :target="target">
    <v-list-item-icon class-name="flex align-center flex-grow-0">
      <v-icon v-if="navigation.icon"> {{ icon }}</v-icon>
      <i v-else :class="iconClass"></i>
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title
        class="subtitle-2"
        v-text="navigation.label" />
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    navigation: {
      type: Object,
      default: null
    }
  },
  computed: {
    uri() {
      return this.navigation?.pageLink && this.urlVerify(this.navigation.pageLink) || `/portal/${this.navigation.siteKey.name}/${this.navigation.uri}`;
    },
    target() {
      return this.navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    icon() {
      return `fas ${this.navigation?.icon}`;
    },
    iconClass() {
      const capitilizedName = `${this.navigation.name[0].toUpperCase()}${this.navigation.name.slice(1)}`;
      return `uiIcon uiIconFile uiIconToolbarNavItem uiIcon${capitilizedName} icon${capitilizedName} ${this.navigation.icon}`;
    }
  },
  methods: {
    urlVerify(url) {
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
  }
};
</script>

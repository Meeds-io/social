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
  <v-list-item :href="uri" class="ps-0">
    <v-list-item-icon class="flex align-center flex-grow-0 my-2">
      <v-icon v-if="siteRootNode.icon"> {{ icon }}</v-icon>
      <i v-else :class="iconClass"></i>
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title
        class="subtitle-2"
        v-text="site.displayName" />
    </v-list-item-content>
  </v-list-item>
</template>

<script>
export default {
  props: {
    site: {
      type: Object,
      default: null
    }
  },
  data: () => ({
    BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
  }),
  computed: {
    uri() {
      return this.site?.siteNavigations && this.site?.siteNavigations[0]?.uri && this.site?.siteNavigations[0].pageLink && this.urlVerify(this.site?.siteNavigations[0].pageLink) || `/portal/${this.site.name}/${this.site?.siteNavigations[0].uri}`;
    },
    icon() {
      return `fas ${this.site?.siteNavigations && this.site?.siteNavigations[0]?.icon}`;
    },
    siteRootNode() {
      return this.site?.siteNavigations && this.site?.siteNavigations[0];
    },
    iconClass() {
      const capitilizedName = `${this.siteRootNode.name[0].toUpperCase()}${this.siteRootNode.name.slice(1)}`;
      return `uiIcon uiIconFile uiIconToolbarNavItem uiIcon${capitilizedName} icon${capitilizedName} ${this.siteRootNode.icon}`;
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

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
    v-if="!displaySequentially"
    @click="openOrCloseDrawer"
    :class="isCurrentSite && ' v-item--active v-list-item--active ' || ' '">
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
  <v-list-item
    v-else
    :href="uri"
    :target="target"
    :class="itemClass"
    @mouseover="showItemActions = true"
    @mouseleave="showItemActions = false">
    <v-list-item-icon class="flex align-center flex-grow-0 my-2">
      <v-icon v-if="siteRootNode.icon"> {{ icon }}</v-icon>
      <i v-else :class="iconClass"></i>
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title
        class="subtitle-2"
        v-text="site.displayName" />
    </v-list-item-content>
    <v-list-item-action
      v-if="toggleArrow"
      class="my-auto align-center">
      <ripple-hover-button
        class="ms-2 "
        :active="!drawerOpened"
        icon
        @ripple-hover="openOrCloseDrawer()">
        <v-icon
          class="me-0 pa-2 icon-default-color clickable"
          small>
          {{ arrowIcon }}
        </v-icon>
      </ripple-hover-button>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  props: {
    site: {
      type: Object,
      default: null
    },
    openedSite: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    showItemActions: false,
    currentPortal: eXo.env.portal.portalName
    ,
  }),
  computed: {
    uri() {
      return !this.siteRootNode?.pageKey  ? null :  this.siteRootNode?.pageLink && this.urlVerify(this.siteRootNode.pageLink) || `/portal/${this.site.name}/${this.siteRootNode.uri}`;
    },
    target() {
      return this.siteRootNode?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    icon() {
      return this.siteRootNode.icon;
    },
    siteRootNode() {
      return this.site?.siteNavigations?.length && this.findSiteRootNode(this.site.siteNavigations);
    },
    itemClass() {
      return ` ${this.isCurrentSite && ' v-item--active v-list-item--active ' || ' ' } ${this.siteRootNode?.pageKey && ' clickable ' || ' not-clickable '}` ;
    },
    iconClass() {
      const capitilizedName = (this.siteRootNode?.name?.length || 0) > 1 && `${this.siteRootNode.name[0].toUpperCase()}${this.siteRootNode.name.slice(1)}` || '';
      return `uiIcon uiIconFile uiIconToolbarNavItem uiIcon${capitilizedName} icon${capitilizedName} ${this.siteRootNode.icon}`;
    },
    toggleArrow() {
      return this.showItemActions || this.drawerOpened;
    },
    drawerOpened() {
      return this.openedSite?.name === this.site?.name;
    },
    arrowIcon() {
      return this.drawerOpened && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$root.ltr && 'fa-arrow-left' || 'fa-arrow-right';
    },
    arrowIconRight() {
      return this.$root.ltr && 'fa-arrow-right' || 'fa-arrow-left';
    },
    displaySequentially() {
      return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
    },
    isCurrentSite() {
      return this.currentPortal === this.site.name;
    },
  },
  methods: {
    urlVerify(url) {
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
    openOrCloseDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$root.$emit('change-site-menu', this.site);
    },
    findSiteRootNode(navigations) {
      let siteRootNode = null;
      for (const nav of navigations) {
        if (nav.pageKey) {
          siteRootNode = nav;
        } else if (nav.children?.length) {
          siteRootNode = this.findSiteRootNode(nav.children);
        }
        if (siteRootNode) {
          break;
        }
      }
      return siteRootNode;
    }
  }
};
</script>

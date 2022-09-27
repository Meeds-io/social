
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
  <v-btn @click="openOrCloseDrawer()">test</v-btn>
  <!--<v-list-item
    :href="spaceLink"
    :class="homeIcon && (homeLink === spaceLink && 'UserPageLinkHome' || 'UserPageLink')"
    link
    class="px-2 spaceItem">
    <v-list-item-avatar 
      size="28"
      class="me-3 tile my-0 spaceAvatar"
      tile>
      <v-img :src="spaceAvatar" />
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title class="body-2" v-text="spaceDisplayName" />
    </v-list-item-content>
    <v-list-item-icon
      :disabled="loading"
      :loading="loading"
      class="me-2 align-center"
      @click="openOrCloseDrawer($event)">
      <span class="fas spaceRightArrow icon-default-color"></span>
    </v-list-item-icon>
  </v-list-item> -->
</template>
<script>
/* eslint-disable vue/one-component-per-file */
import SpacePanelHamburgerNavigation from './SpacePanelHamburgerNavigation.vue';
export default {
  data () {
    return {
      secondLevelVueInstancee: null,
      secondeLevel: false,
    };
  },
  props: {
    space: {
      type: Object,
      default: null,
    },
    spaceUrl: {
      type: String,
      default: null
    },
    homeLink: {
      type: String,
      default: null,
    },
    homeIcon: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    spaceLink() {
      return this.spaceUrl;
    },
    spaceAvatar() {
      return this.space?.avatarUrl;
    },
    spaceDisplayName() {
      return this.space?.displayName;
    }
  },
  methods: {
    mountSecondLevel(parentId) {
      const VueHamburgerMenuItem = Vue.extend(SpacePanelHamburgerNavigation);
      const vuetify = this.vuetify;
      new VueHamburgerMenuItem({
        i18n: new VueI18n({
          locale: this.$i18n.locale,
          messages: this.$i18n.messages,
        }),
        vuetify,
        el: parentId,
      });
    },
    /*openOrCloseDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
    }, */
    openOrCloseDrawer() {
      this.secondeLevel = !this.secondeLevel;
      if (this.secondeLevel) {
        this.arrowIcon = 'fa-arrow-left';
        this.$emit('open-second-level');
      } else {
        this.arrowIcon = 'fa-arrow-right';
        this.$emit('close-second-level');
      }
    },
  },
};
</script>

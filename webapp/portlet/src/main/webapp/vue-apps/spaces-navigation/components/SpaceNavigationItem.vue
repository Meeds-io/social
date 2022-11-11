
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
  <v-list-item
    v-if="isMobile"
    :class="homeIcon && (homeLink === spaceLink && 'UserPageLinkHome' || 'UserPageLink')"
    class="px-2 spaceItem"
    @click="openOrCloseDrawer()">
    <v-list-item-avatar 
      size="28"
      class="me-3 ms-3 tile my-0 spaceAvatar"
      tile>
      <v-img :src="spaceAvatar" />
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title class="body-2" v-text="spaceDisplayName" />
    </v-list-item-content>
  </v-list-item>
  <v-list-item
    v-else
    :href="spaceLink"
    :class="homeIcon && (homeLink === spaceLink && 'UserPageLinkHome' || 'UserPageLink')"
    link
    class="px-2 spaceItem"
    @mouseover="showItemActions = true" 
    @mouseleave="showItemActions = false">
    <v-list-item-avatar 
      size="28"
      class="me-3 ms-2 tile my-0 spaceAvatar"
      tile>
      <v-img :src="spaceAvatar" />
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title class="body-2" v-text="spaceDisplayName" />
    </v-list-item-content>
    <v-list-item-icon
      v-if="toggleArrow"
      :disabled="loading"
      :loading="loading"
      class="me-2 align-center">
      <v-btn icon @click="openOrCloseDrawer($event)">
        <v-icon
          :id="space.id"
          class="me-0 pa-2 icon-default-color clickable"
          small>
          {{ arrowIcon }} 
        </v-icon>
      </v-btn>
    </v-list-item-icon>
  </v-list-item>
</template>
<script>
export default {
  data () {
    return {
      secondLevelVueInstancee: null,
      secondeLevel: false,
      showItemActions: false,
      arrowIcon: 'fa-arrow-right'
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
    }
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
    },
    toggleArrow() {
      return this.showItemActions || this.secondeLevel;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
  },
  created() {
    document.addEventListener('space-opened', (event) => {
      if (event.detail === this.space.id) {
        this.arrowIcon= 'fa-arrow-right';
        this.secondeLevel = false;
        this.showItemActions = false;
      }
    });
    document.addEventListener('hide-space-panel', () => {
      this.arrowIcon= 'fa-arrow-right';
      this.secondeLevel = false;
      this.showItemActions = false;
    });
  },
  methods: {
    hideSecondeItem() {
      this.arrowIcon= 'fa-arrow-right';
      this.showItemActions = false;
      this.secondeLevel = false;
    },
    openOrCloseDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.secondeLevel = !this.secondeLevel;
      if (this.secondeLevel) {
        this.arrowIcon = 'fa-arrow-left';
        this.$emit('open-space-panel');
      } else {
        this.arrowIcon = 'fa-arrow-right';
        this.$emit('close-space-panel');
      }
    }, 
  },
};
</script>

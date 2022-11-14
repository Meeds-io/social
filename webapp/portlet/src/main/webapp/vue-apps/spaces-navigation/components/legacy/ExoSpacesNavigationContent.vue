
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
  <v-flex
    :class="shaped && 'ms-12'"
    class="mx-0 spacesNavigationContent"
    flat>
    <v-list :shaped="shaped" dense>
      <v-list-item-group v-model="selectedSpaceIndex">
        <v-list-item
          v-for="space in filteredSpaces"
          :key="space.id"
          :href="space.spaceUrl"
          :class="homeIcon && (homeLink === space.spaceUrl && 'UserPageLinkHome' || 'UserPageLink')"
          link
          class="px-2 spaceItem">
          <v-list-item-avatar 
            size="26"
            class="me-3 tile my-0 spaceAvatar"
            tile>
            <v-img :src="space.avatarUrl" />
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="body-2" v-text="space.displayName" />
          </v-list-item-content>
          <v-list-item-icon @click="$emit('selectHome', $event, space)">
            <span class="UserPageHome"></span>
          </v-list-item-icon>
        </v-list-item>
      </v-list-item-group>
    </v-list>
    <v-row v-if="canShowMore" class="mx-0 my-4 justify-center">
      <v-btn
        small
        depressed
        @click="loadNextPage()">
        {{ $t('menu.spaces.showMore') }}
      </v-btn>
    </v-row>
  </v-flex>
</template>
<script>
export default {
  props: {
    homeLink: {
      type: String,
      default: null,
    },
    homeIcon: {
      type: Boolean,
      default: false,
    },
    offset: {
      type: Number,
      default: 0,
    },
    limit: {
      type: Number,
      default: 10,
    },
    pageSize: {
      type: Number,
      default: 10,
    },
    keyword: {
      type: Object,
      default: null,
    },
    showMoreButton: {
      type: Boolean,
      default: false,
    },
    shaped: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    startSearchAfterInMilliseconds: 400,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    spaces: [],
    initialized: false,
    loadingSpaces: false,
    limitToFetch: 0,
    originalLimitToFetch: 0,
  }),
  computed: {
    canShowMore() {
      return this.showMoreButton && !this.loadingSpaces && this.spaces.length >= this.limitToFetch;
    },
    filteredSpaces() {
      if (!this.keyword) {
        return this.spaces;
      } else {
        return this.spaces.slice().filter(space => space.displayName && space.displayName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    },
    selectedSpaceIndex() {
      return this.spaces.findIndex(space => space.spaceUrl === eXo.env.server.portalBaseURL || eXo.env.server.portalBaseURL.indexOf(`${space.spaceUrl}/`) === 0);
    },
  },
  watch: {
    keyword() {
      if (!this.keyword) {
        this.resetSearch();
        this.searchSpaces();
        return;
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loadingSpaces) {
        this.loadingSpaces = true;
        this.waitForEndTyping();
      }
    },
    spaces() {
      this.spaces.forEach(space => {
        space.spaceUrl = `${eXo.env.portal.context}${space.spaceUrl}`;
      });
    },
    limitToFetch() {
      this.searchSpaces()
        .finally(() => {
          if (!this.initialized) {
            this.initialized = true;
            this.$root.$applicationLoaded();
          }
        });
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;
  },
  methods: {
    searchSpaces() {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/${eXo.env.portal.containerName}/social/spaces/lastVisitedSpace/list.json?offset=${this.offset}&limit=${this.limitToFetch}`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp && resp.ok && resp.json())
        .then(data => {
          this.spaces = data && data.spaces || [];
          return this.$nextTick();
        })
        .then(() => {
          if (this.keyword && this.filteredSpaces.length < this.originalLimitToFetch && this.spaces.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
        .finally(() => this.loadingSpaces = false);
    },
    resetSearch() {
      if (this.limitToFetch !== this.originalLimitToFetch) {
        this.limitToFetch = this.originalLimitToFetch;
      }
    },
    loadNextPage() {
      this.originalLimitToFetch = this.limitToFetch += this.pageSize;
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchSpaces();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  }
};
</script>


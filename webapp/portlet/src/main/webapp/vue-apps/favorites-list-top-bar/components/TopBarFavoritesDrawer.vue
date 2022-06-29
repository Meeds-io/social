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
  <exo-drawer
    ref="favoritesDrawer"
    class="favoritesDrawer"
    right>
    <template slot="title">
      {{ $t('UITopBarFavoritesPortlet.title.recentFavorites') }}
    </template>
    <template slot="content">
      <v-list v-if="favoritesList.length" class="mx-3">
        <favorite-item
          v-for="favoriteItem in favoritesList"
          :key="favoriteItem.id"
          :favorite="favoriteItem"
          :activity-extensions="activityExtensions" />
      </v-list>
      <div v-else-if="!loading" class="d-flex full-height disabled-background align-center justify-center">
        <div class="noFavoritesContent">
          <v-icon class="mx-auto disabled--text mb-3" size="100">fas fa-star </v-icon>
          <p class="text-sub-title font-weight-bold">{{ $t('UITopBarFavoritesPortlet.label.NoFavorites') }}</p>
        </div>
      </div>
    </template>
    <template slot="footer" v-if="hasMore">
      <v-btn
        :loading="loading"
        :disabled="loading"
        block
        class="btn pa-0"
        @click="loadMore">
        {{ $t('Search.button.loadMore') }}
      </v-btn>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    favoritesList: [],
    loading: false,
    offset: 0,
    limit: Math.round((window.innerHeight - 122) / 53),
    totalSize: 0,
    pageSize: 10,
    activityExtensions: [],
    extensionApp: 'ActivityFavoriteIcon',
    activityIconExtension: 'activity-favorite-icon-extensions',
  }),
  computed: {
    hasMore() {
      return this.limit < this.totalSize || (this.loading && !this.totalSize);
    },
  },
  watch: {
    limit() {
      this.retrieveFavoritesList();
    },
    loading() {
      if (this.loading) {
        this.$refs.favoritesDrawer.startLoading();
      } else {
        this.$refs.favoritesDrawer.endLoading();
      }
    },
  },
  created() {
    this.$root.$on('open-favorite-drawer', () => {
      document.addEventListener(`extension-${this.extensionApp}-${this.activityIconExtension}-updated`, this.refreshActivityIcon);
      this.refreshActivityIcon();
      this.openDrawer();
    });
    this.$root.$on('close-favorite-drawer', () => {
      this.$refs.favoritesDrawer.close();
    });
    this.$root.$on('refresh-favorite-list', () => {
      this.retrieveFavoritesList();
    });

  },
  beforeDestroy() {
    document.removeEventListener(`extension-${this.extensionApp}-${this.activityIconExtension}-updated`, this.refreshActivityIcon);
  },
  methods: {
    openDrawer() {
      this.retrieveFavoritesList();
      this.$refs.favoritesDrawer.open();
    },
    retrieveFavoritesList() {
      this.loading = true;
      return this.$favoriteService.getFavorites(this.offset, this.limit, true)
        .then(data => {
          this.totalSize = data && data.size || this.totalSize;
          this.favoritesList = data && data.favoritesItem || [];
        })
        .finally(() => this.loading = false);
    },
    loadMore() {
      if (this.hasMore) {
        this.limit += this.pageSize;
      }
    },
    refreshActivityIcon() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.activityIconExtension);
      extensions.forEach(extension => {
        if (extension.id) {
          this.activityExtensions.push(extension);
        }
      });
    },
  }
};
</script>

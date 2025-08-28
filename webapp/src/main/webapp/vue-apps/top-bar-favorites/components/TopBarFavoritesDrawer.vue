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
    v-model="drawer"
    class="favoritesDrawer"
    :loading="loading"
    allow-expand
    right
    @expand-updated="expanded = $event">
    <template #title>
      {{ $t('UITopBarFavoritesPortlet.title.recentFavorites') }}
    </template>
    <template v-if="drawer" #titleIcons>
      <extension-registry-components
        name="FavoritesDrawer"
        type="title-icons"
        class="d-flex" />
    </template>
    <template v-if="drawer" #content>
      <div
        :class="expanded && 'pa-4'"
        class="d-flex light-grey-background-color fill-height">
        <div
          class="page-content pa-0 d-flex fill-height">
          <v-card
            v-if="expanded"
            class="card-border-radius"
            height="fit-content"
            min-width="270"
            width="270"
            max-width="30%"
            flat>
            <favorite-types
              ref="favoriteTypes"
              id="favoriteTypes"
              v-model="type"
              class="flex-grow-0 flex-shrink-0"
              @change="selectType" />
          </v-card>
          <v-expand-x-transition>
            <v-card
              :min-width="separatorWidth"
              :class="expanded && 'me-4'" />
          </v-expand-x-transition>
          <v-card
            :max-height="expanded && '100%' || 'auto'"
            class="d-flex flex-column flex-grow-1 flex-shrink-1 transparent no-border-radius overflow-hidden"
            flat>
            <v-card
              :max-height="expanded && '100%' || 'auto'"
              :class="expanded && 'overflow-x-hidden overflow-y-auto card-border-radius' || 'overflow-x-hidden no-border-radius'"
              :tile="!expanded"
              class="d-flex flex-column flex-grow-1 flex-shrink-1"
              flat>
              <v-list v-if="favoritesToDisplay.length" class="pa-0">
                <favorite-item
                  v-for="favoriteItem in favoritesToDisplay"
                  :key="favoriteItem.id"
                  :favorite="favoriteItem"
                  :activity-extensions="activityExtensions"
                  :click-callback="setFavoriteAsLastAccessed"
                  :expanded="expanded" />
              </v-list>
              <favorite-placeholder
                v-else-if="!loading"
                :type="type" />
            </v-card>
            <v-btn
              v-if="expanded && hasMore"
              :loading="loading"
              :disabled="loading"
              class="btn mx-auto mt-4 flex-grow-0 flex-shrink-0"
              outlined
              @click="loadMore">
              {{ $t('Search.button.loadMore') }}
            </v-btn>
          </v-card>
        </div>
      </div>
    </template>
    <template v-if="!expanded && hasMore" #footer>
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
    drawer: false,
    loading: false,
    expanded: false,
    type: null,
    offset: 0,
    limit: Math.round((window.innerHeight - 122) / 53),
    totalSize: 0,
    pageSize: 10,
    activityExtensions: [],
    extensionApp: 'ActivityFavoriteIcon',
    activityIconExtension: 'activity-favorite-icon-extensions',
  }),
  computed: {
    favoritesToDisplay() {
      return this.favoritesList.filter(f => f && !f.removed).slice(0, this.limit + this.offset);
    },
    limitToFetch() {
      return this.limit + this.pageSize;
    },
    hasMore() {
      return this.limit < this.totalSize || (this.loading && !this.totalSize);
    },
  },
  watch: {
    type() {
      if (this.drawer) {
        this.retrieveFavoritesList();
      }
    },
    limit() {
      if (this.drawer) {
        this.retrieveFavoritesList();
      }
    },
    expanded() {
      if (this.drawer) {
        this.type = null;
      }
    },
  },
  created() {
    document.addEventListener('favorites-open', this.openDrawer);
    document.addEventListener(`extension-${this.extensionApp}-${this.activityIconExtension}-updated`, this.refreshActivityIcon);
    this.$root.$on('favorite-removed', this.handleFavoriteRemoved);
    this.$root.$on('open-favorite-drawer', this.openDrawer);
    this.$root.$on('close-favorite-drawer', this.closeDrawer);
    this.$root.$on('refresh-favorite-list', this.retrieveFavoritesList);
  },
  beforeDestroy() {
    document.removeEventListener('favorites-open', this.openDrawer);
    document.removeEventListener(`extension-${this.extensionApp}-${this.activityIconExtension}-updated`, this.refreshActivityIcon);
    this.$root.$off('favorite-removed', this.handleFavoriteRemoved);
    this.$root.$off('open-favorite-drawer', this.openDrawer);
    this.$root.$off('close-favorite-drawer', this.closeDrawer);
    this.$root.$off('refresh-favorite-list', this.retrieveFavoritesList);
  },
  methods: {
    openDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.type = null;
      this.typeLabel = this.$t('UITopBarFavoritesPortlet.types.all');
      this.retrieveFavoritesList();
      this.refreshActivityIcon();
      window.require(['SHARED/favoriteDrawerExtensions'], () => {
        Promise.resolve(this.$utils.includeExtensions('FavoriteDrawerExtension'))
          .then(() => this.$refs.favoritesDrawer.open());
      });
    },
    closeDrawer() {
      this.$refs.favoritesDrawer.close();
    },
    selectType(type, label) {
      this.type = type;
      this.typeLabel = label;
    },
    retrieveFavoritesList() {
      if (this.loading) {
        return;
      }
      this.loading = true;
      return this.$favoriteService.getFavorites(this.offset, this.limitToFetch, true, this.type)
        .then(data => {
          this.totalSize = data?.size || 0;
          this.favoritesList = data?.favoritesItem || [];
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
      this.activityExtensions = extensions.filter(ext => ext.id);
    },
    setFavoriteAsLastAccessed(objectType, objectId) {
      return this.$favoriteService.setFavoriteAsLastAccessed(objectType, objectId);
    },
    async handleFavoriteRemoved(type, id) {
      this.totalSize--;
      const favoriteItem = this.favoritesList.find(item => item.objectType === type && item.objectId === id);
      if (favoriteItem) {
        this.$set(favoriteItem, 'removed', true);
      }
      await this.$nextTick();
      if (this.totalSize > this.limitToFetch
          && this.favoritesToDisplay.length < (this.limit + this.offset)) {
        this.limit *= 2;
      }
    },
  }
};
</script>

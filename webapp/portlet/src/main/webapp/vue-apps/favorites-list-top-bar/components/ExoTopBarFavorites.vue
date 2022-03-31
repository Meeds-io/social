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
  <v-app id="favoritesListPortlet">
    <v-flex>
      <v-layout>
        <v-btn 
          icon 
          class="icon-default-color" 
          @click="openDrawer()">
          <v-icon size="22">fa-star</v-icon>
        </v-btn>
        <exo-drawer
          ref="favoritesDrawer"
          class="favoritesDrawer"
          body-classes="hide-scroll"
          right>
          <template slot="title">
            {{ $t('UITopBarFavoritesPortlet.title.recentFavorites') }}
          </template>
          <template slot="content">
            <p v-for="favorite in favoritesList" :key="favorite.id">
              {{ favorite.objectType }}
            </p>
          </template>
          <template slot="footer">
          </template>
        </exo-drawer>
      </v-layout>
    </v-flex>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    favoritesList: [],
    offset: 0,
    limit: 10
  }),
  created() {
    this.retrieveFavoritesList();
  },
  methods: {
    openDrawer() {
      this.$refs.favoritesDrawer.open();
    },
    retrieveFavoritesList() {
      return this.$favoriteService.getFavorites(this.offset, this.limit)
        .then(data => {
          this.favoritesList = data || [];
        });
    },
  }
};
</script>

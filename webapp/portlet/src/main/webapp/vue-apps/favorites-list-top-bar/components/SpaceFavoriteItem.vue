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
  <v-list-item class="clickable" :href="spaceUrl">
    <v-list-item-icon class="me-3 my-auto">
      <exo-space-avatar 
        :space="space" 
        :size="25"
        avatar />
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-color body-2">
        <p
          class="ma-auto text-truncate"
          v-sanitized-html="spaceName"></p>
      </v-list-item-title>
    </v-list-item-content>

    <v-list-item-action>
      <favorite-button
        :id="id"
        :favorite="isFavorite"
        :space-id="id"
        :top="top"
        :right="right"
        type="space"
        @removed="removed"
        @remove-error="removeError" />
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: () => null,
    },
  },
  data: () => ({
    space: null,
    spaceName: '',
    spaceUrl: '#',
    isFavorite: true
  }),
  created() {
    this.spaceUrl = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.id}`;
    this.$spaceService.getSpaceById(this.id)
      .then((spaceData)=> {
        this.space = spaceData;
        this.spaceName = spaceData?.displayName ? spaceData.displayName : this.$t('UITopBarFavoritesPortlet.label.space');
        this.spaceUrl = `${eXo.env.portal.context}/g/${spaceData.groupId.replace(/\//g, ':')}`;
      });
  },
  methods: {
    removed() {
      this.isFavorite = !this.isFavorite;
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('spaceList.alert.label')}));
      this.$emit('removed');
      this.$root.$emit('refresh-favorite-list');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('spaceList.alert.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('alert-message', message, type || 'success');
    },
  }
};
</script>

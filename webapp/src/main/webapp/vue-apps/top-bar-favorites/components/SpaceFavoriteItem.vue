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
    v-if="space"
    :href="spaceUrl"
    @keydown.enter="setAsViewed"
    @auxclick="setAsViewed"
    @click="setAsViewed">
    <v-list-item-icon class="me-3 my-auto">
      <v-card
        :min-width="iconWidth"
        class="d-flex justify-center no-border-radius"
        color="transparent"
        flat>
        <exo-space-avatar
          :space="space"
          :size="avatarSize"
          avatar />
      </v-card>
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title>
        <p
          class="ma-auto text-truncate"
          v-sanitized-html="spaceName"></p>
      </v-list-item-title>
      <v-list-item-subtitle v-if="expanded" class="d-flex full-width overflow-hidden pt-2px">
        <span class="flex-grow-0 flex-shrink-0">{{ membersCount }} {{ $t('space.logo.banner.popover.members') }}</span>
        <v-icon class="flex-grow-0 flex-shrink-0 mx-2" size="2">fa-circle</v-icon>
        <span class="flex-grow-1 flex-shrink-1 text-truncate">{{ descriptionText }}</span>
      </v-list-item-subtitle>
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
    clickCallback: {
      type: Function,
      default: null,
    },
    expanded: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    space: null,
    spaceName: '',
    spaceUrl: '#',
    isFavorite: true
  }),
  computed: {
    descriptionText() {
      return this.expanded && this.space?.description && this.$utils.htmlToText(this.space?.description) || '';
    },
    membersCount() {
      return this.space?.membersCount;
    },
    iconWidth() {
      return this.expanded ? 40 : 30;
    },
    avatarSize() {
      return this.expanded ? 35 : 25;
    },
  },
  async created() {
    this.spaceUrl = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.id}`;
    try {
      this.space = await this.$spaceService.getSpaceById(this.id);
      this.spaceName = this.space?.displayName ? this.space.displayName : this.$t('UITopBarFavoritesPortlet.label.space');
      this.spaceUrl = `${eXo.env.portal.context}/s/${this.id}`;
    } catch {
      this.$root.$emit('favorite-removed', 'space', this.id);
    }
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
    setAsViewed(event) {
      if (event.which === 1 || event.which === 2) {
        this.clickCallback('space', this.id);
      }
    },
  }
};
</script>

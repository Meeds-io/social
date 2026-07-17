<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<template>
  <v-list-item
    v-if="page"
    :href="pageUrl"
    @keydown.enter="setAsViewed"
    @auxclick="setAsViewed"
    @click="setAsViewed">
    <v-list-item-icon class="me-3 my-auto">
      <v-icon :size="iconSize">
        fas fa-file-alt
      </v-icon>
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-truncate">
        {{ pageTitleText }}
      </v-list-item-title>
      <v-list-item-subtitle v-if="expanded && excerpt" class="pt-2px text-truncate-2">
        {{ excerpt }}
      </v-list-item-subtitle>
    </v-list-item-content>

    <v-list-item-action>
      <favorite-button
        :id="id"
        :favorite="isFavorite"
        type="page"
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
    expanded: {
      type: Boolean,
      default: false,
    },
    clickCallback: {
      type: Function,
      default: null,
    },
  },
  data: () => ({
    page: null,
    pageUrl: '#',
    isFavorite: true,
  }),
  computed: {
    iconSize() {
      return this.expanded ? 34 : 24;
    },
    pageTitle() {
      const name = this.page?.pageTitle || this.page?.pageName || '';
      const site = this.page?.siteName || '';
      return site && name && `${name} - ${site}` || name || site;
    },
    pageTitleText() {
      return this.pageTitle || this.$t('UITopBarFavoritesPortlet.label.page');
    },
    excerpt() {
      return this.page?.excerpts?.length && this.page.excerpts[0] || '';
    },
  },
  async created() {
    try {
      const resp = await fetch(`/social/rest/pages/${this.id}`, {
        credentials: 'include',
      });
      if (resp?.ok) {
        this.page = await resp.json();
        this.pageUrl = this.page?.pagePath || '#';
      } else {
        this.$root.$emit('favorite-removed', 'page', this.id);
      }
    } catch {
      this.$root.$emit('favorite-removed', 'page', this.id);
    }
  },
  methods: {
    removed() {
      this.isFavorite = !this.isFavorite;
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('UITopBarFavoritesPortlet.label.page')}));
      this.$emit('removed');
      this.$root.$emit('refresh-favorite-list');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('UITopBarFavoritesPortlet.label.page')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('alert-message', message, type || 'success');
    },
    setAsViewed(event) {
      if (event.which === 1 || event.which === 2) {
        this.clickCallback('page', this.id);
      }
    },
  }
};
</script>

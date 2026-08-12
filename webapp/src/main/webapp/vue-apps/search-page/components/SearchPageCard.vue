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
  <v-hover v-slot="{ hover }">
    <v-card
      flat
      class="pa-0"
      :aria-label="link && $t('search.access.to.result', {0: excerptText}) || title"
      :href="link"
      :disabled="!link">
      <v-list class="pa-0" :class="hover && link && 'light-grey-background-color no-border-radius' || ''">
        <v-list-item>
          <v-list-item-icon class="me-2 pt-1">
            <v-icon size="24" class="icon-default-color">
              fas fa-file-alt
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title class="text-truncate" :class="link && 'primary--text' || 'text-color'">
              {{ title }}
            </v-list-item-title>
            <v-list-item-subtitle
              v-if="excerptHtml"
              class="pt-1 text-wrap text-body-2 text-color text-break text-truncate-3"
              v-sanitized-html="excerptHtml">
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action v-if="result && result.id" v-show="hover || isMobile">
            <favorite-button
              :id="result.id"
              :favorite="result.favorite"
              type="page"
              @removed="$emit('refresh-favorite')"
              @added="$emit('refresh-favorite')" />
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
  </v-hover>
</template>
<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: null,
    },
  },
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
    title() {
      const name = this.result?.pageTitle || this.result?.pageName || '';
      const site = this.result?.siteLabel || '';
      return site && name && `${name} - ${site}` || name || site;
    },
    link() {
      return this.result?.pagePath || null;
    },
    excerptHtml() {
      return this.result?.excerpts?.length && this.result.excerpts.join('\r\n...') || '';
    },
    excerptText() {
      return this.$utils.htmlToText(this.excerptHtml);
    },
  },
};
</script>

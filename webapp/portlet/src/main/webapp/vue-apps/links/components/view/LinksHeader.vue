<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <div
    v-if="hasLinks && (showHeader || canEdit)"
    :class="{
      'position-relative pb-4': showHeader,
    }"
    class="d-flex align-center">
    <div v-if="header" class="flex-grow-1 flex-shrink-1 text-truncate widget-text-header text-start">
      {{ header }}
    </div>
    <v-spacer />
    <div
      v-if="hasLinks && (seeMoreUrl || hoverEdit)"
      class="flex-grow-0 flex-shrink-0 text-end">
      <v-btn
        v-if="seeMoreUrl"
        :href="seeMoreUrl"
        :title="$t('links.label.seeMore')"
        target="_blank"
        :outlined="hoverEdit"
        :icon="hoverEdit"
        :class="!hoverEdit && 'pa-0'"
        :color="!hoverEdit && 'primary'"
        :text="!hoverEdit"
        rel="nofollow noreferrer noopener"
        small>
        <v-icon
          v-if="hoverEdit"
          size="18"
          class="primary--text">
          fa-external-link-alt
        </v-icon>
        <span
          v-else-if="seeMoreUrl"
          class="text-none text-font-size">
          {{ $t('links.label.seeMore') }}
        </span>
      </v-btn>
      <v-fab-transition hide-on-leave>
        <v-btn
          v-if="hoverEdit"
          :title="$t('links.label.editSettings')"
          :class="{
            'position-absolute z-index-two t-0': !seeMoreUrl,
            'r-0': !$vuetify.rtl,
            'l-0': $vuetify.rtl,
            'ma-1': !showHeader,
          }"
          small
          icon
          @click="$emit('edit')">
          <v-icon size="18">fa-cog</v-icon>
        </v-btn>
      </v-fab-transition>
    </div>
  </div>
  <div v-else-if="!hasLinks && canEdit" class="d-flex align-center justify-center">
    <v-btn
      class="primary"
      elevation="0"
      outlined
      border
      @click="$emit('add')">
      <v-icon size="18" class="me-2">fa-link</v-icon>
      {{ $t('links.label.addLinksButton') }}
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
    minWidth: {
      type: String,
      default: () => '33%',
    },
    canEdit: {
      type: Boolean,
      default: false,
    },
    hasLinks: {
      type: Boolean,
      default: false,
    },
    hover: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    hoverEdit() {
      return this.canEdit && this.hover;
    },
    showHeader() {
      return this.header?.length || this.seeMoreUrl?.length;
    },
    header() {
      return this.settings?.header?.[this.$root.language] || this.settings?.header?.[this.$root.defaultLanguage];
    },
    seeMoreUrl() {
      return this.$linkService.toLinkUrl(this.settings?.seeMore);
    },
  },
};
</script>
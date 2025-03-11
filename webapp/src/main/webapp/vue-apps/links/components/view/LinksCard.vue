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
  <component
    v-bind="isCard && {
      hover: true,
      outlined: true,
    } || {
      text: true,
      class: 'transparent',
    }"
    :is="isCard && 'v-card' || 'v-btn'"
    class="mx-2"
    :height="itemHeight"
    :href="url"
    :max-height="itemHeight"
    :max-width="itemWidth"
    :min-height="itemHeight"
    :min-width="itemWidth"
    :target="target"
    :width="itemWidth">
    <v-card
      class="d-flex flex-column full-height full-width transparent border-box-sizing align-center justify-start overflow-hidden text-none"
      flat
      :max-height="itemHeight"
      :max-width="itemWidth"
      :min-height="itemHeight"
      :min-width="itemWidth"
      :title="description || name">
      <links-icon
        v-if="showIcon"
        class="justify-center"
        :class="showName && 'pb-0 col-6 align-end' || 'col-12 align-center'"
        :icon="icon"
        :icon-size="iconSize"
        :icon-url="iconUrl" />
      <div
        v-if="showName && name"
        class="pt-3 px-1 full-width text-truncate-2 text-body"
        :class="!showIcon && 'pb-3 my-auto'">
        {{ showName && name || '' }}
      </div>
    </v-card>
  </component>
</template>
<script>
  export default {
    props: {
      link: {
        type: Object,
        default: null,
      },
      type: {
        type: String,
        default: null,
      },
      showName: {
        type: Boolean,
        default: false,
      },
      showIcon: {
        type: Boolean,
        default: false,
      },
      largeIcon: {
        type: Boolean,
        default: false,
      },
      iconSize: {
        type: Number,
        default: () => 0,
      },
    },
    data: () => ({
      hover: false,
    }),
    computed: {
      name () {
        return this.$t(this.link?.name?.[this.$root.language] || this.link?.name?.[this.$root.defaultLanguage]);
      },
      description () {
        return this.$t(this.link?.description?.[this.$root.language] || this.link?.description?.[this.$root.defaultLanguage]);
      },
      url () {
        return this.$utils.toLinkUrl(this.link?.url, {
          urls: true,
          email: true,
          phone: true,
        });
      },
      target () {
        return this.link?.sameTab && '_self' || '_blank';
      },
      iconUrl () {
        if (this.link?.iconSrc) {
          return this.$utils.convertImageDataAsSrc(this.link.iconSrc);
        } else {
          return this.link?.iconUrl;
        }
      },
      icon () {
        return this.link?.icon;
      },
      itemSize () {
        return this.iconSize >= 20 ? this.iconSize * 5 : (this.iconSize < 20 && this.iconSize > 15 ? this.iconSize * 7 : this.iconSize * 8);
      },
      itemWidth () {
        return this.showName && this.itemSize || parseInt(this.itemSize / 2);
      },
      itemHeight () {
        return this.showName && this.itemSize || parseInt(this.itemSize / 2);
      },
      isCard () {
        return this.type === 'CARD';
      },
    },
  };
</script>
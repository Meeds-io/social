<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
    ref="actionMenu"
    v-model="menu"
    :attach="$root.isMobile && '#vuetify-apps' || attachMenu"
    content-class="position-absolute application-menu z-index-modal"
    eager
    :left="!$vuetify.rtl"
    offset-y
    :right="$vuetify.rtl"
    transition="slide-x-reverse-transition">
    <template #activator="{on, attrs}">
      <v-btn
        v-bind="attrs"
        :disabled="$root.isBulkProcessing"
        icon
        :loading="loading"
        :title="$t('social.spaces.administration.manageSpaces.spaceActionsMenu')"
        v-on="on">
        <v-icon size="20">
          fa-ellipsis-v
        </v-icon>
      </v-btn>
    </template>
    <v-list
      :class="$root.isMobile && 'border-top-left-radius border-top-right-radius'"
      dense
      :max-width="!$root.isMobile && 300 || 'auto'">
      <component
        :is="extension.componentName"
        v-for="extension in $root.itemMenuExtensions"
        :key="extension.name"
        :space="space"
        @loading="loading = $event" />
    </v-list>
  </component>
</template>
<script>
  export default {
    props: {
      space: {
        type: Object,
        default: null,
      },
    },
    data: () => ({
      id: Math.random(), // NOSONAR
      menu: false,
      loading: false,
    }),
    watch: {
      menu () {
        // Workaround to fix closing menu when clicking outside
        if (this.menu) {
          document.addEventListener('mousedown', this.closeMenu);
          this.$root.$emit('spaces-administration-list-menu-opened', this.id);
        } else {
          document.removeEventListener('mousedown', this.closeMenu);
        }
      },
    },
    created () {
      this.$root.$on('spaces-administration-list-menu-opened', this.closeMenu);
    },
    beforeUnmount () {
      this.$root.$off('spaces-administration-list-menu-opened', this.closeMenu);
      document.removeEventListener('mousedown', this.closeMenu);
    },
    methods: {
      closeMenu (event) {
        if (event !== this.id) {
          if (event?.target) {
            window.setTimeout(() => {
              this.menu = false;
            }, 200);
          } else {
            this.menu = false;
          }
        }
      },
    },
  };
</script>


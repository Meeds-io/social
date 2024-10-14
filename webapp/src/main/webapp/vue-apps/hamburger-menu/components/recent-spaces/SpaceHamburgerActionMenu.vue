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
  <v-menu
    v-if="extensions?.length"
    v-model="menu"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    content-class="application-menu z-index-modal"
    offset-y>
    <template #activator="{attrs, on}">
      <v-btn
        :aria-label="$t('menu.spaces.openSpaceAdvancedActions')"
        icon
        v-bind="attrs"
        v-on="on">
        <v-icon size="18">fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-list
      max-width="auto"
      min-width="auto"
      width="auto">
      <v-list-item
        v-for="extension in enabledExtensions"
        :key="extension.key"
        v-bind="extension.href && {
          href: () => extension.href(space),
        }"
        v-on="(extension.click || extension.eventName) && {
          click: event => clickButton(extension, event),
        }">
        <v-list-item-icon class="my-auto me-2 ms-0">
          <v-icon size="18">{{ extension.icon }}</v-icon>
        </v-list-item-icon>
        <v-list-item-content class="ms-0 my-auto">
          <v-list-item-title>
            {{ $t(extension.titleKey) }}
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-menu>
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
    extensions: [],
    menu: false,
    conserveHover: false,
  }),
  computed: {
    enabledExtensions() {
      return this.extensions?.filter?.(extension => extension.enabled(this.space));
    },
  },
  watch: {
    menu() {
      if (this.conserveHover) {
        this.conserveHover = false;
      } else {
        if (this.menu) {
          this.refreshExtensions();
        }
        this.setMenuVisibility(this.menu);
      }
    },
  },
  created() {
    document.addEventListener('extension-space-hamburger-menu-item-updated', this.refreshExtensions);
    this.refreshExtensions();
  },
  beforeDestroy() {
    document.removeEventListener('extension-space-hamburger-menu-item-updated', this.refreshExtensions);
  },
  methods: {
    refreshExtensions() {
      this.extensions = extensionRegistry.loadExtensions('space-hamburger', 'menu-item');
    },
    setMenuVisibility(visible) {
      if (visible) {
        this.$root.$emit('menu-opened');
      } else if (!this.conserveHover) {
        this.$root.$emit('menu-closed');
      }
    },
    clickButton(extension, event) {
      this.conserveHover = extension.conserveHover;
      event.stopPropagation();
      event.preventDefault();
      window.setTimeout(() => {
        this.menu = false;
      }, 50);
      if (extension.eventName) {
        this.$root.$emit(extension.eventName, this.space);
        document.dispatchEvent(new CustomEvent(extension.eventName, {detail: this.space}));
      } else {
        extension.click(this.space);
      }
    },
  },
};
</script>
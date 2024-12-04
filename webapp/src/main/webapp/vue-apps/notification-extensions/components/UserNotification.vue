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
  <extension-registry-component
    v-if="extensionComponent"
    :component="extensionComponent"
    :params="componentParams" />
  <user-notification-html
    v-else-if="notification.html"
    :id="notification.id"
    :content="notification.html" />
  <div v-else>-</div>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    refresh: 1,
  }),
  computed: {
    extension() {
      return this.refresh > 0
        && Object.values(this.$root.notificationExtensions)
          .sort((ext1, ext2) => (ext1.rank || 0) - (ext2.rank || 0))
          .find(extension => extension.match && extension.match(this.notification) || extension.type === this.notification.plugin)
        || null;
    },
    extensionComponent() {
      return this.$root.notificationExtensions && this.extension?.vueComponent && {
        componentName: 'notification-extension',
        componentOptions: {
          vueComponent: this.extension.vueComponent,
        },
      } || null;
    },
    componentParams() {
      return {
        notification: this.notification,
      };
    },
  },
  created() {
    document.addEventListener('notification-extensions-refresh', this.forceRefreshExtension);
  },
  beforeDestroy() {
    document.removeEventListener('notification-extensions-refresh', this.forceRefreshExtension);
  },
  methods: {
    refreshExtension() {
      this.refresh++;
    },
  },
};
</script>
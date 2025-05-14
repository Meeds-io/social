<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <!-- Inside $attrs we will have a href, thus the rule is valid, but not directly visible in Vue transpilation -->
  <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions -->
  <a
    v-bind="$attrs"
    @click.stop.prevent="open"
    @keypress.enter.stop.prevent="open">
    <slot></slot>
  </a>
</template>
<script>
export default {
  methods: {
    open() {
      const objectParts = this.$attrs['data-object']?.split?.(':');
      if (objectParts?.length === 2) {
        window.require(['SHARED/ContentLink'], app => app.openPluginDrawer(objectParts[0], objectParts[1]));
      }
    },
  },
};
</script>
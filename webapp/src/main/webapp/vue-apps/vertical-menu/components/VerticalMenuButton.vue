<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
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
  <v-btn
    id="verticalMenuBotton"
    size="22"
    outlined
    icon
    class="ma-2 d-none"
    @click="$root.$emit('open-vertical-menu-drawer')">
    <v-icon size="22">
      fas fa-bars
    </v-icon>
  </v-btn>
</template>
<script>
export default {
  data: () => ({
    interval: null,
  }),
  mounted() {
    this.interval = window.setInterval(this.init, 50);
  },
  methods: {
    init() {
      if (document.querySelector('#breadcrumb')) {
        if (document.querySelector('#breadcrumbParent')) {
          this.mountElement();
        } else {
          document.addEventListener('breadcrumb-app-mounted', this.mountElement);
        }
      } else {
        this.$el.classList.remove('d-none');
      }
    },
    mountElement() {
      if (document.querySelector('#breadcrumbParent')) {
        window.clearInterval(this.interval);
        document.querySelector('#breadcrumbParent').prepend(this.$el);
        this.$el.classList.add('me-2');
        this.$el.classList.remove('ma-2');
        this.$el.classList.remove('d-none');
      }
    },
  },
};
</script>


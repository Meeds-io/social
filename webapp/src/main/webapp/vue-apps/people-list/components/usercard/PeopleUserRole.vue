<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->
<template>
  <v-btn
    :ripple="false"
    :title="title"
    class="userRole"
    width="24"
    height="24"
    icon
    @click.stop.prevent>
    <v-icon
      class="primary--text"
      size="12">
      {{ icon }}
    </v-icon>
  </v-btn>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: null
    },
    icon: {
      type: String,
      default: null
    }
  }
};
</script>

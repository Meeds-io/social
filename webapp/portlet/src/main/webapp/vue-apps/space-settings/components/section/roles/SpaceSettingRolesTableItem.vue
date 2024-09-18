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
  <tr>
    <td class="ps-0">
      <div>{{ name }}</div>
    </td>
    <td v-if="!$root.isMobile">
      <div>{{ description }}</div>
    </td>
    <td class="pe-0">
      <div v-if="size">
        <exo-user-avatars-list
          :users="users"
          :icon-size="33"
          :max="3"
          :default-length="size"
          :margin-left="size > 1 && 'ml-n5' || ''"
          compact
          @open-detail="$emit('open')" />
      </div>
      <div v-else>-</div>
    </td>
  </tr>
</template>
<script>
export default {
  props: {
    name: {
      type: String,
      default: null,
    },
    description: {
      type: String,
      default: null,
    },
    users: {
      type: Array,
      default: null,
    },
    size: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    menu: false,
    realizationLink: null,
  }),
};
</script>
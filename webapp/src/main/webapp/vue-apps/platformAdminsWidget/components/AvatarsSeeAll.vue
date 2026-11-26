<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2026 Meeds Association contact@meeds.io

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
    v-ripple="false"
    :title="label"
    :aria-label="label"
    :min-width="width"
    :min-height="size"
    :height="size"
    :class="hovered && 'caption white--text grey-lighten1-background px-1' || 'pa-0'"
    class="avatars-see-all-button"
    text
    @mouseenter="hovered = true"
    @mouseleave="hovered = false"
    @focus="hovered = true"
    @blur="hovered = false"
    @click="$emit('open')">
    <span
      v-if="hovered"
      class="text-body white--text text-center text-no-wrap">
      {{ $t('activity.reactions.seeMore') }}
    </span>
    <!-- The avatars only show who: the button is the one action, so nothing
         inside it takes the keyboard focus or the pointer -->
    <div
      v-else
      class="pe-none"
      aria-hidden="true"
      inert>
      <exo-user-avatars-list
        :users="users"
        :default-length="count"
        :icon-size="size"
        :max="max"
        :popover="false"
        compact />
    </div>
  </v-btn>
</template>
<script>
// The avatars of a list of users, replaced by a "See All" action while the
// pointer or the keyboard focus is on them, as the activity reactions do
export default {
  props: {
    users: {
      type: Array,
      default: () => [],
    },
    count: {
      type: Number,
      default: 0,
    },
    label: {
      type: String,
      default: null,
    },
    max: {
      type: Number,
      default: 4,
    },
    size: {
      type: Number,
      default: 33,
    },
  },
  data: () => ({
    hovered: false,
  }),
  computed: {
    width() {
      const avatarsCount = Math.min(this.users.length, this.max) + (this.count > this.max ? 1 : 0);
      return this.size + Math.max(avatarsCount - 1, 0) * (this.size / 2);
    },
  },
};
</script>

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
  <div>
    <v-card
      v-for="property in userTypeProperties"
      :key="property.value"
      class="elevation-0 py-1 px-1 d-flex mb-1"
      :disabled="disabled"
      outlined>
      <exo-user-avatar
        :profile-id="property.value"
        :size="28"
        class="my-auto"
        popover-left-position
        align-top />
      <v-btn
        class="ms-auto"
        :disabled="disabled"
        :aria-label="$t('label.remove')"
        icon>
        <v-icon
          small
          class="red--text"
          @click="removeUser(property.value)">
          fa-minus
        </v-icon>
      </v-btn>
    </v-card>
  </div>
</template>

<script>
export default {
  props: {
    userTypeProperties: {
      type: Array,
      default: () => []
    },
    disabled: {
      type: Boolean,
      default: false
    },
  },
  methods: {
    removeUser(userName) {
      this.$emit('remove-value', userName);
    }
  }
};
</script>

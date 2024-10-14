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
  <v-tooltip
    bottom>
    <template #activator="{ on, attrs }">
      <div
        v-bind="attrs"
        v-on="on"
        class="d-inline-block">
        <v-btn
          v-if="!isHidden"
          :disabled="!isHiddenable"
          icon
          :aria-label="$t('profileContactInformation.hide.property.label')"
          @click="hideProperty">
          <v-icon
            size="18"
            class="icon-default-color">
            fas fa-eye
          </v-icon>
        </v-btn>
        <v-btn
          v-else
          icon
          :aria-label="$t('profileContactInformation.show.property.label')"
          @click="showProperty">
          <v-icon
            size="18"
            class="icon-default-color">
            fas fa-eye-slash
          </v-icon>
        </v-btn>
      </div>
    </template>
    <span v-if="!isHiddenable && !isNew">
      {{ $t('profileContactInformation.hiddenable.disabled') }}
    </span>
    <span v-else-if="!isHidden && !isNew">
      {{ $t('profileContactInformation.hide.property.label') }}
    </span>
    <span v-else-if="!isNew">
      {{ $t('profileContactInformation.show.property.label') }}
    </span>
    <span v-else>
      {{ $t('profileContactInformation.property.not.saved.label') }}
    </span>
  </v-tooltip>
</template>

<script>
export default {
  props: {
    property: {
      type: Object,
      default: null
    }
  },
  computed: {
    isHiddenable() {
      return this.property?.hiddenable;
    },
    isHidden() {
      return this.property?.hidden;
    },
    isNew() {
      return this.property?.isNew;
    }
  },
  methods: {
    hideProperty() {
      this.$root.$emit('hide-profile-property', this.property);
    },
    showProperty() {
      this.$root.$emit('show-profile-property', this.property);
    }
  }
};
</script>

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
  <v-list-item
    v-if="isUpdatingStatus"
    @click="disconnect">
    <v-progress-circular
      :class="iconClass"
      size="18"
      indeterminate />
  </v-list-item>
  <v-list-item
    v-else-if="confirmedUser"
    @click="disconnect">
    <v-list-item-title class="align-center d-flex">
      <v-icon
        :class="iconClass"
        size="16">
        fas fa-minus-circle
      </v-icon>
      <span class="mx-2">
        {{ $t('peopleList.button.disconnect') }}
      </span>
    </v-list-item-title>
  </v-list-item>
  <v-list-item
    v-else-if="incomingUser">
    <v-list-item-title class="align-center d-flex">
      <div @click="acceptToConnect">
        <v-icon
          :class="iconClass"
          size="16">
          mdi-check
        </v-icon>
        <span class="mx-2">
          {{ $t('peopleList.button.acceptToConnect') }}
        </span>
      </div>
      <v-divider
        vertical />
      <div @click="refuseToConnect">
        <v-icon
          :class="iconClass"
          size="16">
          mdi-close
        </v-icon>
        <span class="mx-2">
          {{ $t('peopleList.button.refuseToConnect') }}
        </span>
      </div>
    </v-list-item-title>
  </v-list-item>
  <v-list-item
    v-else-if="outgoingUser"
    @click="cancelRequest">
    <v-list-item-title class="align-center d-flex">
      <v-icon
        :class="iconClass"
        size="16">
        mdi-close
      </v-icon>
      <span class="mx-2">
        {{ $t('peopleList.button.cancelRequest') }}
      </span>
    </v-list-item-title>
  </v-list-item>
  <v-list-item
    v-else
    @click="connect">
    <v-list-item-title class="align-center d-flex">
      <v-icon
        :class="iconClass"
        size="16">
        fas fa-plus-circle
      </v-icon>
      <span class="mx-2">
        {{ $t('peopleList.button.connect') }}
      </span>
    </v-list-item-title>
  </v-list-item>
</template>

<script>
export default {
  props: {
    relationshipStatus: {
      type: String,
      default: null
    },
    compactDisplay: {
      type: Boolean,
      default: false
    },
    isMobile: {
      type: Boolean,
      default: false
    },
    isUpdatingStatus: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    iconClass() {
      return !this.compactDisplay && this.isMobile && 'mx-4';
    },
    confirmedUser() {
      return this.relationshipStatus === 'CONFIRMED';
    },
    incomingUser() {
      return this.relationshipStatus === 'INCOMING';
    },
    outgoingUser() {
      return this.relationshipStatus === 'OUTGOING';
    },
  },
  methods: {
    connect() {
      this.$emit('connect');
    },
    disconnect() {
      this.$emit('disconnect');
    },
    acceptToConnect() {
      this.$emit('accept-to-connect');
    },
    refuseToConnect() {
      this.$emit('refuse-to-connect');
    },
    cancelRequest() {
      this.$emit('cancel-request');
    }
  }
};
</script>

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
  <v-tooltip bottom>
    <template #activator="{ on, attrs }">
      <v-btn
        :loading="saving"
        icon
        v-bind="attrs"
        v-on="on"
        @click="muteSpace">
        <v-icon size="16" class="icon-default-color">{{ spaceMuted && 'fa-bell-slash' || 'far fa-bell-slash' }}</v-icon>
      </v-btn>
    </template>
    <span>{{ spaceMuted && $t('Notification.tooltip.unmuteSpaceNotification') || $t('Notification.tooltip.muteSpaceNotification') }}</span>
  </v-tooltip>
</template>

<script>
export default {
  props: {
    spaceId: {
      type: String,
      default: null,
    },
    origin: {
      type: String,
      default: null,
    },
    muted: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      saving: false,
      muteValue: null,
    };
  },
  computed: {
    spaceMuted() {
      return (this.muteValue === null && typeof window.MUTED_SPACES?.[this.spaceId] === 'undefined') ? this.muted : window.MUTED_SPACES[this.spaceId] || this.muteValue || false;
    },
  },
  methods: {
    muteSpace() {
      this.saving = true;
      return this.$spaceService.muteSpace(this.spaceId, this.spaceMuted)
        .then(() => {
          document.dispatchEvent(new CustomEvent('refresh-notifications'));
          if (this.spaceMuted) {
            this.$root.$emit('alert-message', this.$t('Notification.alert.successfullyUnmuted'), 'success');
            document.dispatchEvent(new CustomEvent('space-unmuted', {detail: {
              name: this.origin,
              spaceId: this.spaceId,
            }}));
          } else {
            this.$root.$emit('alert-message', this.$t('Notification.alert.successfullyMuted'), 'success');
            document.dispatchEvent(new CustomEvent('space-muted', {detail: {
              name: this.origin,
              spaceId: this.spaceId,
            }}));
          }
        })
        .then(() => this.muteValue = !this.spaceMuted)
        .catch(() => this.$root.$emit('alert-message', this.$t('Notification.alert.errorChangingSpaceMutingStatus'), 'error'))
        .finally(() => this.saving = false);
    },
  }
};
</script>

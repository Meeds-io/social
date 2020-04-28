<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
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
  <div
    id="profileHeaderActions"
    :class="owner && 'profileHeaderOwnerActions' || 'profileHeaderOtherActions'"
    class="mt-auto mr-3">
    <template v-if="!owner || skeleton">
      <template v-if="!skeleton">
        <v-btn
          v-for="(extension, i) in enabledProfileActionExtensions"
          :key="i"
          class="btn mx-2"
          @click="extension.click(user)">
          <i :class="extension.icon ? extension.icon : 'hidden'" class="uiIcon" />
          <span class="buttonText">
            {{ extension.title }}
          </span>
        </v-btn>
      </template>
      <div v-if="!skeleton && invited" class="invitationButtons d-inline">
        <v-dialog
          v-model="mobileAcceptRefuseConnectionDialog"
          width="200"
          max-width="100vw">
          <v-card class="pa-0">
            <v-btn
              :disabled="loading"
              :loading="loading"
              block
              class="white no-border-radius"
              @click="acceptToConnect">
              {{ $t('profileHeader.button.acceptToConnect') }}
            </v-btn>
            <v-btn
              :disabled="loading"
              :loading="loading"
              block
              class="white no-border-radius"
              @click="refuseToConnect">
              {{ $t('profileHeader.button.refuseToConnect') }}
            </v-btn>
          </v-card>
        </v-dialog>
        <div class="acceptToConnectButtonParent">
          <v-btn
            :loading="sendingAction"
            :disabled="sendingAction"
            class="btn btn-primary mx-auto acceptToConnectButton"
            @click="acceptToConnect">
            <i class="uiIconSocConnectUser"/>
            <span class="buttonText">
              {{ $t('profileHeader.button.acceptToConnect') }}
            </span>
          </v-btn>
          <v-btn
            class="btn btn-primary peopleButtonMenu dropdownButton"
            @click="openSecondButton">
            <v-icon>mdi-menu-down</v-icon>
          </v-btn>
          <v-btn
            class="btn btn-primary peopleButtonMenu dialogButton"
            @click="openSecondButton(true)">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
        <v-btn
          v-show="displaySecondButton"
          :loading="sendingSecondAction"
          :disabled="sendingSecondAction"
          class="btn mx-auto refuseToConnectButton"
          @click="refuseToConnect">
          <i class="uiIconSocCancelConnectUser"/>
          <span class="buttonText">
            {{ $t('profileHeader.button.refuseToConnect') }}
          </span>
        </v-btn>
      </div>
      <v-btn
        v-else-if="!skeleton && requested"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn btn-primary mx-auto cancelRequestButton"
        @click="cancelRequest">
        <i class="uiIconSocCancelConnectUser"/>
        <span class="buttonText">
          {{ $t('profileHeader.button.cancelRequest') }}
        </span>
      </v-btn>
      <v-btn
        v-else-if="skeleton || disconnected"
        :loading="sendingAction"
        :disabled="sendingAction || skeleton"
        :class="skeleton && 'skeleton-background skeleton-text' || 'btn-primary'"
        class="btn mx-auto connectUserButton"
        @click="connect">
        <i v-if="!skeleton" class="uiIconSocConnectUser"/>
        <span class="buttonText">
          {{ skeleton && '&nbsp;&nbsp;&nbsp;&nbsp;' || $t('profileHeader.button.connect') }}
        </span>
      </v-btn>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    skeleton: {
      type: Boolean,
      default: () => true,
    },
    owner: {
      type: Boolean,
      default: () => true,
    },
    hover: {
      type: Boolean,
      default: () => false,
    },
  },
  data: () => ({
    profileActionExtensions: [],
    mobileAcceptRefuseConnectionDialog: false,
    sendingAction: false,
    sendingSecondAction: false,
    displaySecondButton: false,
    waitTimeUntilCloseMenu: 200,
  }),
  computed: {
    relationshipStatus() {
      return this.user && this.user.relationshipStatus;
    },
    connected() {
      return this.relationshipStatus === 'CONFIRMED';
    },
    disconnected() {
      return !this.relationshipStatus || this.relationshipStatus === 'IGNORED';
    },
    invited() {
      return this.relationshipStatus === 'INCOMING';
    },
    requested() {
      return this.relationshipStatus === 'OUTGOING';
    },
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.user) {
        return [];
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.user));
    },
  },
  created() {
    // To refresh menu when a new extension is ready to be used
    document.addEventListener('profile-extension-updated', this.refreshExtensions);

    // To broadcast event about current page supporting profile extensions
    document.dispatchEvent(new CustomEvent('profile-extension-init'));

    this.refreshExtensions();

    $(document).on('mousedown', () => {
      if (this.displaySecondButton) {
        window.setTimeout(() => {
          this.displaySecondButton = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
  },
  methods: {
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    openSecondButton(openDialog) {
      if (openDialog) {
        this.mobileAcceptRefuseConnectionDialog = true;
      } else {
        this.displaySecondButton = !this.displaySecondButton;
      }
    },
    connect() {
      this.sendingAction = true;
      this.$userService.connect(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    acceptToConnect() {
      this.sendingAction = true;
      this.$userService.confirm(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    refuseToConnect() {
      this.sendingSecondAction = true;
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingSecondAction = false;
        });
    },
    cancelRequest() {
      this.sendingAction = true;
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
  },
};
</script>
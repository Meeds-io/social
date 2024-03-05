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
  <v-card
    :id="userMenuParentId"
    outlined
    class="peopleCardItem d-flex mx-2"
    flat
    hover>
    <v-img
      :lazy-src="userBannerUrl"
      :src="userBannerUrl"
      transition="none"
      height="80px"
      class="white--text align-start d-block peopleBannerImg"
      eager />
    <div
      class="peopleToolbarIcons mt-1">
      <v-btn
        :title="$t('peopleList.label.openUserInfo')"
        icon
        small
        class="peopleInfoIcon d-flex ms-1"
        @click="$emit('flip')">
        <v-icon size="12">fa-info</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isManager"
        :title="$t('peopleList.label.spaceManager')"
        :ripple="false"
        color="primary"
        class="peopleInfoIcon d-flex not-clickable primary-border-color ms-1"
        icon
        small>
        <v-icon size="12">mdi-account-cog</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isSpaceRedactor"
        :title="$t('peopleList.label.spaceRedactor')"
        :ripple="false"
        color="primary"
        class="peopleInfoIcon d-flex not-clickable primary-border-color ms-1"
        icon
        small>
        <v-icon size="12">mdi-account-edit</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isSpacePublisher"
        :title="$t('peopleList.label.spacePublisher')"
        :ripple="false"
        color="primary"
        class="peopleInfoIcon d-flex not-clickable primary-border-color ms-1"
        icon
        small>
        <v-icon size="12">fa-paper-plane</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isGroupBound"
        :title="$t('peopleList.label.groupBound')"
        :ripple="false"
        color="grey"
        class="peopleGroupMemberBindingIcon d-flex not-clickable ms-1"
        icon
        small>
        <span class="d-flex uiIconGroup"></span>
      </v-btn>
      <v-spacer />
      <template v-if="canUseActionsMenu && !isSameUser">
        <v-menu
          ref="actionMenu"
          v-model="displayActionMenu"
          :attach="`#${userMenuParentId}`"
          transition="slide-x-reverse-transition"
          content-class="peopleActionMenu mt-n6 me-4"
          offset-y>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              :title="$t('peopleList.label.openUserMenu')"
              icon
              text
              class="d-block me-1 peopleMenuIcon">
              <v-icon size="21">
                mdi-dots-vertical
              </v-icon>
            </v-btn>
          </template>
          <v-list class="pa-0 white" dense>
            <v-list-item
              v-for="(extension, i) in enabledProfileActionExtensions"
              :key="i"
              @click="extension.click(user)">
              <v-list-item-title class="align-center d-flex">
                <v-icon
                  size="18">
                  {{ extension.class }}
                </v-icon>
                <span class="mx-2">
                  {{ extension.title }}
                </span>
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </div>
    <div class="peopleAvatar">
      <a :href="url">
        <v-img
          :lazy-src="`${userAvatarUrl}`"
          :src="`${userAvatarUrl}`"
          transition="none"
          class="mx-auto"
          height="65px"
          width="65px"
          max-height="65px"
          max-width="65px"
          eager />
      </a>
    </div>
    <v-card-text
      class="peopleCardBody align-center py-0 py-sm-1">
      <div>
        <a
          :href="url"
          :title="user.fullname"
          :class="usernameClass"
          class="userFullname font-weight-bold">
          {{ user.fullname }}
          <span v-if="externalUser" class="externalFlagClass">
            {{ $t('peopleList.label.external') }}
          </span>
        </a>
        <v-card-subtitle
          class="userPositionLabel text-truncate pa-0 mt-0 mt-sm-auto"
          v-sanitized-html="userPosition" />
      </div>
    </v-card-text>
    <v-card-actions
      v-if="!isSameUser"
      class="peopleCardActions mt-auto mb-3">
      <exo-confirm-dialog
        ref="confirmDialog"
        :title="confirmTitle"
        :message="confirmMessage"
        :ok-label="$t('peopleList.label.ok')"
        :cancel-label="okMethod && $t('peopleList.label.cancel')"
        @ok="okConfirmDialog"
        @dialog-closed="closeConfirmDialog" />
      <v-btn
        v-if="!user.enabled || user.deleted"
        class="btn mx-auto cancelRequestButton"
        depressed
        disabled
        block>
        <span v-if="user.deleted" class="d-inline peopleDeleteButton">
          {{ $t('peopleList.label.deletedUser') }}
        </span>
        <span v-else class="d-inline peopleDeleteButton">
          {{ $t('peopleList.label.disabledUser') }}
        </span>
        <v-icon class="d-none peopleDeleteButtonMinus">mdi-minus</v-icon>
      </v-btn>
      <v-btn
        v-else-if="confirmedUser"
        :loading="isSendingAction"
        :disabled="isSendingAction"
        class="btn mx-auto peopleRelationshipButton disconnectUserButton"
        depressed
        block
        @click="disconnectConfirm">
        <i class="uiIconSocCancelConnectUser peopleRelationshipIcon d-inline"></i>
        <span class="d-inline peopleRelationshipButtonText">
          {{ $t('peopleList.button.disconnect') }}
        </span>
        <v-icon class="d-none relationshipButtonMinus">mdi-minus</v-icon>
      </v-btn>
      <div v-else-if="incomingUser" class="invitationButtons">
        <div class="acceptToConnectButtonParent">
          <v-btn
            :loading="isSendingAction"
            :disabled="isSendingAction"
            class="btn mx-auto peopleRelationshipButton acceptToConnectButton"
            depressed
            @click="acceptToConnect">
            <i class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
            <span class="d-flex">
              {{ $t('peopleList.button.acceptToConnect') }}
            </span>
            <v-icon class="d-none relationshipButtonMinus">mdi-check</v-icon>
          </v-btn>
          <v-btn
            class="btn peopleButtonMenu d-inline"
            depressed
            x-small
            @click="displaySecondButton = !displaySecondButton">
            <v-icon>mdi-menu-down</v-icon>
          </v-btn>
        </div>
        <div class="position-absolute width-full ps-2 pe-3">
          <v-btn
            v-show="displaySecondButton"
            :loading="isSendingSecondAction"
            :disabled="isSendingSecondAction"
            class="btn ms-n1 peopleRelationshipButton refuseToConnectButton"
            depressed
            block
            @click="refuseToConnect">
            <i class="uiIconSocCancelConnectUser peopleRelationshipIcon d-inline"></i>
            <span class="d-flex">
              {{ $t('peopleList.button.refuseToConnect') }}
            </span>
            <v-icon class="d-none relationshipButtonMinus">mdi-close</v-icon>
          </v-btn>
        </div>
      </div>
      <v-btn
        v-else-if="outgoingUser"
        :loading="isSendingAction"
        :disabled="isSendingAction"
        class="btn mx-auto peopleRelationshipButton cancelRequestButton"
        depressed
        block
        @click="cancelRequest">
        <i class="uiIconSocCancelConnectUser peopleRelationshipIcon d-inline"></i>
        <span class="d-inline peopleRelationshipButtonText">
          {{ $t('peopleList.button.cancelRequest') }}
        </span>
        <v-icon class="d-none relationshipButtonMinus">mdi-close</v-icon>
      </v-btn>
      <v-btn
        v-else
        :loading="isSendingAction"
        :disabled="isSendingAction"
        class="btn mx-auto peopleRelationshipButton connectUserButton"
        depressed
        block
        @click="connect">
        <i class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
        <span class="d-inline peopleRelationshipButtonText">
          {{ $t('peopleList.button.connect') }}
        </span>
        <v-icon class="d-none relationshipButtonMinus">mdi-plus</v-icon>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: null,
    },
    isManager: {
      type: Boolean,
      default: false,
    },
    enabledProfileActionExtensions: {
      type: Array,
      default: () => [],
    },
    isSendingAction: {
      type: Boolean,
      default: false,
    },
    isSendingSecondAction: {
      type: Boolean,
      default: false,
    },
    relationshipStatus: {
      type: String,
      default: null
    },
    url: {
      type: String,
      default: null
    },
    userAvatarUrl: {
      type: String,
      default: null
    },
  },
  data: () => ({
    displayActionMenu: false,
    waitTimeUntilCloseMenu: 200,
    confirmTitle: '',
    confirmMessage: '',
    okMethod: null,
    displaySecondButton: false,
  }),
  watch: {
    displayActionMenu(newVal) {
      if (newVal) {
        document.getElementById(`peopleCardItem${this.user.id}`).style.zIndex = 3;
      } else {
        document.getElementById(`peopleCardItem${this.user.id}`).style.zIndex = 0;
      }
    }
  },
  computed: {
    isSameUser() {
      return this.user && this.user.username === eXo.env.portal.userName;
    },
    userBannerUrl() {
      return this.user && this.user.banner || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.user.username}/banner`;
    },
    userMenuParentId() {
      return this.user && this.user.id && `userMenuParent-${this.user.id}` || 'userMenuParent';
    },
    canUseActionsMenu() {
      return this.user && this.enabledProfileActionExtensions.length;
    },
    usernameClass() {
      return `${(!this.user.enabled || this.user.deleted) && 'text-sub-title' || 'text-color text-truncate pt-1 d-block'}`;
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
    userPosition() {
      return this.user?.position || '&nbsp;';
    },
    externalUser() {
      return this.user.external === 'true';
    },
  },
  created() {
    $(document).on('mousedown', () => {
      if (this.displayActionMenu) {
        window.setTimeout(() => {
          this.displayActionMenu = false;
          this.displaySecondButton = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
  },
  methods: {
    connect() {
      this.$emit('connect', this.user);
    },
    disconnect() {
      this.$emit('disconnect', this.user);
    },
    disconnectConfirm() {
      this.openConfirmDialog(
        this.$t('peopleList.title.disconnectConfirm'),
        this.$t('peopleList.message.disconnectConfirm', {0: `<b>${this.user.fullname}</b>`}),
        this.disconnect);
    },
    acceptToConnect() {
      this.$emit('accept-to-connect', this.user);
    },
    refuseToConnect() {
      this.$emit('refuse-to-connect', this.user);
    },
    cancelRequest() {
      this.$emit('cancel-request', this.user);
    },
    openConfirmDialog(title, message, okMethod) {
      this.confirmTitle = title;
      this.confirmMessage = message;
      if (okMethod) {
        this.okMethod = okMethod;
      } else {
        this.okMethod = null;
      }
      this.$refs.confirmDialog.open();
    },
    okConfirmDialog() {
      if (this.okMethod) {
        this.okMethod();
      }
    },
    closeConfirmDialog() {
      this.confirmTitle = '';
      this.confirmMessage = '';
      this.okMethod = null;
    },
  },
};
</script>

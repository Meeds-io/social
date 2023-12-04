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
  <v-app class="singlePageApplication">
    <v-card
      class="spaceAccessInfo py-12 text-center"
      flat>
      <v-card-text>
        <v-icon color="primary" class="fa-7x">{{ spaceNotAccessible && 'fa-door-closed' || 'fa-door-open' }}</v-icon>
      </v-card-text>
      <v-card-text
        v-if="firstLabel"
        class="text-h5 text-color"
        v-sanitized-html="firstLabel" />
      <v-card-text
        v-if="secondLabel"
        class="text-h5 text-sub-title"
        v-sanitized-html="secondLabel" />
      <v-card-actions class="justify-center py-5">
        <template v-if="spaceAccessTypeLabel === 'INVITED_SPACE'">
          <v-btn
            :disabled="sendingRefuse"
            :loading="sendingAction"
            color="primary"
            class="btn spaceAcceptInvitationButton"
            @click="acceptToJoin">
            {{ $t('UISpaceAccess.action.Accept') }}
          </v-btn>
          <div class="mx-4"></div>
          <v-btn
            :disabled="sendingAction"
            :loading="sendingRefuse"
            class="btn spaceRefuseInvitationButton"
            @click="refuseToJoin">
            {{ $t('UISpaceAccess.action.Refuse') }}
          </v-btn>
        </template>
        <v-btn
          v-else-if="spaceAccessTypeLabel === 'JOIN_SPACE'"
          :loading="sendingAction"
          color="primary"
          class="btn spaceJoinButton"
          @click="join">
          {{ $t('UISpaceAccess.action.Join') }}
        </v-btn>
        <v-btn
          v-else-if="spaceAccessTypeLabel === 'REQUESTED_JOIN_SPACE'"
          :loading="sendingAction"
          class="btn spaceCancelRequestButton"
          @click="cancelRequest">
          {{ $t('UISpaceMember.label.CancelRequest') }}
        </v-btn>
        <v-btn
          v-else-if="spaceAccessTypeLabel === 'REQUEST_JOIN_SPACE'"
          :loading="sendingAction"
          color="primary"
          class="btn spaceRequestJoinButton"
          @click="requestJoin">
          {{ $t('UISpaceAccess.action.RequestToJoin') }}
        </v-btn>
        <v-btn
          v-else-if="spaceAccessTypeLabel === 'CLOSED_SPACE' || spaceAccessTypeLabel === 'SPACE_NOT_FOUND'"
          :href="spacesLink"
          :loading="sendingAction"
          color="primary"
          class="btn spaceFindSpacesLink">
          {{ $t('UISpaceAccess.FindSpaces') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-app>
</template>
<script>
export default {
  props: {
    parameters: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    sendingAction: false,
    sendingRefuse: false,
  }),
  computed: {
    spacesLink() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/spaces`;
    },
    spaceLink() {
      return this.parameters?.originalUri;
    },
    spaceNotAccessible() {
      return this.spaceAccessTypeLabel === 'CLOSED_SPACE' || this.spaceAccessTypeLabel === 'SPACE_NOT_FOUND';
    },
    spaceAccessTypeLabel() {
      return this.parameters?.spaceAccessTypeLabel;
    },
    spaceId() {
      return this.parameters?.spaceId;
    },
    spacePrettyName() {
      return this.parameters?.spacePrettyName;
    },
    spaceDisplayName() {
      return this.parameters?.spaceDisplayName;
    },
    firstLabel() {
      switch (this.spaceAccessTypeLabel) {
      case 'INVITED_SPACE':
      case 'JOIN_SPACE':
      case 'REQUESTED_JOIN_SPACE':
      case 'REQUEST_JOIN_SPACE':
      case 'CLOSED_SPACE':
        return this.$t('UISpaceAccess.restrictedArea');
      case 'SPACE_NOT_FOUND':
        return this.$t('UISpace.space-not-found');
      }
      return null;
    },
    secondLabel() {
      switch (this.spaceAccessTypeLabel) {
      case 'INVITED_SPACE':
        return this.$t('UISpaceAccess.invited-space').replace('{0}', `<strong>${this.spaceDisplayName}</strong>`);
      case 'JOIN_SPACE':
        return this.$t('UISpaceAccess.memberRestrict').replace('{0}', `<strong>${this.spaceDisplayName}</strong>`);
      case 'REQUESTED_JOIN_SPACE':
        return this.$t('UISpaceAcess.requested-join-space').replace('{0}', `<strong>${this.spaceDisplayName}</strong>`);
      case 'REQUEST_JOIN_SPACE':
        return this.$t('UISpaceAccess.memberRestrict').replace('{0}', `<strong>${this.spaceDisplayName}</strong>`);
      case 'CLOSED_SPACE':
        return this.$t('UISpaceAccess.closedSpace').replace('{0}', `<strong>${this.spaceDisplayName}</strong>`);
      case 'SPACE_NOT_FOUND':
        return this.$t('UISpace.exploreSpacesCanJoin');
      }
      return null;
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  methods: {
    gotToSpace() {
      window.location.href = `${window.location.origin}${this.spaceLink}`;
    },
    gotToSpaces() {
      window.location.href = `${window.location.origin}${this.spacesLink}`;
    },
    handleError() {
      this.$root.$emit('alert-message', this.$t('UISpaceAccess.error'), 'error');
      this.sendingAction = false;
      this.sendingRefuse = false;
    },
    acceptToJoin() {
      this.sendingAction = true;
      this.$spaceService.accept(this.spaceId)
        .then(() => this.gotToSpace())
        .catch(() => this.handleError());
    },
    refuseToJoin() {
      this.sendingRefuse = true;
      this.$spaceService.deny(this.spaceId)
        .then(() => this.gotToSpaces())
        .catch(() => this.handleError());
    },
    join() {
      this.sendingAction = true;
      this.$spaceService.join(this.spaceId)
        .then(() => this.gotToSpace())
        .catch(() => this.handleError());
    },
    requestJoin() {
      this.sendingAction = true;
      this.$spaceService.requestJoin(this.spaceId)
        .then(() => this.gotToSpace())
        .catch(() => this.handleError());
    },
    cancelRequest() {
      this.sendingAction = true;
      this.$spaceService.cancel(this.spaceId)
        .then(() => this.gotToSpace())
        .catch(() => this.handleError());
    },
  },
};
</script>
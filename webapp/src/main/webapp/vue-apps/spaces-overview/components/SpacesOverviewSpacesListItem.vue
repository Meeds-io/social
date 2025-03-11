<template>
  <v-list-item class="pa-0 spaceItem">
    <v-list-item-avatar
      class="my-0"
      :class="spaceItemClass"
      :href="url"
      tile>
      <v-avatar
        :size="avatarSize"
        tile>
        <v-img
          class="mx-auto spaceAvatar"
          :height="avatarSize"
          :max-height="avatarSize"
          :max-width="avatarSize"
          role="presentation"
          :src="avatarUrl"
          :width="avatarSize" />
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content
      class="pa-0"
      :class="spaceItemClass"
      :href="url">
      <v-list-item-title>
        <a
          class="text-color"
          :href="url">
          {{ space.displayName }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle>
        <template v-if="filter === 'requests'">
          {{ $t('spacesOverview.requestToJoin.from', {0: user.fullname }) }}
        </template>
        <template v-else>
          {{ $t('spacesOverview.members', {0: space.membersCount}) }}
        </template>
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="ma-0 flex-row align-self-center">
      <template v-if="filter === 'requests'">
        <v-btn
          class="me-2 spacesOverviewCheck success-color-background"
          dark
          depressed
          :disabled="sendingAction"
          fab
          :height="actionIconSize"
          :loading="sendingAction"
          :width="actionIconSize"
          @click="acceptUserRequest">
          <v-icon
            dark
            size="18">
            fa-check
          </v-icon>
        </v-btn>
        <v-btn
          class="spacesOverviewClose error-color-background"
          dark
          depressed
          :disabled="sendingAction"
          fab
          :height="actionIconSize"
          :loading="sendingAction"
          :width="actionIconSize"
          @click="refuseUserRequest">
          <v-icon
            dark
            size="18">
            fa-times
          </v-icon>
        </v-btn>
      </template>
      <template v-if="filter === 'invited'">
        <v-btn
          class="me-2 spacesOverviewCheck success-color-background"
          dark
          depressed
          :disabled="sendingAction"
          fab
          :height="actionIconSize"
          :loading="sendingAction"
          :width="actionIconSize"
          @click="acceptToJoin">
          <v-icon
            dark
            size="18">
            fa-check
          </v-icon>
        </v-btn>
        <v-btn
          class="spacesOverviewClose error-color-background"
          dark
          depressed
          :disabled="sendingAction"
          fab
          :height="actionIconSize"
          :loading="sendingAction"
          :width="actionIconSize"
          @click="refuseToJoin">
          <v-icon
            dark
            size="18">
            fa-times
          </v-icon>
        </v-btn>
      </template>
      <template v-if="filter === 'manager'">
        <v-btn
          class="spacesOverviewCheck outlined"
          dark
          depressed
          :disabled="sendingAction"
          fab
          :height="actionIconSize"
          icon
          :loading="sendingAction"
          :width="actionIconSize"
          @click="$emit('edit')">
          <i class="uiIcon uiIconEdit"></i>
        </v-btn>
      </template>
      <template v-if="filter === 'pending'">
        <v-btn
          class="spacesOverviewClose error-color-background"
          dark
          depressed
          :disabled="sendingAction"
          fab
          :height="actionIconSize"
          :loading="sendingAction"
          :width="actionIconSize"
          @click="cancelRequest">
          <v-icon
            dark
            size="18">
            fa-times
          </v-icon>
        </v-btn>
      </template>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
  const randomMax = 10000;
  export default {
    props: {
      space: {
        type: Object,
        default: () => null,
      },
      filter: {
        type: String,
        default: () => null,
      },
      avatarSize: {
        type: Number,
        default: () => 37,
      },
    },
    data () {
      return {
        actionIconSize: 27,
        sendingAction: false,
        spaceItemClass: `spaceList${parseInt(Math.random() * randomMax)
          .toString()
          .toString()}`,
      };
    },
    computed: {
      avatarUrl () {
        return this.space && this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.space.prettyName}/avatar`;
      },
      user () {
        return this?.space?.pending?.[0];
      },
      url () {
        if (!this.space?.id) {
          return '#';
        }
        return `${eXo.env.portal.context}/s/${this.space.id}`;
      },
    },
    methods: {
      acceptUserRequest () {
        this.sendingAction = true;
        this.$spaceService.acceptUserRequest(this.space.id, this.user.username)
          .then(() => this.$emit('refresh', 'receivedRequests'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      refuseUserRequest () {
        this.sendingAction = true;
        this.$spaceService.refuseUserRequest(this.space.id, this.user.username)
          .then(() => this.$emit('refresh', 'receivedRequests'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      acceptToJoin () {
        this.sendingAction = true;
        this.$spaceService.accept(this.space.id)
          .then(() => this.$emit('refresh', 'invitations'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      refuseToJoin () {
        this.sendingAction = true;
        this.$spaceService.deny(this.space.id)
          .then(() => this.$emit('refresh', 'invitations'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      cancelRequest () {
        this.sendingAction = true;
        this.$spaceService.cancel(this.space.id)
          .then(() => this.$emit('refresh', 'sentRequests'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
    },
  };
</script>

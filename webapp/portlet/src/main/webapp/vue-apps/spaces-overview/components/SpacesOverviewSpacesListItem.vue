<template>
  <v-list-item class="pa-0 spaceItem">
    <v-list-item-avatar
      :class="spaceItemClass"
      :href="url"
      class="my-0"
      tile>
      <v-avatar :size="avatarSize" tile>
        <v-img
          :src="avatarUrl"
          :height="avatarSize"
          :width="avatarSize"
          :max-height="avatarSize"
          :max-width="avatarSize"
          class="mx-auto spaceAvatar"
          role="presentation" />
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content
      :class="spaceItemClass"
      :href="url"
      class="pa-0">
      <v-list-item-title>
        <a :href="url" class="text-color">
          {{ space.displayName }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle>
        <template v-if="filter === 'requests'">
          {{ $t('spacesOverview.requestToJoin.from', {0: space.pending[0].fullname }) }}
        </template>
        <template v-else>
          {{ $t('spacesOverview.members', {0: space.membersCount}) }}
        </template>
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="ma-0 flex-row align-self-center">
      <template v-if="filter === 'requests'">
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="me-2 spacesOverviewCheck"
          fab
          dark
          depressed
          @click="acceptUserRequest">
          <v-icon dark>
            mdi-check
          </v-icon>
        </v-btn>
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="spacesOverviewClose"
          fab
          dark
          depressed
          @click="refuseUserRequest">
          <v-icon dark>
            mdi-close
          </v-icon>
        </v-btn>
      </template>
      <template v-if="filter === 'invited'">
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="me-2 spacesOverviewCheck"
          fab
          dark
          depressed
          @click="acceptToJoin">
          <v-icon dark>
            mdi-check
          </v-icon>
        </v-btn>
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="spacesOverviewClose"
          fab
          dark
          depressed
          @click="refuseToJoin">
          <v-icon dark>
            mdi-close
          </v-icon>
        </v-btn>
      </template>
      <template v-if="filter === 'manager'">
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="spacesOverviewCheck outlined"
          icon
          fab
          dark
          depressed
          @click="$emit('edit')">
          <i class="uiIcon uiIconEdit"></i>
        </v-btn>
      </template>
      <template v-if="filter === 'pending'">
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="spacesOverviewClose"
          fab
          dark
          depressed
          @click="cancelRequest">
          <v-icon dark>
            mdi-close
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
  data() {
    return {
      actionIconSize: 27,
      sendingAction: false,
      spaceItemClass: `spaceList${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    avatarUrl() {
      return this.space && this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.space.prettyName}/avatar`;
    },
    url() {
      if (!this.space || !this.space.groupId) {
        return '#';
      }
      const uri = this.space.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
  },
  methods: {
    acceptUserRequest() {
      this.sendingAction = true;
      this.$spaceService.acceptUserRequest(this.space.displayName, this.space.pending[0].username)
        .then(() => this.$emit('refresh', 'receivedRequests'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    refuseUserRequest() {
      this.sendingAction = true;
      this.$spaceService.refuseUserRequest(this.space.displayName, this.space.pending[0].username)
        .then(() => this.$emit('refresh', 'receivedRequests'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    acceptToJoin() {
      this.sendingAction = true;
      this.$spaceService.accept(this.space.id)
        .then(() => this.$emit('refresh', 'invitations'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    refuseToJoin() {
      this.sendingAction = true;
      this.$spaceService.deny(this.space.id)
        .then(() => this.$emit('refresh', 'invitations'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    cancelRequest() {
      this.sendingAction = true;
      this.$spaceService.cancel(this.space.id)
        .then(() => this.$emit('refresh', 'sentRequests'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
  }
};
</script>

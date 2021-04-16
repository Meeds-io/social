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
          class="mx-auto spaceAvatar" />
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
    labels() {
      return {
        CancelRequest: this.$t('spacesOverview.label.profile.CancelRequest'),
        Confirm: this.$t('spacesOverview.label.profile.Confirm'),
        Connect: this.$t('spacesOverview.label.profile.Connect'),
        Ignore: this.$t('spacesOverview.label.profile.Ignore'),
        RemoveConnection: this.$t('spacesOverview.label.profile.RemoveConnection'),
        StatusTitle: this.$t('spacesOverview.label.profile.StatusTitle'),
        join: this.$t('spacesOverview.label.profile.join'),
        leave: this.$t('spacesOverview.label.profile.leave'),
        members: this.$t('spacesOverview.label.profile.members'),
      };
    },
  },
  mounted() {
    if (this.space && this.space.groupId) {
      // TODO disable tiptip because of high CPU usage using its code
      this.initTiptip();
    }
  },
  methods: {
    initTiptip() {
      this.$nextTick(() => {
        $(`.${this.spaceItemClass}`).spacePopup({
          userName: eXo.env.portal.userName,
          spaceID: this.space.id,
          restURL: '/portal/rest/v1/social/spaces/{0}',
          membersRestURL: '/portal/rest/v1/social/spaces/{0}/users?returnSize=true',
          managerRestUrl: '/portal/rest/v1/social/spaces/{0}/users?role=manager&returnSize=true',
          membershipRestUrl: '/portal/rest/v1/social/spacesMemberships?space={0}&returnSize=true',
          defaultAvatarUrl: this.avatarUrl,
          deleteMembershipRestUrl: '/portal/rest/v1/social/spacesMemberships/{0}:{1}:{2}',
          labels: this.labels,
          content: false,
          keepAlive: true,
          defaultPosition: this.tiptipPosition || 'left_bottom',
          maxWidth: '420px',
        });
      });
    },
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
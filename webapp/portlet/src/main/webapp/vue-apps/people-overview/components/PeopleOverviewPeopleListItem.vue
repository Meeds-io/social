<template>
  <v-list-item class="pa-0 userItem">
    <v-list-item-avatar
      :class="userItemClass"
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
          class="mx-auto userAvatar"
          role="presentation" />
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content
      :class="userItemClass"
      :href="url"
      class="pa-0">
      <v-list-item-title>
        <a :href="url" class="text-color">
          {{ user.fullname }}
          <span v-if="user.external === 'true'" class="externalTagClass">
            ({{ $t('UserProfilePopup.label.profile.external') }})
          </span>
        </a>
      </v-list-item-title>
      <v-list-item-subtitle>
        {{ $t('peopleOverview.connectionsInCommon', {0: user.connectionsInCommonCount}) }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="ma-0 flex-row align-self-center">
      <template v-if="filter === 'invitations'">
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="me-2 peopleOverviewCheck"
          fab
          dark
          depressed
          @click="acceptToConnect">
          <v-icon dark>
            mdi-check
          </v-icon>
        </v-btn>
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="peopleOverviewClose"
          fab
          dark
          depressed
          @click="refuseToConnect">
          <v-icon dark>
            mdi-close
          </v-icon>
        </v-btn>
      </template>
      <template v-if="filter === 'pending'">
        <v-btn
          :width="actionIconSize"
          :height="actionIconSize"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="peopleOverviewClose"
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
    user: {
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
      userItemClass: `userItem${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    avatarUrl() {
      return this.user && this.user.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.user.username}/avatar`;
    },
    url() {
      return this.user && this.user.profile || `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.user.username}/`;
    },
  },
  methods: {
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
  }
};
</script>

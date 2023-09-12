<template>
  <v-slide-y-transition>
    <v-card
      v-if="!hidden"
      :color="unread && '#f0f7fd'"
      flat
      tile>
      <v-list-item
        :href="url"
        class="d-flex d-relative pa-2"
        @click="markAsRead">
        <v-list-item-avatar v-if="$slots.avatar || avatarUrl">
          <slot v-if="$slots.avatar" name="avatar"></slot>
          <v-avatar
            v-else
            :class="avatarClass"
            :size="45">
            <img
              :src="avatarUrl"
              class="object-fit-cover ma-auto"
              loading="lazy"
              role="presentation"
              alt="">
          </v-avatar>
        </v-list-item-avatar>
        <v-list-item-content class="py-0">
          <v-list-item-title
            v-sanitized-html="message"
            class="subtitle-2 pb-2" />
          <v-list-item-subtitle class="d-flex align-center">
            <div class="flex-grow-1 flex-shrink-1 me-2">
              <extension-registry-components
                :params="extensionParams"
                :type="`${notification.plugin}-actions`"
                name="WebNotification"
                class="d-flex flex-wrap" />
            </div>
            <div class="flex-grow-0 flex-shrink-0 caption me-1">
              {{ relativeDateLabel }}
            </div>
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-btn
          :class="$vuetify.rtl && 'l-0' || 'r-0'"
          class="remove-item position-absolute t-0 mt-1 me-1"
          small
          icon
          @click.stop.prevent="hideNotification">
          <v-icon size="16">fa-times</v-icon>
        </v-btn>
      </v-list-item>
    </v-card>
  </v-slide-y-transition>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
    avatarUrl: {
      type: Object,
      default: null,
    },
    avatarClass: {
      type: String,
      default: null,
    },
    message: {
      type: String,
      default: null,
    },
    url: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    hidden: false,
  }),
  computed: {
    remoteId() {
      return this.notification.parameters.remoteId;
    },
    notificationId() {
      return this.notification.id;
    },
    unread() {
      return this.notification?.read === false;
    },
    lastUpdateTime() {
      return this.notification?.created && new Date(this.notification?.created);
    },
    relativeDateLabelKey() {
      return this.lastUpdateTime && this.$dateUtil.getShortRelativeTimeLabelKey(this.lastUpdateTime) || '';
    },
    relativeDateLabelValue() {
      return this.lastUpdateTime && this.$dateUtil.getShortRelativeTimeValue(this.lastUpdateTime) || 1;
    },
    relativeDateLabel() {
      return this.lastUpdateTime && this.$t(this.relativeDateLabelKey, {0: this.relativeDateLabelValue}) || '';
    },
    extensionParams() {
      return {
        notification: this.notification,
      };
    },
  },
  created() {
    this.$identityService.getIdentityByProviderIdAndRemoteId('organization', this.remoteId)
      .then(identity => this.identity = identity);
  },
  methods: {
    hideNotification() {
      this.$notificationService.hideNotification(this.notificationId)
        .then(() => {
          this.$root.$emit('hide-notification', this.notificationId);
          this.hidden = true;
        });
    },
    markAsRead() {
      return this.$notificationService.markRead(this.notificationId);
    },
  },
};
</script>
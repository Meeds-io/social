<template>
  <v-slide-y-transition>
    <v-card
      v-if="loading"
      class="d-flex align-center justify-center ma-auto my-2"
      tile
      flat />
    <v-card
      v-else-if="!hidden"
      :color="unread && '#f0f7fd'"
      flat
      tile>
      <v-list-item
        :href="url"
        class="d-flex d-relative pa-2"
        @click="markAsRead">
        <v-list-item-avatar
          v-if="$slots.avatar || avatarUrl"
          :rounded="spaceAvatar">
          <slot v-if="$slots.avatar" name="avatar"></slot>
          <v-avatar
            v-else
            :class="avatarClass"
            :size="45"
            :rounded="spaceAvatar">
            <img
              :src="avatarUrl"
              class="object-fit-cover ma-auto"
              loading="lazy"
              role="presentation"
              alt="">
          </v-avatar>
        </v-list-item-avatar>
        <v-list-item-content class="py-0 pe-5">
          <v-list-item-title
            v-sanitized-html="message"
            class="subtitle-2 text-wrap text-truncate-2" />
          <v-list-item-subtitle class="d-flex flex-column justify-center">
            <div
              :class="actionsClass"
              class="flex-grow-1 flex-shrink-1 my-1">
              <slot v-if="$slots.actions" name="actions"></slot>
              <extension-registry-components
                :params="extensionParams"
                :type="`${notification.plugin}-actions`"
                name="WebNotification"
                class="d-flex flex-wrap"
                strict-type />
            </div>
            <div class="flex-grow-0 flex-shrink-0 caption me-1">
              {{ relativeDateLabel }}
            </div>
          </v-list-item-subtitle>
        </v-list-item-content>
        <div
          :class="$vuetify.rtl && 'l-0' || 'r-0'"
          class="position-absolute t-0 pt-2px me-1">
          <v-btn
            class="remove-item"
            small
            icon
            @click.stop.prevent="hideNotification">
            <v-icon size="16">fa-times</v-icon>
          </v-btn>
        </div>
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
    actionsClass: {
      type: String,
      default: null,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    spaceAvatar: {
      type: Boolean,
      default: false,
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
      return this.lastUpdateTime && this.$dateUtil.getRelativeTimeLabelKey(this.lastUpdateTime, true) || '';
    },
    relativeDateLabelValue() {
      return this.lastUpdateTime && this.$dateUtil.getRelativeTimeValue(this.lastUpdateTime) || 1;
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
  watch: {
    loading: {
      immediate: true,
      handler(newVal, oldVal) {
        if (newVal && !oldVal) {
          this.$root.$emit('notification-loading-start');
        } else if (!newVal && oldVal) {
          this.$root.$emit('notification-loading-end');
        }
      }
    },
  },
  methods: {
    hideNotification() {
      this.$notificationService.hideNotification(this.notificationId)
        .then(() => {
          this.$root.$emit('hide-notification', this.notificationId);
          this.hidden = true;
          document.dispatchEvent(new CustomEvent('refresh-notifications'));
        });
    },
    markAsRead() {
      return this.$notificationService.markRead(this.notificationId);
    },
  },
};
</script>
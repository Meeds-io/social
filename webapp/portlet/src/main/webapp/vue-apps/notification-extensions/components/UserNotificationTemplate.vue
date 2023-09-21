<template>
  <v-hover
    v-if="!hidden"
    v-slot="{ hover }">
    <v-card
      ref="content"
      :min-height="absolute && minHeight || 'auto'"
      :min-width="absolute && minWidth || 'auto'"
      :class="absolute && (movingLeft && 'blue darken-1' || 'red darken-1')"
      class="position-relative no-border"
      tile
      flat>
      <v-slide-x-transition>
        <v-list-item
          :href="url"
          :input-value="unread && !hover"
          :class="absolute && 'position-absolute white' || 'position-static'"
          :style="absolute && {
            top: 0,
            left: `${left}px`,
            width: `${minWidth}px`,
            'min-width': `${minWidth}px`,
            'padding-bottom': '9px !important',
          }"
          class="d-flex d-relative pa-2"
          v-touch="{
            start: moveStart,
            end: moveEnd,
            move: moveSwipe,
          }"
          @mousedown="markAsRead">
          <v-list-item-avatar
            :rounded="spaceAvatar"
            :tile="!!$slots.avatar"
            :size="47"
            min-width="47"
            class="mt-0 mb-auto me-3 pt-2px align-start">
            <slot v-if="$slots.avatar" name="avatar"></slot>
            <v-avatar
              v-else
              :class="avatarClass"
              :size="36"
              :rounded="spaceAvatar">
              <img
                :src="avatarUrl"
                class="object-fit-cover ma-auto"
                loading="lazy"
                role="presentation"
                alt="">
            </v-avatar>
          </v-list-item-avatar>
          <v-list-item-content class="py-0 pe-5 text-color">
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
            v-if="hover"
            :class="$vuetify.rtl && 'l-0' || 'r-0'"
            class="position-absolute t-0 pt-2px me-1 d-none d-sm-block ">
            <v-btn
              class="remove-item"
              small
              icon
              @click.stop.prevent="hideNotification">
              <v-icon size="16">fa-times</v-icon>
            </v-btn>
          </div>
        </v-list-item>
      </v-slide-x-transition>
    </v-card>
  </v-hover>
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
    index: {
      type: Number,
      default: () => 0,
    },
    spaceAvatar: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    hidden: false,
    absolute: false,
    left: 0,
    startEvent: null,
    minHeight: 0,
    minWidth: 0,
    movingLeft: false,
    moving: false,
    markedAsRead: false,
  }),
  computed: {
    remoteId() {
      return this.notification.parameters.remoteId;
    },
    notificationId() {
      return this.notification.id;
    },
    unread() {
      return this.markedAsRead === false && this.notification?.read === false;
    },
    lastUpdateTime() {
      return this.notification?.created && new Date(this.notification?.created);
    },
    relativeDateLabelKey() {
      return this.lastUpdateTime && this.$root.now && this.$dateUtil.getRelativeTimeLabelKey(this.lastUpdateTime, true) || '';
    },
    relativeDateLabelValue() {
      return this.lastUpdateTime && this.$root.now && this.$dateUtil.getRelativeTimeValue(this.lastUpdateTime) || 1;
    },
    relativeDateLabel() {
      return this.lastUpdateTime && this.$root.now && this.$t(this.relativeDateLabelKey, {0: this.relativeDateLabelValue}) || '';
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
      this.hidden = true;
      this.$notificationService.hideNotification(this.notificationId)
        .then(() => {
          this.$root.$emit('hide-notification', this.notificationId);
          document.dispatchEvent(new CustomEvent('refresh-notifications'));
        });
    },
    markAsRead() {
      this.markedAsRead = true;
      return this.$notificationService.markRead(this.notificationId)
        .then(() => document.dispatchEvent(new CustomEvent('refresh-notifications')));
    },
    reset() {
      this.absolute = false;
      this.left = 0;
      this.movingLeft = false;
      this.startEvent = null;
      this.moving = false;
      this.minWidth = 0;
      this.minHeight = 0;
    },
    moveStart() {
      if (this.absolute) {
        return;
      }
      this.reset();
      this.minHeight = Math.max(this.minHeight, this.$refs?.content?.$el?.offsetHeight);
      this.minWidth = Math.max(this.minWidth, this.$refs?.content?.$el?.offsetWidth);
      window.setTimeout(() => this.absolute = true, 50);
    },
    moveEnd() {
      const deleteNotification = this.left > 0;
      const confirm = Math.abs(this.left) > (this.minWidth / 2);
      if (confirm) {
        if (deleteNotification) {
          this.hideNotification();
        } else {
          this.markAsRead();
          this.reset();
        }
      } else {
        this.reset();
      }
    },
    moveSwipe(event) {
      if (!this.absolute) {
        return;
      }
      if (!this.startEvent) {
        this.startEvent = event;
      } else if (!this.moving) {
        this.moving = true;
        this.$nextTick().then(() => {
          if (this.unread) {
            this.left = parseInt(event.touchmoveX - this.startEvent.touchmoveX);
          } else {
            this.left = Math.max(0, parseInt(event.touchmoveX - this.startEvent.touchmoveX));
          }
          this.movingLeft = this.left < 0;
          this.moving = false;
        });
      }
    },
  },
};
</script>
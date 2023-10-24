<template>
  <div
    class="position-relative unread-activity"
    @click="markAsRead">
    <v-scale-transition>
      <v-btn
        v-show="displayBadge"
        :style="`z-index: ${zIndex};`"
        class="unread-activity-badge"
        absolute
        icon
        height="20"
        width="20"
        text>
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <div
              class="unread-activity-badge"
              v-bind="attrs"
              v-on="on"></div>
          </template>
          <span>{{ $t('Unread.clickTooltip') }}</span>
        </v-tooltip>
      </v-btn>
    </v-scale-transition>
    <slot></slot>
  </div>
</template>
<script>
export default {
  props: {
    unreadMetadata: {
      type: Object,
      default: null,
    },
    spaceId: {
      type: String,
      default: null,
    },
    zIndex: {
      type: Number,
      default: () => 1,
    },
  },
  data: () => ({
    textLength: 0,
    textPosition: null,
    windowHeight: window.innerHeight || document.documentElement?.clientHeight,
    streamHeight: null,
    isReading: false,
    displayTimeout: false,
    computeTimeout: false,
    isPageHidden: false,
  }),
  computed: {
    isUnread() {
      return !!this.unreadMetadata;
    },
    displayBadge() {
      return this.spaceId && this.isUnread;
    },
    waitTimeToMarkAsRead() {
      return Math.max(parseInt(this.textLength / 100 * 5000) - 1000, 4000);
    },
    applicationName() {
      return this.unreadMetadata?.objectType;
    },
    applicationId() {
      return this.unreadMetadata?.objectId;
    },
  },
  watch: {
    displayBadge() {
      if (this.displayBadge) {
        this.countText();
        this.computePagePosition();
        this.installBodyScrollListener();
      } else {
        this.uninstallBodyScrollListener();
      }
      this.computeIsReading();
    },
    isPageHidden() {
      this.computeIsReading();
    },
    isReading(newVal, oldVal) {
      if (newVal && !oldVal) {
        this.displayTimeout = window.setTimeout(() => {
          if (this.computeIsReading()) {
            this.markAsRead();
          }
        }, this.waitTimeToMarkAsRead);
      } else if (this.displayTimeout) {
        window.clearTimeout(this.displayTimeout);
        this.displayTimeout = false;
        this.computeTimeout = false;
      }
    },
  },
  mounted() {
    document.addEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
    document.addEventListener('notification.read.item', this.handleUpdatesFromWebSocket);
    document.addEventListener('notification.read.allItems', this.handleUpdatesFromWebSocket);
  },
  beforeDestroy() {
    document.removeEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
    document.removeEventListener('notification.read.item', this.handleUpdatesFromWebSocket);
    document.removeEventListener('notification.read.allItems', this.handleUpdatesFromWebSocket);
  },
  methods: {
    installBodyScrollListener() {
      const siteBodyElement = document.querySelector('.site-scroll-parent');
      siteBodyElement.addEventListener('scroll', this.computePagePosition, false);
      document.addEventListener('visibilitychange', this.computeIsPageHidden, false);
    },
    uninstallBodyScrollListener() {
      const siteBodyElement = document.querySelector('.site-scroll-parent');
      siteBodyElement.removeEventListener('scroll', this.computePagePosition, false);
      document.removeEventListener('visibilitychange', this.computeIsPageHidden, false);
    },
    handleUpdatesFromWebSocket(event) {
      const data = event?.detail;
      const wsEventName = data?.wsEventName || '';
      let spaceWebNotificationItem = data?.message?.spaceWebNotificationItem || data?.message?.spacewebnotificationitem;
      if (spaceWebNotificationItem?.length) {
        spaceWebNotificationItem = JSON.parse(spaceWebNotificationItem);
      }
      const applicationName = spaceWebNotificationItem?.applicationName;
      const applicationId = spaceWebNotificationItem?.applicationItemId;
      const spaceId = spaceWebNotificationItem?.spaceId;
      if (Number(this.spaceId) === Number(spaceId)) {
        if (wsEventName === 'notification.read.allItems') {
          this.$emit('read');
        } else {
          if (applicationName === this.applicationName && applicationId === this.applicationId) {
            if (wsEventName === 'notification.unread.item') {
              this.$emit('unread');
            } else if (wsEventName === 'notification.read.item') {
              this.$emit('read');
            }
          }
        }
      }
    },
    countText() {
      if (this.displayBadge) {
        this.textLength = this.$el?.innerText.replace(/[\n\r\t ]/g, '').length || 0;
      }
    },
    computePagePosition() {
      if (this.displayBadge && !this.computeTimeout) {
        this.computeTimeout = window.setTimeout(() => {
          this.textPosition = this.$el?.getBoundingClientRect();
          if (!this.streamHeight) {
            this.streamHeight = this.windowHeight - 100;
          }
          this.computeIsPageHidden();
          this.computeIsReading();
          this.computeTimeout = false;
        }, 1000);
      }
    },
    computeIsReading() {
      this.isReading = this.displayBadge && this.textPosition && !this.isPageHidden
        && ((this.textPosition.top >= 200 && this.textPosition.top <= this.streamHeight)
          || (this.textPosition.bottom >= 200 && this.textPosition.bottom <= this.streamHeight)
          || (this.textPosition.top <= 200 && this.textPosition.bottom >= this.streamHeight)
        ) || false;
      return this.isReading;
    },
    computeIsPageHidden() {
      this.isPageHidden = document.hidden || document.msHidden || document.webkitHidden || document.mozHidden;
    },
    markAsRead(event) {
      if (this.displayBadge) {
        this.$spaceService.markAsRead(this.spaceId, this.applicationName, this.applicationId, event?.type && 'click' || 'read');
      }
    },
  }
};
</script>
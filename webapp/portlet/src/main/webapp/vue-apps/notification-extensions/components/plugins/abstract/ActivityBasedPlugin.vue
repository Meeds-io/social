<template>
  <user-notification-template
    :notification="notification"
    :avatar-url="profileAvatarUrl"
    :message="message"
    :loading="loading || loadingActivity"
    :url="activityUrl">
    <template #actions>
      <div
        v-if="parentContentText"
        :title="parentContentText"
        class="text-truncate">
        <v-icon size="14" class="me-1">fa-stream</v-icon>
        {{ parentContentText }}
      </div>
      <div
        :class="parentContentText && 'my-2' || ''"
        :title="contentText"
        class="text-truncate">
        <v-icon size="14" class="me-1">{{ notificationIcon }}</v-icon>
        {{ contentText }}
      </div>
      <div v-if="reply" class="mt-1">
        <v-btn
          :href="replyUrl"
          color="primary"
          elevation="0"
          small
          outlined>
          <v-icon size="14" class="me-1">{{ replyIcon }}</v-icon>
          <span class="text-none">
            {{ $t(replyKey) }}
          </span>
        </v-btn>
      </div>
      <div v-else-if="$slots.reply" class="overflow-hidden mt-1">
        <slot name="reply"></slot>
      </div>
    </template>
  </user-notification-template>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
    messageKey: {
      type: String,
      default: null,
    },
    messageText: {
      type: String,
      default: null,
    },
    activityIdParam: {
      type: String,
      default: () => 'activityId',
    },
    commentIdParam: {
      type: String,
      default: () => 'commentId',
    },
    icon: {
      type: String,
      default: null,
    },
    reply: {
      type: Boolean,
      default: false,
    },
    replyIcon: {
      type: String,
      default: () => 'far fa-comment',
    },
    replyKey: {
      type: String,
      default: () => 'Notification.label.Reply',
    },
    fromIdentity: {
      type: Object,
      default: null,
    },
    loading: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    loadingActivity: true,
    activity: null,
    parentActivity: null,
    extension: {
      isEnabled: null,
      getIcon: null,
      getMessage: null,
      getContent: null,
    },
  }),
  computed: {
    profile() {
      return this.notification?.from || this.fromIdentity;
    },
    username() {
      return this.profile?.username;
    },
    profileFullname() {
      return this.profile?.fullname || '';
    },
    profileAvatarUrl() {
      return this.profile?.avatar;
    },
    activityId() {
      return this.notification?.parameters && this.notification?.parameters[this.activityIdParam] || this.commentId || null;
    },
    commentId() {
      return this.notification?.parameters && this.notification?.parameters[this.commentIdParam] || null;
    },
    parentCommentId() {
      return this.activity?.parentCommentId || null;
    },
    activityType() {
      return this.activity?.type;
    },
    activityUrl() {
      return (this.commentId && this.activityId && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.activityId}#comment-${this.commentId}`)
        || (this.activityId && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.activityId}`)
        || '#';
    },
    replyUrl() {
      return this.activity && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.activityId}#comment-reply${(this.parentCommentId || this.commentId) && '-' || ''}${this.parentCommentId || this.commentId || ''}`
        || '#';
    },
    space() {
      return this.notification?.space || this.activity?.activityStream?.space;
    },
    spaceDisplatName() {
      return this.space?.displayName || '';
    },
    notificationIcon() {
      if (!this.activity || !this.extension?.getIcon) {
        return this.icon;
      } else {
        return this.extension.getIcon(this.notification, this.activity) || this.icon;
      }
    },
    message() {
      if (this.messageText) {
        return this.messageText;
      } else if (this.extension?.getMessage) {
        return this.extension.getMessage(this.notification, this.activity);
      } else {
        return this.$t(this.messageKey || `Notification.intranet.message.${this.notification?.plugin}`, {
          0: `<a class="user-name font-weight-bold">${this.profileFullname}</a>`,
          1: `<a class="space-name font-weight-bold">${this.spaceDisplatName}</a>`,
        });
      }
    },
    content() {
      if (!this.activity) {
        return '';
      } else if (this.extension?.getContent) {
        return this.extension.getContent(this.notification, this.activity);
      } else {
        const activityTitle = this.activity?.templateParams?.comment
          || this.activity?.title
          || this.activity?.body
          || '';
        const originalActivity = this.activity?.originalActivity;
        const activityShareTitle = originalActivity
          && (originalActivity.templateParams?.comment
            || originalActivity.title
            || originalActivity.body)
            || '';
        if (activityShareTitle?.length && activityTitle?.length) {
          return `${activityTitle}: ${activityShareTitle}`;
        } else if (activityShareTitle?.length) {
          return activityShareTitle;
        } else {
          return activityTitle;
        }
      }
    },
    parentContent() {
      if (!this.parentActivity) {
        return '';
      } else if (this.extension?.getContent) {
        return this.extension.getContent(this.notification, this.parentActivity);
      } else {
        const activityTitle = this.parentActivity?.templateParams?.comment
          || this.parentActivity?.title
          || this.parentActivity?.body
          || '';
        const originalActivity = this.parentActivity?.originalActivity;
        const activityShareTitle = originalActivity
          && (originalActivity.templateParams?.comment
            || originalActivity.title
            || originalActivity.body)
            || '';
        if (activityShareTitle?.length && activityTitle?.length) {
          return `${activityTitle}: ${activityShareTitle}`;
        } else if (activityShareTitle?.length) {
          return activityShareTitle;
        } else {
          return activityTitle;
        }
      }
    },
    contentText() {
      return this.content?.length && this.$utils.htmlToText(this.content) || '';
    },
    parentContentText() {
      return this.parentContent?.length && this.$utils.htmlToText(this.parentContent) || '';
    },
  },
  created() {
    this.$activityService.getActivityById(this.commentId || this.activityId, 'shared')
      .then(activity => {
        this.activity = activity;
        return this.$nextTick();
      })
      .then(() => {
        if (this.activityType) {
          document.addEventListener(`extension-WebNotification-activity-notification-${this.activityType}-updated`, this.refreshActivityTypeExtensions);
          this.refreshActivityTypeExtensions();
        }
      })
      .then(() => {
        const parentId = this.reply && (this.activity?.parentCommentId || this.activity?.activityId);
        if (parentId) {
          return this.$activityService.getActivityById(parentId, 'shared');
        } else {
          return this.$nextTick();
        }
      })
      .then(activity => this.parentActivity = activity)
      .finally(() => this.loadingActivity = false);
  },
  beforeDestroy() {
    if (this.activityType) {
      document.removeEventListener(`extension-WebNotification-activity-notification-${this.activityType}-updated`, this.refreshActivityTypeExtensions);
    }
  },
  methods: {
    refreshActivityTypeExtensions() {
      if (this.activityType) {
        this.extension = extensionRegistry.loadExtensions('WebNotification', `activity-notification-${this.activityType}`).find(extension => !extension.isEnabled || extension.isEnabled(this.notification, this.activity));
      }
    },
  },
};
</script>
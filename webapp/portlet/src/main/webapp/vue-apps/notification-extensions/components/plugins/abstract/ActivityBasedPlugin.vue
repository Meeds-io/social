<template>
  <v-card
    v-if="loading"
    class="d-flex align-center justify-center ma-auto"
    tile
    flat>
    <v-progress-circular />
  </v-card>
  <user-notification-template
    v-else-if="activity"
    :notification="notification"
    :avatar-url="profileAvatarUrl"
    :message="message"
    :url="activityUrl">
    <template #actions>
      <div class="text-truncate">
        <v-icon size="14" class="me-1">{{ notificationIcon }}</v-icon>
        {{ contentText }}
      </div>
      <div v-if="reply" class="my-1">
        <v-btn
          :href="activityUrl"
          color="primary"
          elevation="0"
          small
          outlined>
          <v-icon size="14" class="me-1">far fa-comment</v-icon>
          {{ $t('Notification.label.Reply') }}
        </v-btn>
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
  },
  data: () => ({
    loading: true,
    extension: {
      isEnabled: null,
      getIcon: null,
      getMessage: null,
      getContent: null,
    },
  }),
  computed: {
    profile() {
      return this.notification?.from;
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
    activityType() {
      return this.activity?.type;
    },
    activityUrl() {
      return (this.commentId && this.activityId && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}#comment-${this.commentId}`)
        || (this.activityId && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`)
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
      if (this.extension?.getMessage) {
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
    contentText() {
      return this.content?.length && this.$utils.htmlToText(this.content) || '';
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
          this.extension = extensionRegistry.loadExtensions('WebNotification', `activity-notification-${this.activityType}`).find(extension => !extension.isEnabled || extension.isEnabled(this.notification, this.activity));
        }
        return this.$nextTick();
      })
      .finally(() => this.loading = false);
  },
};
</script>
<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<template>
  <user-notification-activity-base
    :notification="notification"
    :message-text="message"
    :from-identity="fromIdentity"
    message-key="Notification.intranet.message.PostReportPlugin">
    <template #reply>
      <v-btn
        :href="targetUrl"
        color="primary"
        elevation="0"
        small
        outlined
        @click.stop>
        <v-icon size="14" class="me-1">fa-exclamation-triangle</v-icon>
        <span class="text-none">
          {{ reviewLabel }}
        </span>
      </v-btn>
    </template>
  </user-notification-activity-base>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  computed: {
    fromIdentity() {
      return this.notification?.from;
    },
    activityId() {
      return this.notification?.parameters?.activityId;
    },
    commentId() {
      return this.notification?.parameters?.commentId;
    },
    targetUrl() {
      return (this.commentId && this.activityId
              && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.activityId}#comment-${this.commentId}`)
        || (this.activityId && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.activityId}`)
        || '#';
    },
    targetType() {
      return this.notification?.parameters?.reportedContentType || 'post';
    },
    reasonLabel() {
      const reason = this.notification?.parameters?.reportReason;
      const reasonKey = reason && `Notification.report.reason.${reason}`;
      return reasonKey && this.$t(reasonKey) !== reasonKey && this.$t(reasonKey) || reason || '';
    },
    targetTypeLabel() {
      const targetKey = `Notification.report.target.${this.targetType}`;
      return this.$t(targetKey) !== targetKey && this.$t(targetKey) || this.targetType;
    },
    reviewLabel() {
      return this.$t('Notification.label.PostReportPlugin.Review');
    },
    message() {
      return this.$t('Notification.intranet.message.PostReportPlugin', {
        0: `<a class="user-name font-weight-bold">${this.fromIdentity?.fullname || ''}</a>`,
        1: `<a href="${this.targetUrl}" target="_blank" class="font-weight-bold" onclick="event.stopPropagation()">${this.targetTypeLabel}</a>`,
        2: this.reasonLabel,
      });
    },
  },
};
</script>

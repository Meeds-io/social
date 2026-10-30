<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <div class="d-flex align-center">
    <v-btn
      v-for="reminder in visibleReminders"
      :id="reminder.id"
      :key="reminder.tab"
      :title="reminder.title"
      :aria-label="reminder.title"
      class="pendingUsersReminderButton ms-2"
      icon
      @click="$root.$emit('openPendingUsers', reminder.tab)">
      <v-badge
        :content="reminder.badge"
        :style="{'--badge-background-color': reminder.color}"
        class="badge-display"
        flat
        dense
        overlap>
        <v-icon size="20" class="icon-default-color">{{ reminder.icon }}</v-icon>
      </v-badge>
    </v-btn>
  </div>
</template>

<script>
import * as pendingUsersService from '../js/PendingUsersService.js';

/**
 * The two reminders above the users table: the accounts created and not
 * invited yet, and the invitations not accepted yet. Counted once when the
 * page opens, then again only after an action that changes them; the Pending
 * Users drawer's totals update them as it loads its tabs
 */
export default {
  data: () => ({
    counts: {
      creation: 0,
      invitations: 0,
    },
    // When the request behind each tab's shown count started
    requestedAt: {
      creation: 0,
      invitations: 0,
    },
  }),
  computed: {
    visibleReminders() {
      return [{
        tab: 'creation',
        id: 'pendingUsersReminderCreation',
        count: this.counts.creation,
        badge: this.badgeOf(this.counts.creation),
        color: 'var(--allPagesPrimaryColor, #578dc9)',
        icon: 'fa-user-plus',
        title: this.$t('UsersManagement.pending.creationReminder', {0: this.counts.creation}),
      }, {
        tab: 'invitations',
        id: 'pendingUsersReminderInvitations',
        count: this.counts.invitations,
        badge: this.badgeOf(this.counts.invitations),
        // the top bar's notification badge color
        color: 'var(--allPagesBadgePrimaryColor, #d32a2a)',
        icon: 'fa-user-clock',
        title: this.$t('UsersManagement.pending.invitationsReminder', {0: this.counts.invitations}),
      }].filter(reminder => reminder.count > 0);
    },
  },
  created() {
    this.$root.$on('pending-users-changed', this.refresh);
    this.$root.$on('pending-users-size', this.applyTabSize);
    this.refresh();
  },
  beforeDestroy() {
    this.$root.$off('pending-users-changed', this.refresh);
    this.$root.$off('pending-users-size', this.applyTabSize);
  },
  methods: {
    // The badge is capped like the top bar's notification badge; the title
    // keeps the exact count
    badgeOf(count) {
      return count > 99 ? '99+' : count;
    },
    refresh() {
      // A monotonic clock, the one the drawer stamps its pages with; a count
      // that could not be read leaves the shown one as it is
      const requestedAt = window.performance.now();
      return Promise.all(Object.keys(this.counts).map(tab =>
        pendingUsersService.countPendingUsers(tab)
          .then(count => this.applyTabSize(tab, count, requestedAt))
          .catch(() => null)));
    },
    // A tab's count, from the reminders' own request or from the Pending
    // Users drawer's page, replaces the shown one only when its request
    // started no earlier: an answer arriving late never shows a number from
    // before a newer request
    applyTabSize(tab, size, requestedAt) {
      if (Object.prototype.hasOwnProperty.call(this.counts, tab) && requestedAt >= this.requestedAt[tab]) {
        this.requestedAt[tab] = requestedAt;
        this.counts[tab] = size;
      }
    },
  },
};
</script>

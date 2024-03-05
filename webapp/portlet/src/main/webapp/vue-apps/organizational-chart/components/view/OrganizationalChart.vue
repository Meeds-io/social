<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <div>
    <div class="width-fit-content ma-auto">
      <div class="d-flex flex-wrap-reverse justify-center">
        <span
          v-if="!managers?.length"
          class="mb-2">
          <v-icon>
            fas fa-user-friends
          </v-icon>
          {{ $t('organizationalChart.noManager.label') }}
        </span>
        <chart-user-compact-card
          v-else
          v-for="manager in managers"
          :key="manager.id"
          class="mb-2 ms-2 me-2"
          :user="manager"
          @click="updateChart(manager)" />
      </div>
      <v-divider />
    </div>
    <div class="chartVerticalLine">
      <v-divider
        class="d-flex ma-auto"
        vertical />
    </div>
    <people-user-card
      id="user"
      :user="user"
      :preferences="preferences"
      :profile-action-extensions="profileActionExtensions" />
    <div class="chartVerticalLine">
      <v-divider
        class="d-flex ma-auto"
        vertical />
    </div>
    <div class="width-fit-content ma-auto">
      <v-divider class="mb-2" />
      <div class="d-flex flex-wrap justify-center">
        <span
          v-if="!managedUsers?.length">
          <v-icon>
            fas fa-user-friends
          </v-icon>
          {{ $t('organizationalChart.noSubordinate.label') }}
        </span>
        <chart-user-compact-card
          v-else
          v-for="managedUser in managedUsers"
          :key="managedUser.id"
          class="mb-2 ms-2 me-2"
          :user="managedUser"
          @click="updateChart(managedUser)" />
      </div>
    </div>
    <div
      class="mt-2 d-flex flex-column"
      v-if="hasMore">
      <v-btn
        :loading="isLoading"
        class="btn"
        flat
        outlined
        @click="loadMoreMangedUsers">
        {{ $t('Search.button.loadMore') }}
      </v-btn>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {
      scrollToViewProps: {behavior: 'smooth', block: 'center', inline: 'center'}
    };
  },
  props: {
    user: {
      type: Object,
      default: null
    },
    preferences: {
      type: Object,
      default: null,
    },
    managedUsers: {
      type: Array,
      default: () => []
    },
    profileActionExtensions: {
      type: Array,
      default: () => []
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    hasMore: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    managers() {
      return this.user?.managers?.filter(manager => manager.enabled)
        ?.sort((a, b) => this.usersNaturalComparator(a,b));
    }
  },
  mounted() {
    this.scrollUserToViewCenter();
  },
  methods: {
    usersNaturalComparator(a, b) {
      return this.$root.$children[0].usersNaturalComparator(a, b);
    },
    loadMoreMangedUsers() {
      this.$emit('load-more-managed-users');
    },
    updateChart(user) {
      this.$emit('update-chart', user);
    },
    scrollUserToViewCenter() {
      setTimeout(() => {
        document.getElementById('user').scrollIntoView(this.scrollToViewProps);
      }, 500);
    }
  }
};
</script>

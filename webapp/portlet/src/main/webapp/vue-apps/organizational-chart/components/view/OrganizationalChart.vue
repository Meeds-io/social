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
      <div
        class="d-flex flex-wrap-reverse flex-row justify-center">
        <span
          v-if="!listManagers?.length"
          class="text-header">
          <v-icon>
            fas fa-user-friends
          </v-icon>
          {{ $t('organizationalChart.noManager.label') }}
        </span>
        <chart-user-compact-card
          v-else
          v-for="manager in listManagers"
          :key="manager.id"
          class="mt-5 ms-3 me-3"
          :user="manager"
          @click="updateChart(manager)" />
      </div>
      <v-divider class="mt-2 mb-1 mx-auto" />
    </div>
    <div class="chartVerticalLine">
      <v-divider
        class="d-flex mx-auto"
        vertical />
    </div>
    <people-user-card
      id="chartCenterUser"
      class="my-1"
      width="268"
      :ignored-navigation-extensions="['user-chart']"
      :user="user"
      :user-navigation-extensions="userNavigationExtensions"
      :profile-action-extensions="profileActionExtensions" />
    <div class="chartVerticalLine">
      <v-divider
        class="d-flex mx-auto"
        vertical />
    </div>
    <div class="width-fit-content ma-auto">
      <v-divider class="mb-2 mt-1 mx-2" />
      <div
        class="d-flex flex-wrap justify-center">
        <span
          class="text-header"
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
          class="mb-6 ms-3 me-3"
          :user="managedUser"
          @click="updateChart(managedUser)" />
      </div>
    </div>
    <div
      class="mt-2 d-flex flex-column"
      v-if="hasMore && !preview">
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
    managedUsers: {
      type: Array,
      default: () => []
    },
    profileActionExtensions: {
      type: Array,
      default: () => []
    },
    userNavigationExtensions: {
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
    },
    preview: {
      type: Boolean,
      default: false
    },
    previewCount: {
      type: Number,
      default: 2
    }
  },
  computed: {
    listManagers() {
      return this.preview && this.managers?.length
                          && this.managers.slice(0, this.previewCount) || this.managers;
    },
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
      this.$emit('update-chart', user.id);
    },
    scrollUserToViewCenter() {
      setTimeout(() => {
        document.getElementById('chartCenterUser').scrollIntoView(this.scrollToViewProps);
      }, 500);
    }
  }
};
</script>

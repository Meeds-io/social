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
  <v-hover v-model="hover">
    <v-card
      :id="userMenuParentId"
      class="peopleCardItem d-flex mx-2"
      :class="hover && 'grey lighten-4'"
      flat>
      <div class="peopleToolbarIcons my-auto ms-auto">
        <v-avatar
          v-if="user.isGroupBound"
          class="peopleGroupMemberBindingIcon d-flex mx-2 my-0"
          :size="28"
          :title="$t('peopleList.label.groupBound')">
          <v-icon
            color="white"
            size="12">
            fa-users
          </v-icon>
        </v-avatar>
        <people-user-menu
          attach-menu
          :bottom-menu="$root.isMobile"
          :display-menu-button="$root.isMobile || hover"
          menu-button-class="me-1"
          :profile-action-extensions="profileActionExtensions"
          :space-id="spaceId"
          :space-members-extensions="spaceMembersExtensions"
          :user="user"
          :user-navigation-extensions="userNavigationExtensions" />
      </div>
      <div class="peopleAvatar">
        <a
          :aria-label="$t('profileSettings.label.profile')"
          :href="url">
          <v-img
            class="mx-auto"
            eager
            height="40px"
            :lazy-src="`${userAvatarUrl}`"
            max-height="40px"
            max-width="40px"
            :src="`${userAvatarUrl}`"
            transition="none"
            width="40px" />
        </a>
      </div>
      <v-card-text
        class="peopleCardBody align-center d-flex full-height"
        :class="{
          'py-0': mobileDisplay,
          'py-1': !mobileDisplay,
        }">
        <div class="my-auto">
          <a
            class="userFullname font-weight-bold text-capitalize"
            :class="usernameClass"
            :href="url"
            :title="user.fullname">
            {{ user.fullname }}
            <span
              v-if="externalUser"
              class="externalFlagClass">
              {{ $t('peopleList.label.external') }}
            </span>
          </a>
          <v-card-subtitle
            class="userPositionLabel text-truncate pa-0"
            :class="{
              'mt-0': mobileDisplay,
              'mt-auto': !mobileDisplay,
            }">
            <a
              class="grey--text text--darken-1"
              :href="url">
              {{ userPosition }}
            </a>
          </v-card-subtitle>
        </div>
      </v-card-text>
    </v-card>
  </v-hover>
</template>
<script>
  export default {
    props: {
      user: {
        type: Object,
        default: null,
      },
      spaceId: {
        type: String,
        default: null,
      },
      userNavigationExtensions: {
        type: Array,
        default: () => [],
      },
      profileActionExtensions: {
        type: Array,
        default: () => [],
      },
      spaceMembersExtensions: {
        type: Array,
        default: () => [],
      },
      mobileDisplay: {
        type: Boolean,
        default: false,
      },
      url: {
        type: String,
        default: null,
      },
      userAvatarUrl: {
        type: String,
        default: null,
      },
      isUpdatingStatus: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      hover: false,
    }),
    computed: {
      userMenuParentId () {
        return this.user?.id && `userMenuParent-${this.user.id}` || 'userMenuParent';
      },
      usernameClass () {
        return `${(!this.user.enabled || this.user.deleted) && 'text-subtitle' || 'primary--text text-truncate-2 mt-0'}`;
      },
      userPosition () {
        return this.user?.position || '';
      },
      externalUser () {
        return this.user?.external === 'true';
      },
    },
  };
</script>

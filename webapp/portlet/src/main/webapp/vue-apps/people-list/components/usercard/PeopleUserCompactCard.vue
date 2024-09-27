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
  <v-hover v-slot="{hover}">
    <v-card
      :id="userMenuParentId"
      :class="hover && 'grey lighten-4'"
      class="peopleCardItem d-flex mx-2"
      flat>
      <div class="peopleToolbarIcons my-auto ms-auto">
        <v-btn
          v-if="user.isGroupBound"
          :title="$t('peopleList.label.groupBound')"
          :ripple="false"
          color="grey"
          class="peopleGroupMemberBindingIcon d-flex not-clickable ms-1"
          icon
          small>
          <span class="d-flex uiIconGroup"></span>
        </v-btn>
        <v-spacer />
        <div v-if="$root.isMobile">
          <v-icon
            size="14"
            class="my-1"
            @click="openBottomMenu">
            fas fa-ellipsis-v
          </v-icon>
        </div>
        <template v-else-if="canUseActionsMenu">
          <v-menu
            ref="actionMenu"
            v-model="displayActionMenu"
            attach
            transition="slide-x-reverse-transition"
            content-class="peopleActionMenu mt-n6 me-4"
            offset-y>
            <template #activator="{ on, attrs }">
              <v-btn
                v-show="hover"
                v-bind="attrs"
                v-on="on"
                :title="$t('peopleList.label.openUserMenu')"
                icon
                text>
                <v-icon
                  class="icon-default-size icon-default-color">
                  mdi-dots-vertical
                </v-icon>
              </v-btn>
            </template>
            <v-list class="pa-0 white" dense>
              <template v-if="$root.spaceId && spaceMembersExtensions?.length">
                <v-list-item
                  v-for="extension in spaceMembersExtensions"
                  :key="extension.id"
                  @click="extension.click(user)">
                  <v-list-item-title class="align-center d-flex">
                    <v-card
                      class="d-flex align-center justify-center transparent"
                      height="25"
                      width="25"
                      flat>
                      <v-icon size="20">
                        {{ extension.class }}
                      </v-icon>
                    </v-card>
                    <span class="ms-3">
                      {{ extension.title || $t(extension.titleKey) }}
                    </span>
                  </v-list-item-title>
                </v-list-item>
              </template>
              <template v-else>
                <v-list-item
                  v-for="(extension, i) in actionExtensions"
                  :key="i"
                  @click="extension.click(user)">
                  <v-list-item-title class="align-center d-flex">
                    <v-card
                      class="d-flex align-center justify-center transparent"
                      height="25"
                      width="25"
                      flat>
                      <v-icon size="20">
                        {{ extension.class }}
                      </v-icon>
                    </v-card>
                    <span class="ms-3">
                      {{ extension.title || $t(extension.titleKey) }}
                    </span>
                  </v-list-item-title>
                </v-list-item>
              </template>
            </v-list>
          </v-menu>
        </template>
      </div>
      <div class="peopleAvatar">
        <a :href="url">
          <v-img
            :lazy-src="`${userAvatarUrl}`"
            :src="`${userAvatarUrl}`"
            transition="none"
            class="mx-auto"
            height="40px"
            width="40px"
            max-height="40px"
            max-width="40px"
            eager />
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
            :href="url"
            :title="user.fullname"
            :class="usernameClass"
            class="userFullname font-weight-bold text-capitalize">
            {{ user.fullname }}
            <span v-if="externalUser" class="externalFlagClass">
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
              :href="url"
              class="grey--text text--darken-1">
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
    isManager: {
      type: Boolean,
      default: false,
    },
    userNavigationExtensions: {
      type: Array,
      default: () => [],
    },
    enabledProfileActionExtensions: {
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
      default: null
    },
    userAvatarUrl: {
      type: String,
      default: null
    },
    isUpdatingStatus: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    displayActionMenu: false,
    waitTimeUntilCloseMenu: 200,
    bottomMenu: false,
  }),
  computed: {
    actionExtensions() {
      if (!this.isSameUser) {
        return [...this.enabledProfileActionExtensions, ...this.filteredUserNavigationExtensions];
      } else {
        return this.filteredUserNavigationExtensions;
      }
    },
    filteredUserNavigationExtensions() {
      return this.userNavigationExtensions.filter(extension => extension.enabled(this.user));
    },
    isSameUser() {
      return this.user?.username === eXo?.env?.portal?.userName;
    },
    userMenuParentId() {
      return this.user?.id && `userMenuParent-${this.user.id}` || 'userMenuParent';
    },
    canUseActionsMenu() {
      return this.user && (this.enabledProfileActionExtensions.length || this.userNavigationExtensions.length);
    },
    usernameClass() {
      return `${(!this.user.enabled || this.user.deleted) && 'text-subtitle' || 'primary--text text-truncate-2 mt-0'}`;
    },
    userPosition() {
      return this.user?.position || '';
    },
    externalUser() {
      return this.user?.external === 'true';
    },
  },
  watch: {
    displayActionMenu(newVal) {
      if (newVal) {
        document.getElementById(`peopleCardItem${this.user.id}`).style.zIndex = 3;
      } else {
        document.getElementById(`peopleCardItem${this.user.id}`).style.zIndex = 0;
      }
    }
  },
  created() {
    $(document).on('mousedown', () => {
      if (this.displayActionMenu) {
        window.setTimeout(() => {
          this.displayActionMenu = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
  },
  methods: {
    openBottomMenu() {
      if (!this.isSameUser) {
        this.$root.$emit('open-people-compact-card-options-drawer',
          this.user, [...this.enabledProfileActionExtensions, ...this.filteredUserNavigationExtensions], this.spaceMembersExtensions);
      } else {
        this.$root.$emit('open-people-compact-card-options-drawer',
          this.user, [...this.filteredUserNavigationExtensions], this.spaceMembersExtensions);
      }
    }
  },
};
</script>

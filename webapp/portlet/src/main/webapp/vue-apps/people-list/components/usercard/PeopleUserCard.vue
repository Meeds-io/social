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
  <v-hover v-slot="{ hover }">
    <v-card
      :class="hover && 'elevation-2'"
      class="mx-auto border-box-sizing card-border-radius socialUserCard"
      width="300"
      height="235"
      :href="profileUrl"
      outlined>
      <v-img
        :lazy-src="bannerUrl"
        :src="bannerUrl"
        height="50px">
        <div class="ms-11 ps-11 mt-2 position-absolute z-index-two">
          <people-user-role
            v-if="isManager"
            :title="$t('peopleList.label.spaceManager')"
            :icon="'fas fa-user-cog'" />
          <people-user-role
            v-if="isRedactor"
            :title="$t('peopleList.label.spaceRedactor')"
            :icon="'fas fa-user-edit'" />
          <people-user-role
            v-if="isPublisher"
            :title="$t('peopleList.label.spacePublisher')"
            :icon="'fas fa-paper-plane'" />
          <people-user-role
            v-if="isGroupBound"
            :title="$t('peopleList.label.groupBound')"
            :icon="'fas fa-users'" />
        </div>
        <div
          v-if="spaceMembersExtensions.length"
          class="full-width position-absolute z-index-two">
          <v-menu
            v-if="hover || showMenu"
            ref="actionMenu"
            v-model="menu"
            transition="slide-x-reverse-transition"
            content-class="mt-6 ms-5"
            left
            offset-x>
            <template #activator="{ on, attrs }">
              <v-btn
                v-bind="attrs"
                v-on="on"
                :title="$t('peopleList.label.openUserMenu')"
                class="d-block grey darken-1 mt-2 ms-auto me-2"
                width="21"
                height="21"
                icon
                text
                @click.prevent>
                <v-icon
                  class="white--text"
                  size="13">
                  fas fa-ellipsis-v
                </v-icon>
              </v-btn>
            </template>
            <v-list class="pa-0 white" dense>
              <v-list-item
                v-for="(extension, i) in spaceMembersExtensions"
                :key="i"
                @click="extension.click(user)">
                <v-list-item-title class="align-center d-flex">
                  <v-icon
                    size="15">
                    {{ extension.class }}
                  </v-icon>
                  <span class="mx-2">
                    {{ extension.title }}
                  </span>
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </div>
      </v-img>
      <div class="d-flex">
        <v-avatar
          class="mt-n12 ms-5"
          size="80">
          <v-img
            :src="`${avatarUrl}&size=80x80`"
            :alt="fullName"
            eager />
        </v-avatar>
        <v-card-title
          class="ps-1 pe-2 py-2 userCardTitle align-baseline">
          <p
            class="text-truncate-2 text-break text-subtitle-1 font-weight-bold mb-0">
            {{ fullName }}
            <span
              v-if="externalUser"
              class="grey--text">
              {{ $t('peopleList.label.external') }}
            </span>
          </p>
        </v-card-title>
      </div>
      <div class="userFieldsArea">
        <p class="mb-0 text-body-2 px-3 text-truncate">
          {{ firstField }}
        </p>
        <v-card-subtitle
          class="px-3">
          <p class="mb-0 text-truncate">
            {{ secondField }}
          </p>
          <p class="mb-0 text-truncate">
            {{ thirdField }}
          </p>
        </v-card-subtitle>
      </div>
      <v-card-actions
        v-if="!isSameUser"
        class="justify-end d-flex width-full">
        <v-btn
          v-for="extension in profileActionExtensions"
          :key="extension.id"
          icon
          @click.prevent="extension.click(user)">
          <v-icon
            :title="extension.title"
            size="22">
            {{ extension.class }}
          </v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-hover>
</template>

<script>
export default {
  data() {
    return {
      menu: false,
      defaultPreferences: {
        firstField: 'position',
        secondField: 'team',
        thirdField: 'city'
      }
    };
  },
  props: {
    user: {
      type: Object,
      default: null
    },
    profileActionExtensions: {
      type: Array,
      default: () => []
    },
    userNavigationExtensions: {
      type: Array,
      default: () => []
    },
    ignoredNavigationExtensions: {
      type: Array,
      default: () => []
    },
    preferences: {
      type: Object,
      default: null
    },
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    showMenu() {
      return this.menu || this.isMobile;
    },
    preferencesObject() {
      return this.preferences || this.defaultPreferences;
    },
    filteredUserNavigationExtensions() {
      return this.userNavigationExtensions.filter(extension => extension.enabled(this.user)
        && !this.ignoredNavigationExtensions.includes(extension?.id));
    },
    filteredProfileActionExtensions() {
      return this.profileActionExtensions.filter(extension => extension.enabled(this.user?.dataEntity || this.user));
    },
    isSameUser() {
      return this.user?.username === eXo?.env?.portal?.userName;
    },
    fieldsToDisplay() {
      return this.user?.properties?.filter(property => Object.values(this.preferencesObject).includes(property.propertyName));
    },
    firstField() {
      return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferencesObject.firstField)[0]?.value;
    },
    secondField() {
      return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferencesObject.secondField)[0]?.value;
    },
    thirdField() {
      return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferencesObject.thirdField)[0]?.value;
    },
    bannerUrl() {
      return this.user?.banner;
    },
    avatarUrl() {
      return this.user?.avatar;
    },
    fullName() {
      return this.user?.fullname;
    },
    enabled() {
      return this.user?.enabled;
    },
    externalUser() {
      return this.user?.external;
    },
    profileUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.user?.username}`;
    }
  }
};
</script>

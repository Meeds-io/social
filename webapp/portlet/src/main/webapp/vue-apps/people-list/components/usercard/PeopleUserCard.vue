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
      :width="$attrs.width"
      :min-width="$attrs.minWidth"
      height="227"
      :href="profileUrl"
      outlined>
      <v-img
        :lazy-src="bannerUrl"
        :src="bannerUrl"
        height="50px" />
      <div class="d-flex">
        <v-avatar
          class="mt-n10 ms-5"
          size="65">
          <v-img
            :src="`${avatarUrl}&size=65x65`"
            :alt="fullName"
            eager />
        </v-avatar>
        <v-card-title
          class="ps-1 pe-2 py-2 userCardTitle align-baseline">
          <p
            class="text-truncate-2 text-break text-subtitle-2 font-weight-bold mb-0">
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
        <p class="mb-0 text-subtitle-2 px-3 text-truncate">
          {{ firstField }}
        </p>
        <v-card-subtitle
          class="px-3">
          <p class="mb-0 text-subtitle-2 text-truncate">
            {{ secondField }}
          </p>
          <p class="mb-0 text-subtitle-2 text-truncate">
            {{ thirdField }}
          </p>
        </v-card-subtitle>
      </div>
      <v-card-actions>
        <div class="me-auto">
          <v-btn
            v-for="extension in filteredUserNavigationExtensions"
            :key="extension.id"
            icon
            @click.prevent="extension.click(user)">
            <v-icon
              :title="$t(extension.titleKey)"
              size="22">
              {{ extension.class }}
            </v-icon>
          </v-btn>
        </div>
        <div
          v-if="!isSameUser"
          class="ms-auto">
          <v-btn
            v-for="extension in filteredProfileActionExtensions"
            :key="extension.id"
            icon
            @click.prevent="extension.click(user)">
            <v-icon
              :title="extension.title"
              size="22">
              {{ extension.class }}
            </v-icon>
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-hover>
</template>

<script>
export default {
  data() {
    return {
      preferences: {
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
    }
  },
  computed: {
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
      return this.user?.properties?.filter(property => Object.values(this.preferences).includes(property.propertyName));
    },
    firstField() {
      return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.firstField)[0]?.value;
    },
    secondField() {
      return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.secondField)[0]?.value;
    },
    thirdField() {
      return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.thirdField)[0]?.value;
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

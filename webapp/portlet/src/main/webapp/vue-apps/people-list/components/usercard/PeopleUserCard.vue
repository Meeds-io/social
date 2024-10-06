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
      :width="$attrs.width"
      :min-width="$attrs.minWidth"
      :href="profileUrl"
      class="mx-auto border-box-sizing socialUserCard"
      height="227"
      outlined>
      <v-img
        :lazy-src="bannerUrl"
        :src="bannerUrl"
        height="50px">
        <div
          :class="$vuetify.rtl && 'l-0' || 'r-0'"
          class="position-absolute">
          <people-user-menu
            v-if="spaceId"
            :user="user"
            :space-id="spaceId"
            :space-members-extensions="spaceMembersExtensions"
            :display-menu-button="$root.isMobile || hover"
            :bottom-menu="$root.isMobile"
            menu-button-class="mt-2 me-2"
            dark />
        </div>
        <people-user-card-roles
          :user="user"
          class="ms-11 ps-11 mt-2 position-absolute z-index-two" />
      </v-img>
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
            class="text-truncate-2 text-body font-weight-bold mb-0">
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
        <p class="mb-0 px-3 text-truncate">
          {{ firstField }}
        </p>
        <v-card-subtitle
          class="px-3">
          <p class="mb-0 ext-truncate">
            {{ secondField }}
          </p>
          <p class="mb-0 text-truncate">
            {{ thirdField }}
          </p>
        </v-card-subtitle>
      </div>
      <v-card-actions>
        <div class="me-auto">
          <v-btn
            v-for="extension in filteredUserNavigationExtensions"
            :key="extension.id"
            :title="extension.title || $t(extension.titleKey)"
            icon
            @touchstart.stop="0"
            @touchend.stop="0"
            @mousedown.stop="0"
            @mouseup.stop="0"
            @click.stop.prevent="extension.click(user)">
            <v-card
              class="d-flex align-center justify-center transparent"
              height="25"
              width="25"
              flat>
              <v-icon size="20">
                {{ extension.class }}
              </v-icon>
            </v-card>
          </v-btn>
        </div>
        <div
          v-if="!isSameUser"
          class="ms-auto d-flex">
          <span
            v-for="extension in filteredProfileActionExtensions"
            :key="extension.id">
            <v-btn
              v-if="!extension.init"
              :aria-label="extension.title || $t(extension.titleKey)"
              icon
              @touchstart.stop="0"
              @touchend.stop="0"
              @mousedown.stop="0"
              @mouseup.stop="0"
              @click.stop.prevent="extension.click(user)">
              <v-icon
                :title="extension.title || $t(extension.titleKey)"
                size="20">
                {{ extension.class }}
              </v-icon>
            </v-btn>
            <span
              v-else
              :class="`${extension.appClass} ${extension.typeClass}`"
              :ref="extension.id">
            </span>
          </span>
        </div>
      </v-card-actions>
    </v-card>
  </v-hover>
</template>
<script>
export default {
  props: {
    user: {
      type: Object,
      default: null
    },
    spaceId: {
      type: String,
      default: null
    },
    profileActionExtensions: {
      type: Array,
      default: () => []
    },
    spaceMembersExtensions: {
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
    compactDisplay: {
      type: Boolean,
      default: false
    },
    preferences: {
      type: Object,
      default: null
    },
  },
  data: () => ({
    menu: false,
  }),
  computed: {
    filteredSpaceMembersExtensions() {
      return this.spaceId && this.spaceMembersExtensions?.filter?.(extension => extension.enabled(this.user)) || [];
    },
    filteredUserNavigationExtensions() {
      return this.userNavigationExtensions.filter(extension => extension.enabled(this.user)
        && !this.ignoredNavigationExtensions.includes(extension?.id));
    },
    filteredProfileActionExtensions() {
      return this.profileActionExtensions.filter(extension => extension.enabled(this.user?.dataEntity || this.user));
    },
    fieldsToDisplay() {
      return this.user?.properties?.filter(property => Object.values(this.preferences).includes(property.propertyName));
    },
    isSameUser() {
      return this.user?.username === eXo?.env?.portal?.userName;
    },
    firstField() {
      if (this.preferences) {
        return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.firstField)[0]?.value;
      } else {
        return this.user?.primaryProperty;
      }
    },
    secondField() {
      if (this.preferences) {
        return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.secondField)[0]?.value;
      } else {
        return this.user?.secondaryProperty;
      }
    },
    thirdField() {
      if (this.preferences) {
        return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.thirdField)[0]?.value;
      } else {
        return this.user?.tertiaryProperty;
      }
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
    },
    isManager() {
      return this.user?.isManager;
    },
    isRedactor() {
      return this.user?.isSpaceRedactor;
    },
    isPublisher() {
      return this.user?.isSpacePublisher;
    },
    isGroupBound() {
      return this.user?.isGroupBound;
    }
  },
  created() {
    document.addEventListener('mousedown',  this.closeMenu);
  },
  mounted() {
    this.initExtensions();
  },
  methods: {
    initExtensions() {
      this.filteredProfileActionExtensions.forEach((extension) => {
        if (extension.init) {
          let container = this.$refs[extension.id];
          if (container && container.length > 0) {
            container = container[0];
            extension.init(container, this.user.username);
          }
        }
      });
    }
  }
};
</script>

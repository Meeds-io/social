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
      class="d-flex flex-column application-border-radius border-color socialUserCard"
      :class="hover && 'elevation-2'"
      :height="cardHeight"
      :href="profileUrl"
      :min-width="$attrs.minWidth"
      outlined
      :width="$attrs.width">
      <v-card
        class="overflow-hidden application-border-radius-top no-border-bottom-right-radius no-border-bottom-left-radius position-relative d-flex justify-center z-index-one"
        flat
        :max-height="bannerHeight"
        max-width="100%"
        :min-height="bannerHeight"
        min-width="100%">
        <img
          :alt="$t('peopleList.userBanner.alt')"
          class="position-absolute"
          height="100%"
          :src="bannerUrl"
          style="max-width: 1000%; min-width: 100%; max-height: 100%; min-height: 100%;"
          width="auto">
      </v-card>
      <v-card
        class="d-flex mt-n9 pt-2px px-2"
        flat
        :max-height="avatarSize"
        max-width="100%"
        :min-height="avatarSize"
        min-width="100%">
        <v-card
          class="overflow-hidden d-flex border-radius-circle align-center justify-center z-index-two"
          flat
          :max-height="avatarSize"
          :max-width="avatarSize"
          :min-height="avatarSize"
          :min-width="avatarSize">
          <img
            :alt="$t('peopleList.userAvatar.alt')"
            class="overflow-hidden border-radius"
            height="100%"
            :src="`${avatarUrl}&size=65x65`"
            style="max-width: 1000%; max-height: 100%;"
            width="auto">
        </v-card>
        <div class="d-flex flex-grow-1 align-center flex-shrink-1 ps-1 pt-4">
          <people-user-card-roles
            class="mt-4"
            :user="user" />
        </div>
      </v-card>
      <div
        class="font-weight-bold overflow-hidden text-truncate-2 justify-center px-2 mt-4 max-height-2lh"
        :title="fullName">
        {{ fullName }}
        <span
          v-if="externalUser"
          class="grey--text">
          {{ $t('peopleList.label.external') }}
        </span>
      </div>
      <div class="userFieldsArea px-2">
        <div
          v-if="firstField?.length"
          class="text-truncate mt-1"
          :title="firstField">
          {{ firstField }}
        </div>
        <div
          v-if="secondField?.length"
          class="text-subtitle text-truncate mt-1"
          :title="secondField">
          {{ secondField }}
        </div>
        <div
          v-if="thirdField?.length"
          class="text-subtitle text-truncate mt-1"
          :title="thirdField">
          {{ thirdField }}
        </div>
      </div>
      <div class="d-flex full-width position-absolute b-0 mb-1 px-1">
        <div class="me-auto">
          <v-btn
            v-for="extension in filteredUserNavigationExtensions"
            :key="extension.id"
            icon
            :title="extension.title || $t(extension.titleKey)"
            @click.stop.prevent="extension.click(user)"
            @mousedown.stop="0"
            @mouseup.stop="0"
            @touchend.stop="0"
            @touchstart.stop="0">
            <v-card
              class="d-flex align-center justify-center transparent"
              flat
              height="25"
              width="25">
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
              @click.stop.prevent="extension.click(user)"
              @mousedown.stop="0"
              @mouseup.stop="0"
              @touchend.stop="0"
              @touchstart.stop="0">
              <v-icon
                size="20"
                :title="extension.title || $t(extension.titleKey)">
                {{ extension.class }}
              </v-icon>
            </v-btn>
            <span
              v-else
              :ref="extension.id"
              :class="`${extension.appClass} ${extension.typeClass}`"></span>
          </span>
        </div>
        <people-user-menu
          v-if="spaceId"
          :bottom-menu="$root.isMobile"
          dark
          display-menu-button
          :space-id="spaceId"
          :space-members-extensions="spaceMembersExtensions"
          :user="user" />
      </div>
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
      profileActionExtensions: {
        type: Array,
        default: () => [],
      },
      spaceMembersExtensions: {
        type: Array,
        default: () => [],
      },
      userNavigationExtensions: {
        type: Array,
        default: () => [],
      },
      ignoredNavigationExtensions: {
        type: Array,
        default: () => [],
      },
      compactDisplay: {
        type: Boolean,
        default: false,
      },
      preferences: {
        type: Object,
        default: null,
      },
    },
    data: () => ({
      cardHeight: 254,
      avatarSize: 65,
      bannerHeight: 50,
      menu: false,
    }),
    computed: {
      filteredSpaceMembersExtensions () {
        return this.spaceId && this.spaceMembersExtensions?.filter?.(extension => extension.enabled(this.user)) || [];
      },
      filteredUserNavigationExtensions () {
        return this.userNavigationExtensions.filter(extension => extension.enabled(this.user)
          && !this.ignoredNavigationExtensions.includes(extension?.id));
      },
      filteredProfileActionExtensions () {
        return this.profileActionExtensions.filter(extension => extension.enabled(this.user?.dataEntity || this.user));
      },
      fieldsToDisplay () {
        return this.user?.properties?.filter(property => Object.values(this.preferences).includes(property.propertyName));
      },
      isSameUser () {
        return this.user?.username === eXo?.env?.portal?.userName;
      },
      firstField () {
        if (this.preferences) {
          return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.firstField)[0]?.value;
        } else {
          return this.user?.primaryProperty;
        }
      },
      secondField () {
        if (this.preferences) {
          return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.secondField)[0]?.value;
        } else {
          return this.user?.secondaryProperty;
        }
      },
      thirdField () {
        if (this.preferences) {
          return this.fieldsToDisplay?.filter(property => property.propertyName === this.preferences.thirdField)[0]?.value;
        } else {
          return this.user?.tertiaryProperty;
        }
      },
      bannerUrl () {
        return this.user?.banner;
      },
      avatarUrl () {
        return this.user?.avatar;
      },
      fullName () {
        return this.user?.fullname;
      },
      enabled () {
        return this.user?.enabled;
      },
      externalUser () {
        return this.user?.external === 'true' || this.user?.dataEntity?.external === 'true';
      },
      profileUrl () {
        return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.user?.username}`;
      },
      isManager () {
        return this.user?.isManager;
      },
      isRedactor () {
        return this.user?.isSpaceRedactor;
      },
      isPublisher () {
        return this.user?.isSpacePublisher;
      },
      isGroupBound () {
        return this.user?.isGroupBound;
      },
    },
    created () {
      document.addEventListener('mousedown',  this.closeMenu);
    },
    mounted () {
      this.initExtensions();
    },
    methods: {
      initExtensions () {
        this.filteredProfileActionExtensions.forEach(extension => {
          if (extension.init) {
            let container = this.$refs[extension.id];
            if (container && container.length > 0) {
              container = container[0];
              extension.init(container, this.user.username);
            }
          }
        });
      },
    },
  };
</script>

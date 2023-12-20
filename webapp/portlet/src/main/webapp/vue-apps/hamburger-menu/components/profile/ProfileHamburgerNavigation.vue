<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-flex id="ProfileHamburgerNavigation">
    <v-list-item
      :href="PROFILE_URI"
      class="accountTitleItem top-bar-height">
      <v-list-item-avatar size="44" class="me-3 mt-0 mb-0 elevation-1">
        <img
          :src="avatar"
          :alt="fullName"
          width="44"
          height="44">
      </v-list-item-avatar>
      <v-list-item-content class="py-0 accountTitleLabel">
        <v-list-item-title class="font-weight-bold body-2 mb-0">{{ fullName }} <span v-if="external" class="externalFlagClass">{{ $t('menu.profile.external') }}</span></v-list-item-title>
        <v-list-item-subtitle class="font-italic caption">{{ position }}</v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action v-if="stickyAllowed" class="my-auto">
        <v-btn
          :title="value && $t('menu.collapse') || $t('menu.expand')"
          icon
          @click="changeMenuStickiness">
          <v-icon>{{ arrowIconClass }}</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
  </v-flex>
</template>

<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    stickyAllowed: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      PROFILE_URI: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile`,
      IDENTITY_REST_API_URI: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${eXo.env.portal.userIdentityId}`,
      profile: null,
    };
  },
  computed: {
    arrowIconClass() {
      return this.value && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$root.ltr && 'fa-angle-double-left' || 'fa-angle-double-right';
    },
    arrowIconRight() {
      return this.$root.ltr && 'fa-angle-double-right' || 'fa-angle-double-left';
    },
    avatar() {
      return this.profile && this.profile.avatar || '';
    },
    fullName() {
      return this.profile && this.profile.fullname || '';
    },
    position() {
      return this.profile && this.profile.position || '';
    },
    external() {
      return this.profile && this.profile.dataEntity && this.profile.dataEntity.external === 'true' ;
    },
  },
  created() {
    document.addEventListener('userModified', event => {
      if (event && event.detail) {
        Object.assign(this.profile, event.detail);
      }
    });
    this.retrieveUserInformation();
  },
  methods: {
    retrieveUserInformation() {
      this.profile = this.$currentUserIdentity && this.$currentUserIdentity.profile;
      if (!this.profile) {
        return this.$identityService.getIdentityById(eXo.env.portal.userIdentityId)
          .then(data => this.profile = data && data.profile);
      }
    },
    changeMenuStickiness(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'APPLICATION', 'HamburgerMenu', 'Sticky', String(!this.value))
        .then(() => this.$emit('input', !this.value));
    },
  },
};
</script>

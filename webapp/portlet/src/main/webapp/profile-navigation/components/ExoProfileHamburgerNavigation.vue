<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
    <v-row class="accountTitleWrapper mx-0">
      <v-list-item
        :href="PROFILE_URI"
        class="accountTitleItem py-3">
        <v-list-item-avatar size="44" class="mr-3 mt-0 mb-0 elevation-1">
          <v-img :src="avatar"/>
        </v-list-item-avatar>
        <v-list-item-content class="py-0 accountTitleLabel">
          <v-list-item-title class="font-weight-bold body-2 mb-0">{{ fullName }}</v-list-item-title>
          <v-list-item-subtitle class="font-italic caption">{{ position }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-row>
  </v-flex>
</template>
<script>
export default {
  data() {
    return {
      PROFILE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile`,
      IDENTITY_REST_API_URI: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${eXo.env.portal.userIdentityId}`,
      DEFAULT_AVATAR: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${eXo.env.portal.userName}/avatar`,
      profile: null,
    };
  },
  computed: {
    avatar() {
      return this.profile && this.profile.avatar || this.DEFAULT_AVATAR;
    },
    fullName() {
      return this.profile && this.profile.fullname;
    },
    position() {
      return this.profile && this.profile.position;
    },
  },
  created() {
    fetch(this.IDENTITY_REST_API_URI, {
      method: 'GET',
      credentials: 'include',
    })
      .then(data => data && data.ok && data.json())
      .then(data => this.profile = data && data.profile)
      .finally(() => {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      });
    document.addEventListener('userModified', event => {
      if (event && event.detail) {
        Object.assign(this.profile, event.detail);
      }
    });
  },
};
</script>

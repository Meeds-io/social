<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-app
    v-if="displayApp"
    :class="owner && 'profileAboutMe' || 'profileAboutMeOther'">
    <widget-wrapper
      :title="title"
      extra-class="application-body">
      <template #action>
        <v-btn
          v-if="owner"
          id="aboutMeEditButton"
          icon
          outlined
          small
          :aria-label="$t('profileSettings.label.edit')"
          @click="editAboutMe">
          <v-icon size="18">fas fa-edit</v-icon>
        </v-btn>
      </template>
      <div
        v-if="hasAboutMe || !owner"
        v-sanitized-html="aboutMe"
        id="aboutMeParagraph"
        class="text-color"></div>
      <div
        v-else
        id="aboutMeParagraph"
        class="text-color">
        {{ $t('profileAboutMe.emptyOwner') }}
      </div>
    </widget-wrapper> 
    <profile-about-me-drawer
      v-if="owner && initialized"
      ref="aboutMeDrawer"
      v-model="aboutMe" />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    aboutMe: null,
    initialized: false,
    drawer: false,
  }),
  computed: {
    title() {
      return this.owner && this.$t('profileAboutYouself.title') || this.$t('profileAboutMe.title');
    },
    aboutMeText() {
      return this.$utils.htmlToText(this.aboutMe);
    },
    hasAboutMe() {
      return this.aboutMeText?.trim?.()?.length;
    },
    displayApp() {
      return !this.initialized || this.owner || this.hasAboutMe;
    },
  },
  watch: {
    displayApp() {
      this.$root.$updateApplicationVisibility(this.displayApp);
    }
  },
  created() {
    this.$userService.getUser(eXo.env.portal.profileOwner)
      .then(user => {
        this.aboutMe = user && user.aboutMe || '';
        return this.$nextTick();
      })
      .then(() => this.$root.$emit('application-loaded'))
      .finally(() => {
        this.$root.$applicationLoaded();
        this.initialized = true;
      });
  },
  mounted() {
    if (this.aboutMe) {
      this.$root.$emit('application-loaded');
    }
  },
  methods: {
    editAboutMe() {
      this.$refs.aboutMeDrawer.open();
    },
  },
};
</script>
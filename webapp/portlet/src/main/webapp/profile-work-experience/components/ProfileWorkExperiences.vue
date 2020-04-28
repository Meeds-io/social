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
  <v-app
    :class="owner && 'profileWorkExperience' || 'profileWorkExperienceOther'"
    class="white">
    <v-toolbar color="white" flat class="border-box-sizing">
      <div
        :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height-thick skeleton-border-radius'"
        class="text-header-title text-sub-title">
        {{ skeleton && '&nbsp;' || $t('profileWorkExperiences.title') }}
      </div>
      <v-spacer />
      <v-btn
        v-if="owner || skeleton"
        :disabled="skeleton"
        :class="skeleton && 'skeleton-background'"
        icon
        outlined
        small
        @click="editWorkExperiences">
        <i
          v-if="!skeleton"
          class="uiIconEdit uiIconLightBlue pb-2" />
      </v-btn>
    </v-toolbar>
    <div v-if="owner || (experiences && experiences.length)" class="px-4 pb-6 white">
      <v-timeline
        :dense="mobile"
        class="workExperienceTimeLine"
        align-top>
        <template v-if="experiences && experiences.length">
          <profile-work-experience-item
            v-for="experience in experiences"
            :key="experience.id"
            :experience="experience"
            :skeleton="skeleton" />
        </template>
        <template v-else-if="owner">
          <profile-work-experience-item empty />
        </template>
      </v-timeline>
    </div>
    <profile-work-experience-drawer
      ref="profileWorkExperiencesDrawer"
      :key="workExperiencesDrawerKey"
      :experiences="experiences"
      @refreshWorkExperiencesDrawer="initWorkExperiencesDrawer"
      @refresh="setExperiences($event)" />
  </v-app>
</template>

<script>
export default {
  props: {
    experiences: {
      type: Array,
      default: () => null,
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    skeleton: true,
    error: null,
    saving: null,
    workExperiencesDrawerKey: 0,
  }),
  computed: {
    mobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
  },
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    this.setExperiences(this.experiences);
    this.skeleton = false;
  },
  methods: {
    refresh() {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'all')
        .then(user => {
          this.setExperiences(user && user.experiences);
          return this.$nextTick();
        })
        .catch((e) => {
          console.warn('Error while retrieving user details', e); // eslint-disable-line no-console
        })
        .finally(() => {
          this.skeleton = false;
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        });
    },
    setExperiences(experiences) {
      experiences = experiences || [];
      try {
        experiences.sort((a, b) => {
          if (a.isCurrent) {
            return -1;
          }
          if (b.isCurrent) {
            return 1;
          }
          if (!a.startDate && !b.startDate ) {
            return 0;
          }
          if (!a.startDate) {
            return 1;
          }
          if (!b.startDate) {
            return -1;
          }
          return new Date(b.startDate).getTime() - new Date(a.startDate).getTime();
        });
      } catch (e) {
        console.error('Error sorting experiences', e); // eslint-disable-line no-console
      }
      this.experiences = experiences;
    },
    initWorkExperiencesDrawer() {
      this.workExperiencesDrawerKey += 1;
    },
    editWorkExperiences() {
      this.$refs.profileWorkExperiencesDrawer.open();
    },
  },
};
</script>
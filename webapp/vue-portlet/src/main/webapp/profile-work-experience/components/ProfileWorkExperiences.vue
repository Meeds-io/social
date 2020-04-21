<template>
  <v-app
    :class="owner && 'profileWorkExperience' || 'profileWorkExperienceOther'"
    class="white">
    <v-toolbar color="white" flat>
      <div class="text-header-title text-sub-title">
        {{ $t('profileWorkExperiences.title') }}
      </div>
      <v-spacer />
      <v-btn
        v-if="owner"
        icon
        outlined
        small
        @click="editWorkExperiences">
        <i class="uiIconEdit uiIconLightBlue" />
      </v-btn>
    </v-toolbar>
    <div v-if="experiences && experiences.length" class="px-4 pb-6 white">
      <v-timeline
        class="workExperienceTimeLine"
        align-top>
        <profile-work-experience-item
          v-for="experience in experiences"
          :key="experience.id"
          :experience="experience" />
      </v-timeline>
    </div>
    <profile-work-experience-drawer
      ref="profileWorkExperiencesDrawer"
      :experiences="experiences"
      @refresh="setExperiences($event)" />
  </v-app>
</template>

<script>
import * as userService from '../../common/js/UserService.js'; 

export default {
  props: {
    experiences: {
      type: Array,
      default: () => null,
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    error: null,
    saving: null,
  }),
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    this.setExperiences(this.experiences);
  },
  methods: {
    refresh() {
      return userService.getUser(eXo.env.portal.profileOwner, 'all')
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
    editWorkExperiences() {
      this.$refs.profileWorkExperiencesDrawer.open();
    },
  },
};
</script>
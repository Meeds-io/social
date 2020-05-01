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
    skeleton: true,
    error: null,
    saving: null,
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
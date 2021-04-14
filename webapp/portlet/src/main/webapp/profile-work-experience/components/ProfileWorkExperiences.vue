<template>
  <v-app
    :class="owner && 'profileWorkExperience' || 'profileWorkExperienceOther'"
    class="white">
    <v-toolbar
      color="white"
      flat
      class="border-box-sizing">
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
        <i class="uiIconEdit uiIconLightBlue pb-2"></i>
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
            :experience="experience" />
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
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    experiences: null,
    error: null,
    saving: null,
    workExperiencesDrawerKey: 0,
  }),
  computed: {
    mobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
  },
  created() {
    this.refresh();
  },
  mounted() {
    if (this.experiences) {
      this.$root.$emit('application-loaded');
    }
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
          this.$nextTick().then(() => this.$root.$emit('application-loaded'));
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
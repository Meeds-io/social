<template>
  <v-app
    v-if="displayApp"
    :class="owner && 'profileWorkExperience' || 'profileWorkExperienceOther'"
    class="white">
    <widget-wrapper :title="title">
      <template v-if="owner" #action>
        <v-btn
          icon
          outlined
          small
          @click="addWorkExperience">
          <v-icon size="18">fas fa-plus</v-icon>
        </v-btn>
        <v-btn
          v-if="hasExperiences"
          class="ms-2"
          icon
          outlined
          small
          @click="editWorkExperiences">
          <v-icon size="18">fas fa-edit</v-icon>
        </v-btn>
      </template>
      <v-timeline
        v-if="hasExperiences"
        class="workExperienceTimeLine"
        align-top
        dense>
        <profile-work-experience-item
          v-for="experience in experiences"
          :key="experience.id"
          :experience="experience" />
      </v-timeline>
      <!-- Must be v-html to preserve the href with javascript -->
      <div v-else-if="displayEmptyBlock" v-html="emptyExperiencesOwnerTitle"></div>
    </widget-wrapper> 
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
    addExperienceEventName: 'profile-add-experience',
    experiences: null,
    error: null,
    saving: null,
    workExperiencesDrawerKey: 0,
    initialized: false,
  }),
  computed: {
    hasExperiences() {
      return this.experiences?.length;
    },
    displayApp() {
      return this.owner || !this.initialized || this.hasExperiences;
    },
    title() {
      return this.owner && this.$t('profileYourWorkExperiences.title') || this.$t('profileWorkExperiences.title');
    },
    displayEmptyBlock() {
      return this.owner && !this.hasExperiences;
    },
    emptyExperiencesOwnerTitle() {
      return this.owner && this.$t('profileYourWorkExperiences.emptyTitle', {
        0: '<a id="emptyExperiencesLink" class="primary--text font-weight-bold">',
        1: '</a>',
      });
    },
  },
  created() {
    document.addEventListener('click', this.openDrawerOnLinkClick);

    this.refresh()
      .finally(() => this.$root.$applicationLoaded());
  },
  mounted() {
    if (this.experiences) {
      this.$root.$emit('application-loaded');
    }
  },
  beforeDestroy() {
    document.removeEventListener('click', this.openDrawerOnLinkClick);
  },
  methods: {
    refresh() {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'all')
        .then(user => this.setExperiences(user && user.experiences))
        .catch((e) => console.error('Error while retrieving user details', e))
        .finally(() => {
          this.$nextTick().then(() => this.$root.$emit('application-loaded'));
          this.initialized = true;
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
    openDrawerOnLinkClick(event) {
      if (this.displayEmptyBlock && event?.target?.id === 'emptyExperiencesLink') {
        this.addWorkExperience();
      }
    },
    initWorkExperiencesDrawer() {
      this.workExperiencesDrawerKey += 1;
    },
    editWorkExperiences() {
      this.$refs.profileWorkExperiencesDrawer.open();
    },
    addWorkExperience() {
      this.$refs.profileWorkExperiencesDrawer.open(true);
    },
  },
};
</script>
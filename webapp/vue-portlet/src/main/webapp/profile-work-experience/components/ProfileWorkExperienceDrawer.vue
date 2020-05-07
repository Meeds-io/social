<template>
  <exo-drawer
    ref="profileWorkExperiencesDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    class="profileWorkExperiencesDrawer"
    right>
    <template slot="title">
      {{ $t('profileWorkExperiences.title') }}
    </template>
    <template slot="titleIcons">
      <v-icon class="my-auto" @click="addNew">mdi-plus</v-icon>
    </template>
    <template slot="content">
      <v-form
        ref="profileContactForm"
        class="form-horizontal pa-0"
        flat>
        <v-card-text v-if="error" class="errorBox">
          <v-alert type="error">
            {{ error }}
          </v-alert>
        </v-card-text>
        <v-expansion-panels
          v-if="experiences && experiences.length"
          v-model="openedExperience"
          class="d-block border-box-sizing"
          flat>
          <profile-work-experience-edit-item
            v-for="(experience,i) in experiences"
            :key="i"
            :experience="experience"
            @remove="remove(experience)" />
        </v-expansion-panels>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn mr-2"
          @click="cancel">
          {{ $t('profileWorkExperiences.cancel') }}
        </v-btn>
        <v-btn
          :disabled="saving"
          :loading="saving"
          class="btn btn-primary"
          @click="save">
          {{ $t('profileWorkExperiences.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    experiences: {
      type: Object,
      default: () => null,
    },
  },
  data: () => ({
    openedExperience: null,
    saving: null,
    error: null,
  }),
  methods: {
    save() {
      this.error = null;

      if (!this.$refs.profileContactForm.validate() // Vuetify rules
          || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
        this.handleError(this.$t('profileWorkExperiences.formValidationError'));
        return;
      }

      const experiences = this.experiences.filter(experience => experience && (experience.startDate || experience.endDate || experience.position || experience.company || experience.description || experience.skills));
      for (const experience of experiences) {
        if (experience.startDate && experience.endDate && new Date(experience.startDate) > new Date(experience.endDate)) {
          this.handleError(this.$t('profileWorkExperiences.invalidEndDate'));
          return;
        }
      }

      this.$refs.profileWorkExperiencesDrawer.startLoading();
      this.saving = true;
      this.$userService.updateProfileFields(eXo.env.portal.userName, {
        experiences: experiences
      }, [
        'experiences',
      ])
        .then(this.refresh)
        .then(() => {
          this.$refs.profileWorkExperiencesDrawer.close();
        })
        .catch(this.handleError)
        .finally(() => {
          this.saving = false;
          this.$refs.profileWorkExperiencesDrawer.endLoading();
        });
    },
    handleError(error) {
      if (error) {
        this.error = String(error);
        window.setTimeout(() => {
          this.error = null;
        }, 5000);
      }
    },
    refresh() {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'all')
        .then(user => {
          this.user = user;
          this.$emit('refresh', user && user.experiences || []);
        })
        .catch((e) => {
          console.warn('Error while retrieving user details', e); // eslint-disable-line no-console
        });
    },
    addNew() {
      this.experiences.unshift({});
      this.openedExperience = 0;
    },
    remove(experience) {
      const index = this.experiences.findIndex(temp => temp === experience);
      if (this.openedExperience === index) {
        this.openedExperience = null;
      }
      this.experiences.splice(index, 1);
    },
    cancel() {
      this.$refs.profileWorkExperiencesDrawer.close();
    },
    open() {
      const experiences = this.experiences ? this.experiences.slice() : [{}];
      this.experiences = experiences.map(experience => Object.assign({}, experience));
      this.$refs.profileWorkExperiencesDrawer.open();
    },
  },
};
</script>
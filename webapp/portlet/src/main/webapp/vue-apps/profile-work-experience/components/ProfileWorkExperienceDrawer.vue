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
        <v-expansion-panels
          v-if="experiences && experiencesToEdit.length"
          v-model="openedExperiences"
          class="d-block border-box-sizing"
          multiple
          flat>
          <profile-work-experience-edit-item
            v-for="(experience,i) in experiencesToEdit"
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
          class="btn me-2"
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
      type: Array,
      default: () => null,
    },
  },
  data: () => ({
    hasCurrentExperience: false,
    experiencesToEdit: [],
    openedExperiences: [],
    saving: null,
    error: null,
  }),
  watch: {
    error() {
      if (!this.error) {
        this.$root.$emit('close-alert-message');
      }
    },
  },
  methods: {
    save() {
      this.error = null;

      const experiences = this.experiencesToEdit.filter(experience => experience && (experience.startDate || experience.endDate || experience.position || experience.company || experience.description || experience.skills));
      for (const experience of experiences) {
        if (!experience.endDate && this.hasCurrentExperience && !experience.id) {
          this.handleError(this.$t('profileWorkExperiences.invalidStillInPosition'));
          return;
        }

        experience.company = experience.company && experience.company.trim();
        if (!experience.company || experience.company.length > 100 || experience.company.length < 3) {
          this.handleError(this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileWorkExperiences.company'),
            1: 3,
            2: 250,
          }));
          return;
        }

        experience.position = experience.position && experience.position.trim();
        if (!experience.position || experience.position.length > 100 || experience.position.length < 3) {
          this.handleError(this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileWorkExperiences.jobTitle'),
            1: 3,
            2: 100,
          }));
          return;
        }

        if (experience.description && experience.description.length > 1500) {
          this.handleError(this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileWorkExperiences.jobDetails'),
            1: 0,
            2: 1500,
          }));
          return;
        }

        if (experience.endDate && new Date(experience.endDate) > new Date()) {
          this.handleError(this.$t('profileWorkExperiences.beforeTodayEndDate'));
          return;
        }

        if (experience.startDate && new Date(experience.startDate) > new Date()) {
          this.handleError(this.$t('profileWorkExperiences.beforeTodayStartDate'));
          return;
        }

        if (experience.startDate && experience.endDate && new Date(experience.startDate) >= new Date(experience.endDate)) {
          this.handleError(this.$t('profileWorkExperiences.invalidEndDate'));
          return;
        }
      }

      if (this.error) {
        return;
      }

      if (!this.$refs.profileContactForm.validate() // Vuetify rules
        || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
        this.handleError(this.$t('profileWorkExperiences.formValidationError'));
        return;
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
          this.$emit('refreshWorkExperiencesDrawer');
        });
    },
    handleError(error) {
      if (error) {
        this.error = String(error);
        this.$root.$emit('alert-message', this.error, 'error');
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
      this.experiencesToEdit.unshift({});
      this.openedExperiences.push(this.openedExperiences.length);
    },
    remove(experience) {
      const index = this.experiencesToEdit.findIndex(temp => temp === experience);
      if (this.openedExperiences.includes(index)) {
        this.openedExperiences.splice(this.openedExperiences.indexOf(index), 1);
      }
      this.experiencesToEdit.splice(index, 1);
    },
    cancel() {
      this.$refs.profileWorkExperiencesDrawer.close();
    },
    open(initiateNew) {
      const experiences = this.experiences && this.experiences.slice() || [];
      if (initiateNew) {
        experiences.unshift({});
      }
      this.experiencesToEdit = experiences.map(experience => Object.assign({}, experience));
      this.hasCurrentExperience = this.experiencesToEdit.find(experience => experience.isCurrent);
      this.openedExperiences = [];
      this.$nextTick().then(() => {
        this.$refs.profileWorkExperiencesDrawer.open();
        if (initiateNew) {
          this.$nextTick().then(() => this.openedExperiences = [0]);
        }
      });
    },
  },
};
</script>

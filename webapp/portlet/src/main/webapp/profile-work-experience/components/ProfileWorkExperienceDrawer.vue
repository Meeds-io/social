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
          v-model="openedExperiences"
          class="d-block border-box-sizing"
          multiple
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
    hasCurrentExperience: false,
    openedExperiences: [],
    saving: null,
    error: null,
  }),
  watch: {
    openedExperiences() {
      console.log('*** openedExperiences: ', this.openedExperiences);
    }
  },
  created() {
    if (this.experiences) {
      this.hasCurrentExperience = this.experiences.some(experience => experience.isCurrent);
    }
  },
  methods: {
    save() {
      this.error = null;

      const experiences = this.experiences.filter(experience => experience && (experience.startDate || experience.endDate || experience.position || experience.company || experience.description || experience.skills));
      console.log('Experiences: ', experiences);
      for (const experience of experiences) {
        const itemIndex = this.experiences.findIndex(exp => exp.id === experience.id);
        console.log('############################################');
        console.log('this.experiences: ', this.experiences);
        console.log('# * # itemIndex : ', itemIndex);
        console.log('### $refs: ', this.$refs);
        console.log('### profileContactForm: ', this.$refs.profileContactForm);
        this.openedExperiences = [];
        this.openedExperiences.push(itemIndex);
        
        if (!experience.endDate && this.hasCurrentExperience && !experience.id) {
          this.handleError(this.$t('profileWorkExperiences.invalidStillInPosition'));
          return;
        }
        if (experience.startDate && experience.endDate && new Date(experience.startDate) >= new Date(experience.endDate)) {
          this.handleError(this.$t('profileWorkExperiences.invalidEndDate'));
          return;
        }
        
        if (experience.company && experience.position) {
          console.log('experience.company: ', experience.company);
          console.log('experience.company.field: ', this.$refs.profileContactForm.$el[1]);
          if (this.$refs.profileContactForm.$el[1]) {
            experience.company = experience.company.trim();
            if (experience.company !== null && experience.company.length > 250 || experience.company !== null && experience.company.length < 3) {
              this.$refs.profileContactForm.$el[1].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
                0: this.$t('profileWorkExperiences.company'),
                1: 3,
                2: 250,
              }));
            } else if (experience.company) {
              this.$refs.profileContactForm.$el[1].setCustomValidity('');
            }
          }

          console.log('experience.position: ', experience.position);
          console.log('experience.position.field: ', this.$refs.profileContactForm.$el[2]);
          if (this.$refs.profileContactForm.$el[2]) {
            experience.position = experience.position.trim();
            if (experience.position !== null && experience.position.length > 100 || experience.position !== null && experience.position.length < 3) {
              this.$refs.profileContactForm.$el[2].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
                0: this.$t('profileWorkExperiences.jobTitle'),
                1: 3,
                2: 100,
              }));
            } else if (experience.position) {
              this.$refs.profileContactForm.$el[2].setCustomValidity('');
            }
          }

          if (this.$refs.profileContactForm.$el[3]) {
            if (experience.description && experience.description.length > 1500) {
              this.$refs.profileContactForm.$el[3].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
                0: this.$t('profileWorkExperiences.jobDetails'),
                1: 0,
                2: 1500,
              }));
            } else if (experience.description) {
              this.$refs.profileContactForm.$el[3].setCustomValidity('');
            }
          }

          if (experience.endDate && new Date(experience.endDate) > new Date()) {
            this.handleError(this.$t('profileWorkExperiences.beforeTodayEndDate'));
            return;
          }

          if (experience.startDate && new Date(experience.startDate) > new Date()) {
            this.handleError(this.$t('profileWorkExperiences.beforeTodayStartDate'));
            return;
          }
        } else {
          console.log('*experience.company: ', experience.company ? 'true': 'false');
          console.log('*experience.position: ', experience.position ? 'true': 'false');
          console.log('*EL: ', this.$refs.profileContactForm.$el);
          if (!experience.company) {
            this.$refs.profileContactForm.$el[1].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
              0: this.$t('profileWorkExperiences.company'),
              1: 3,
              2: 250,
            }));
          } else {
            this.$refs.profileContactForm.$el[1].setCustomValidity('');
          }

          if (!experience.position) {
            this.$refs.profileContactForm.$el[2].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
              0: this.$t('profileWorkExperiences.jobTitle'),
              1: 3,
              2: 100,
            }));
          } else {
            this.$refs.profileContactForm.$el[2].setCustomValidity('');
          }
        }
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
      this.openedExperiences.push(0);
    },
    remove(experience) {
      const index = this.experiences.findIndex(temp => temp === experience);
      if (this.openedExperiences.includes(index)) {
        this.openedExperiences.splice(this.openedExperiences.findIndex(index), 1);
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

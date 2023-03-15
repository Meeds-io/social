<template>
  <v-expansion-panel
    v-if="experience"
    class="profileWorkExperiencesEditItem border-color border-radius my-3 mx-2">
    <v-expansion-panel-header>
      <div v-if="experience.id" class="truncate-text">
        <div>{{ position }}</div>
        <div class="text-sub-title">{{ company }}</div>
      </div>
    </v-expansion-panel-header>
    <v-expansion-panel-content>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pt-0 pb-2">
        {{ $t('profileWorkExperiences.company') }} *
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <input
          v-model="experience.company"
          name="company"
          type="text"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="250"
          autofocus="autofocus"
          required>
      </v-card-text>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
        {{ $t('profileWorkExperiences.jobTitle') }} *
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <input
          v-model="experience.position"
          name="position"
          type="text"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="250"
          required>
      </v-card-text>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
        {{ $t('profileWorkExperiences.jobDetails') }}
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <textarea
          v-model="experience.description"
          name="description"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="1500"></textarea>
      </v-card-text>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
        {{ $t('profileWorkExperiences.usedSkills') }}
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <input
          v-model="experience.skills"
          name="skills"
          type="text"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="2000">
      </v-card-text>
      <v-card-text class="d-flex text-color pb-2">
        <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold me-3">
          {{ $t('profileWorkExperiences.startDate') }}
        </div>
        <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold px-3">
          {{ $t('profileWorkExperiences.endDate') }}
        </div>
      </v-card-text>
      <v-card-text class="d-flex py-0 profileWorkExperiencesDates">
        <div class="align-start flex-grow-0 text-no-wrap text-left font-weight-bold half-width me-3">
          <date-picker
            v-model="experience.startDate"
            :left="$vuetify.rtl"
            class="ignore-vuetify-classes"
            top
            return-iso
            required />
        </div>
        <div class="align-end flex-grow-0 text-no-wrap text-left font-weight-bold half-width">
          <date-picker
            v-model="endDate"
            :disabled="isCurrent"
            :min-value="experience.startDate"
            :left="!$vuetify.rtl"
            class="ignore-vuetify-classes"
            top
            return-iso
            required />
        </div>
      </v-card-text>
      <v-card-text class="d-flex">
        <v-switch
          v-model="isCurrent"
          :label="$t('profileWorkExperiences.stillInPosition')"
          class="" />
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <v-btn
          color="error"
          class="pa-0"
          outlined
          @click="$emit('remove')">
          <i class="uiIconTrash pb-1 pe-2"></i>
          {{ $t('profileWorkExperiences.removeExperience') }}
        </v-btn>
      </v-card-text>
    </v-expansion-panel-content>
  </v-expansion-panel>
</template>

<script>
export default {
  props: {
    experience: {
      type: Object,
      default: () => null,
    },
  },
  data: () => ({
    isCurrent: null,
    endDate: null,
    company: null,
    position: null,
  }),
  watch: {
    experience() {
      this.copyAtributes();
    },
    endDate() {
      this.experience.endDate = this.endDate;
    },
    isCurrent() {
      this.experience.isCurrent = this.isCurrent;
      if (this.isCurrent) {
        this.endDate = '';
      }
    },
  },
  created() {
    this.copyAtributes();
  },
  methods: {
    copyAtributes() {
      this.company = this.experience && this.experience.company || '';
      this.position = this.experience && this.experience.position || '';
      this.isCurrent = this.experience && this.experience.isCurrent || false;
      if (this.isCurrent) {
        this.endDate = '';
      } else {
        this.endDate = this.experience && this.experience.endDate || '';
      }
    },
  },
};
</script>

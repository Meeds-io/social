<template>
  <v-expansion-panel
    v-if="experience"
    class="profileWorkExperiencesEditItem border-color border-radius my-3 mx-2">
    <v-expansion-panel-header>
      <div
        v-if="experience.id"
        class="truncate-text">
        <div>{{ position }}</div>
        <div class="text-subtitle">
          {{ company }}
        </div>
      </div>
    </v-expansion-panel-header>
    <v-expansion-panel-content>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap pt-0 pb-2">
        {{ $t('profileWorkExperiences.company') }} *
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <input
          v-model="experience.company"
          autofocus="autofocus"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="250"
          name="company"
          required
          type="text">
      </v-card-text>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap pb-2">
        {{ $t('profileWorkExperiences.jobTitle') }} *
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <input
          v-model="experience.position"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="250"
          name="position"
          required
          type="text">
      </v-card-text>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap pb-2">
        {{ $t('profileWorkExperiences.jobDetails') }}
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <textarea
          v-model="experience.description"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="1500"
          name="description"></textarea>
      </v-card-text>
      <v-card-text class="d-flex flex-grow-1 text-no-wrap pb-2">
        {{ $t('profileWorkExperiences.usedSkills') }}
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <input
          v-model="experience.skills"
          class="ignore-vuetify-classes flex-grow-1"
          maxlength="2000"
          name="skills"
          type="text">
      </v-card-text>
      <v-card-text class="d-flex text-color pb-2">
        <div class="align-start flex-grow-1 text-no-wrap me-3">
          {{ $t('profileWorkExperiences.startDate') }}
        </div>
        <div
          v-if="!isCurrent"
          class="align-start flex-grow-1 text-no-wrap px-3">
          {{ $t('profileWorkExperiences.endDate') }}
        </div>
      </v-card-text>
      <v-card-text class="d-flex flex-row full-width py-0 profileWorkExperiencesDates">
        <div class="align-start flex-grow-0 col-6 pa-0 text-no-wrap half-width me-3">
          <date-picker
            v-model="experience.startDate"
            class="ignore-vuetify-classes"
            :left="$vuetify.rtl"
            required
            return-iso
            top />
        </div>
        <div
          v-if="!isCurrent"
          class="align-end flex-grow-0 col-6 pa-0 text-no-wrap half-width">
          <date-picker
            v-model="endDate"
            class="ignore-vuetify-classes"
            :disabled="isCurrent"
            :left="!$vuetify.rtl"
            :min-value="experience.startDate"
            required
            return-iso
            top />
        </div>
      </v-card-text>
      <v-card-text class="d-flex">
        <v-switch
          v-model="isCurrent"
          class=""
          :label="$t('profileWorkExperiences.stillInPosition')" />
      </v-card-text>
      <v-card-text class="d-flex py-0">
        <v-btn
          class="pa-0"
          color="error"
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
      experience () {
        this.copyAtributes();
      },
      endDate () {
        this.experience.endDate = this.endDate;
      },
      isCurrent () {
        this.experience.isCurrent = this.isCurrent;
        if (this.isCurrent) {
          this.endDate = '';
        }
      },
    },
    created () {
      this.copyAtributes();
    },
    methods: {
      copyAtributes () {
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

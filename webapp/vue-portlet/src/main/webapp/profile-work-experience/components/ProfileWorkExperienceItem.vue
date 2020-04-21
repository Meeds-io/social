<template>
  <v-timeline-item
    class="workExperienceTimeLineItem"
    color="tertiary"
    right>
    <div slot="opposite" class="workExperienceTimeLineItemTime">
      {{ displayedDate }}
    </div>
    <v-card class="elevation-2">
      <v-card-text class="pb-3">
        <div
          class="text-color"
          v-text="experience.position">
        </div>
        <div
          class="text-sub-title"
          v-text="experience.company">
        </div>
      </v-card-text>
      <v-card-text class="pt-0">
        <h6
          class="paragraph text-color font-weight-light pb-1 mt-0"
          v-text="experience.description">
        </h6>
        <div class="text-color font-weight-bold">
          {{ $t('profileWorkExperiences.appliedSkills') }} : {{ experience.skills }}
        </div>
      </v-card-text>
    </v-card>
  </v-timeline-item>
</template>

<script>
import {formatDateObjectToDisplay} from '../../common/js/DateUtil.js';

export default {
  props: {
    experience: {
      type: Object,
      default: () => null,
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    },
  }),
  computed: {
    displayedDate() {
      if (this.experience.isCurrent && this.experience.startDate) {
        const startDate = formatDateObjectToDisplay(new Date(this.experience.startDate), this.dateFormat);
        return this.$t('profileWorkExperiences.since', {
          0: startDate,
        });
      } else {
        const startDate = formatDateObjectToDisplay(new Date(this.experience.startDate), this.dateFormat);
        const endDate = formatDateObjectToDisplay(new Date(this.experience.endDate), this.dateFormat);
        return this.$t('profileWorkExperiences.fromTo', {
          0: startDate,
          1: endDate,
        });
      }
    }
  },
};
</script>
<template>
  <v-timeline-item
    color="tertiary"
    class="workExperienceTimeLineItem">
    <v-row class="ma-0">
      <v-col
        cols="12"
        sm="3"
        class="px-0">
        <div class="text-color font-weight-bold">
          {{ experience.position }}
        </div>
        <div>
          {{ experience.company }}
        </div>
        <div v-if="displayedFromDate" class="workExperienceTimeLineItemTime text-sub-title">
          {{ displayedFromDate }}
        </div>
        <div v-if="displayedToDate" class="workExperienceTimeLineItemTime text-sub-title">
          {{ displayedToDate }}
        </div>
      </v-col>
      <v-col class="px-0 px-sm-3">
        <v-card flat tile>
          <v-card-text class="pa-0">
            <div
              v-autolinker="experience.description"
              class="text-color mb-4">
            </div>
            <template v-if="experience.skills">
              <div class="text-color font-weight-bold">
                {{ $t('profileWorkExperiences.appliedSkills') }} :
              </div>
              <div class="text-color">
                {{ experience.skills }}
              </div>
            </template>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-timeline-item>
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
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    },
  }),
  computed: {
    displayedFromDate() {
      if (!this.experience.startDate) {
        return null;
      }
      if (this.experience.isCurrent) {
        const startDateObject = this.$dateUtil.getDateObjectFromString(this.experience.startDate,true);
        const startDate = this.$dateUtil.formatDateObjectToDisplay(startDateObject, this.dateFormat);
        return this.$t('profileWorkExperiences.since', {
          0: startDate,
        });
      } else {
        const startDateObject = this.$dateUtil.getDateObjectFromString(this.experience.startDate,true);
        const startDate = this.$dateUtil.formatDateObjectToDisplay(startDateObject, this.dateFormat);
        return this.$t('profileWorkExperiences.from', {
          0: startDate,
        });
      }
    },
    displayedToDate() {
      if (!this.experience.endDate || this.experience.isCurrent) {
        return null;
      }
      const endDateObject = this.$dateUtil.getDateObjectFromString(this.experience.endDate,true);
      const endDate = this.$dateUtil.formatDateObjectToDisplay(endDateObject, this.dateFormat);
      return this.$t('profileWorkExperiences.to', {
        0: endDate,
      });
    },
  },
};
</script>

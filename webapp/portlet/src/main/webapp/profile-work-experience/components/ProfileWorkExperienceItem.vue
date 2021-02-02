<template>
  <v-timeline-item
    :class="mobile && 'caption' || ''"
    :small="mobile"
    color="tertiary"
    class="workExperienceTimeLineItem"
    right>
    <div
      v-if="!mobile"
      slot="opposite"
      class="workExperienceTimeLineItemTime">
      {{ displayedDate }}
    </div>
    <v-card>
      <v-card-text v-if="experience" class="pb-3">
        <div
          v-if="mobile"
          class="text-color font-weight-bold mb-2"
          v-text="displayedDate">
        </div>
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
        <template v-if="empty">
          <div class="text-color pt-2">
            {{ $t('profileWorkExperiences.emptyExperienceDescription') }}
          </div>
        </template>
        <template v-else>
          <h6
            v-autolinker="experience.description"
            class="paragraph text-color font-weight-light pb-1 mt-0">
          </h6>
          <div class="text-color font-weight-bold">
            {{ $t('profileWorkExperiences.appliedSkills') }} : {{ experience.skills }}
          </div>
        </template>
      </v-card-text>
    </v-card>
  </v-timeline-item>
</template>

<script>
export default {
  props: {
    experience: {
      type: Object,
      default: () => null,
    },
    empty: {
      type: Boolean,
      default: false,
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
    mobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    displayedDate() {
      if (this.empty) {
        return this.$t('profileWorkExperiences.date');
      } else if (this.experience.isCurrent && this.experience.startDate) {
        const startDate = this.$dateUtil.formatDateObjectToDisplay(new Date(this.experience.startDate), this.dateFormat);
        return this.$t('profileWorkExperiences.since', {
          0: startDate,
        });
      } else {
        const startDate = this.$dateUtil.formatDateObjectToDisplay(new Date(this.experience.startDate), this.dateFormat);
        const endDate = this.$dateUtil.formatDateObjectToDisplay(new Date(this.experience.endDate), this.dateFormat);
        return this.$t('profileWorkExperiences.fromTo', {
          0: startDate,
          1: endDate,
        });
      }
    }
  },
};
</script>
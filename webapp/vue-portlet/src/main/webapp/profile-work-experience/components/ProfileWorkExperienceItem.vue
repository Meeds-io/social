<template>
  <v-timeline-item
    :class="mobile && 'caption' || ''"
    :color="skeleton && 'skeleton-background' || 'tertiary'"
    :small="mobile"
    class="workExperienceTimeLineItem"
    right>
    <div
      v-if="!mobile"
      slot="opposite"
      :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius'"
      class="workExperienceTimeLineItemTime">
      {{ skeleton && '&nbsp;' || displayedDate }}
    </div>
    <v-card :class="skeleton && 'elevation-0 skeleton-border'">
      <v-card-text v-if="experience" class="pb-3">
        <div
          v-if="mobile"
          :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius mb-3'"
          class="text-color font-weight-bold mb-2"
          v-text="displayedDate">
        </div>
        <div
          :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius mb-3'"
          class="text-color"
          v-text="experience.position">
        </div>
        <div
          :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius mb-3'"
          class="text-sub-title"
          v-text="experience.company">
        </div>
      </v-card-text>
      <v-card-text class="pt-0">
        <template v-if="skeleton">
          <div :class="skeleton && 'skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-3'">
            &nbsp;
          </div>
          <div :class="skeleton && 'skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-5'">
            &nbsp;
          </div>
          <div :class="skeleton && 'skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-3'">
            &nbsp;
          </div>
        </template>
        <template v-else-if="empty">
          <div class="text-color pt-2">
            {{ $t('profileWorkExperiences.emptyExperienceDescription') }}
          </div>
        </template>
        <template v-else>
          <h6
            class="paragraph text-color font-weight-light pb-1 mt-0"
            v-text="experience.description">
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
import {formatDateObjectToDisplay} from '../../common/js/DateUtil.js';

export default {
  props: {
    experience: {
      type: Object,
      default: () => null,
    },
    skeleton: {
      type: Boolean,
      default: false,
    },
    empty: {
      type: Boolean,
      default: true,
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
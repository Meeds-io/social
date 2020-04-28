<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
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
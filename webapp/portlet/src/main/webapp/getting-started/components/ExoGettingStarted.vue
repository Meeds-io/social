<template>
  <v-app>
    <v-flex 
      d-flex 
      xs12 
      sm12>
      <v-layout 
        row 
        wrap 
        mx-0>
        <v-flex 
          d-flex
          xs12>
          <v-card
            flat
            class="flex">
            <v-card-title class="getting-started-title subtitle-1 text-uppercase pb-0">
              <span>
                {{ $t('locale.portlet.gettingStarted.title') }}
              </span>
            </v-card-title>
            <v-list dense class="getting-started-list">
              <v-list-item v-for="(step, index) in gettingStratedSteps" :key="index" class="getting-started-list-item">
                <v-list-item-icon class="mr-3 steps-icon">
                  <i v-if="step.status" class="UICheckIcon white--text" />
                  <span v-else class="step-number font-weight-bold text-center white--text" >
                    <span>{{ index + 1 }}</span>
                  </span>
                </v-list-item-icon>
                <v-list-item-content :class="{ inactiveStep: !step.status }" class="pb-3">
                  <v-list-item-title class="body-2">
                    <span>
                      {{ $t(`locale.portlet.gettingStarted.step.${step.name}`) }}
                    </span>
                  </v-list-item-title>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card>
        </v-flex>
      </v-layout>
    </v-flex>
  </v-app>
</template>
<script>
import * as gettingStartedService from '../gettingStartedService.js';
export default {
  data () {
    return {
      gettingStratedSteps: [],
    };
  },
  created() {
    this.initGettingStarted();
  },
  methods : {
    initGettingStarted() {
      gettingStartedService.getGettingStartedSteps()
        .then(data => {
          this.gettingStratedSteps = data;
          return this.$nextTick();
        })
        .then(() => this.$root.$emit('application-loaded'));
    }
  }
};
</script>
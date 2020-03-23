<template>
  <v-app id="GettingStartedPortlet">
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
            <v-card-title class="getting-started-title subtitle-1 text-uppercase pb-0"> {{ $t('social.portlet.gettingStarted.title') }}</v-card-title>
            <v-list dense class="getting-started-list">
              <v-list-item v-for="(step, index) in gettingStratedSteps" :key="index" class="getting-started-list-item">
                <v-list-item-icon class="mr-3 steps-icon">
                  <i v-if="step.status" class="UICheckIcon white--text"></i>
                  <span v-else class="step-number font-weight-bold text-center white--text">{{ index+1 }}</span>
                </v-list-item-icon>
                <v-list-item-content :class="{ inactiveStep: !step.status }">
                  <v-list-item-title class="body-2">{{ step.name }}</v-list-item-title>
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
export default {
  data () {
    return {
      gettingStratedSteps: [
        {
          name: 'Upload your profile picture',
          status: true
        },
        {
          name: 'Connect with collegues',
          status: false
        },
        {
          name: 'Join a space',
          status: false
        },
        {
          name: 'Post an activity',
          status: true
        }
      ]
    };
  }
};
</script>
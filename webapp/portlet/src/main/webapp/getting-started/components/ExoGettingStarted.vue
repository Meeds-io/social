<template>
  <div v-if="showGettingStrated">
    <v-app>
      <v-flex d-flex xs12 sm12 class="hiddenable-widget">
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
                <span style="width: 95%">
                  {{ $t('locale.portlet.gettingStarted.title') }}
                </span>
                <a v-if="showButtonClose" href="#" @click="hideGettingStarted">x</a>
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
  </div>
</template>
<script>
import * as gettingStartedService from '../gettingStartedService.js';
export default {
  data () {
    return {
      gettingStratedSteps: [],
      showButtonClose: true,
      showGettingStrated: null
    };
  },
  watch: {
    gettingStratedSteps(){
      for (let i =0 ;i<this.gettingStratedSteps.length;i++){
        if (!this.gettingStratedSteps[i].status){
          this.showButtonClose = false;
        }
      }
    }
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
      for (let i =0 ;i<this.gettingStratedSteps.length;i++){
        if (!this.gettingStratedSteps.status){
          this.showButtonClose = false;
        }
      }
      if (localStorage.getItem('gettingStarted') && this.showButtonClose){
        const data = JSON.parse(localStorage.getItem('gettingStarted') || {});
        if (data.user === eXo.env.portal.userName && data.isGettingStartedEnabled === true){
          this.showGettingStrated = false;
          return;
        }
      }else if (this.showButtonClose){
        gettingStartedService.getGettingStartedSettings().then((resp) =>{
          if (resp && resp.value){
            this.showGettingStrated = false;
            return;
          }else {
            this.showGettingStrated = true;
          }
        });
      }
    },
    hideGettingStarted(){
      gettingStartedService.saveGettingStartedSettings().then((response) => {
        if (response){
          const gettingStarted = {
            user : eXo.env.portal.userName,
            isGettingStartedEnabled: true
          };
          localStorage.setItem('gettingStarted',JSON.stringify(gettingStarted));
          this.showGettingStrated = false;
        }
      });
    }
  }
};
</script>
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
                <span class="title">
                  {{ $t('locale.portlet.gettingStarted.title') }}
                  <a v-show="showButtonClose" :title="$t('locale.portlet.gettingStarted.button.close')" class="btClose" href="#" rel="tooltip" data-placement="bottom" @click="hideGettingStarted">x</a>
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
  </div>
</template>
<script>
import * as gettingStartedService from '../gettingStartedService.js';
export default {
  data () {
    return {
      gettingStratedSteps: [],
      showGettingStrated: false
    };
  },computed: {
    showButtonClose : function () {
      return this.gettingStratedSteps.some(step => step.status === false) ? false : true;
    }
  },
  created() {
    this.initGettingStarted();
  },
  methods : {
    initGettingStarted() {
      if (localStorage.getItem('gettingStarted') && this.showButtonClose){
        const data = JSON.parse(localStorage.getItem('gettingStarted') || {});
        if (data.user === eXo.env.portal.userName && data.gettingStartedStatus === true){
          this.clearCache();
          this.showGettingStrated = false;
          return;
        }else {
          this.showGettingStrated = true;
          this.getGettingStartedSteps();
        }
      }else if (this.showButtonClose){
        gettingStartedService.getGettingStartedSettings().then((resp) =>{
          if (resp && resp.value){
            this.clearCache();
            this.showGettingStrated = false;
            return;
          }else {
            this.showGettingStrated = true;
            this.getGettingStartedSteps();
          }
        });
      }else {
        this.showGettingStrated = true;
        this.getGettingStartedSteps();
      }
    },
    getGettingStartedSteps(){
      gettingStartedService.getGettingStartedSteps()
        .then(data => {
          this.gettingStratedSteps = data;
          return this.$nextTick();
        })
        .then(() => {
          this.$root.$emit('application-loaded');
        });
    },
    hideGettingStarted(){
      gettingStartedService.saveGettingStartedSettings().then((response) => {
        if (response){
          this.clearCache();
          const gettingStarted = {
            user : eXo.env.portal.userName,
            gettingStartedStatus: true
          };
          localStorage.setItem('gettingStarted',JSON.stringify(gettingStarted));
          this.showGettingStrated = false;
        }
      });
    },
    clearCache(){
      caches.open('portal-pwa-resources-dom').then(function(cache) {
        cache.delete('/dom-cache?id=GettingStartedPortlet').then(function() {
          console.debug('cache deleted');
        });
      });
    }
  }
};
</script>

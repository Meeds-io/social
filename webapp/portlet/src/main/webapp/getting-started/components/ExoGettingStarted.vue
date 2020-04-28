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
            <v-card-title class="getting-started-title subtitle-1 text-uppercase pb-0">
              <span :class="firstLoadingSteps && 'skeleton-background skeleton-text skeleton-header skeleton-border-radius'">
                {{ $t('locale.portlet.gettingStarted.title') }}
              </span>
            </v-card-title>
            <v-list dense class="getting-started-list">
              <v-list-item v-for="(step, index) in gettingStratedSteps" :key="index" class="getting-started-list-item">
                <v-list-item-icon :class="firstLoadingSteps && 'skeleton-text'" class="mr-3 steps-icon">
                  <i v-if="step.status" :class="firstLoadingSteps && 'skeleton-background skeleton-text'" class="UICheckIcon white--text"></i>
                  <span v-else :class="firstLoadingSteps && 'skeleton-background'" class="step-number font-weight-bold text-center white--text" >
                    <span :class="firstLoadingSteps && 'skeleton-text'" >{{ index+1 }}</span>
                  </span>
                </v-list-item-icon>
                <v-list-item-content :class="{ inactiveStep: !step.status }" class="pb-3">
                  <v-list-item-title class="body-2">
                    <span :class="firstLoadingSteps && 'skeleton-background skeleton-text skeleton-list-item-title skeleton-border-radius'">
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
      firstLoadingSteps: true
    };
  },
  created() {
    this.initGettingStarted();
  },
  methods : {
    initGettingStarted() {
      gettingStartedService.getGettingStartedSteps().then(data => {
        this.gettingStratedSteps = data;
        if(this.firstLoadingSteps) {
          this.firstLoadingSteps = false;
        }
      });
    }
  }
};
</script>
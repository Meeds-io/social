<template>
  <v-app id="multifactorAuthentication">
    <div v-if="!isManage2faPage" class="d-flex">
      <v-card class="my-8 mr-4 ml-4 border-radius firstBlock" flat>
        <v-list>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title text-color font-weight-bold subtitle-1 ml-2 infoTextStyle">
                {{ $t('authentication.multifactor.title') }}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item>
            <v-list-item-content>
              <div class="d-flex">
                <v-list-item-title v-if="!isMultifacorAuthenticationEnabled" class="mb-1 text-color font-weight-medium textSize  ml-2 caption infoTextStyle">
                  {{ $t('authentication.multifactor.header') }}
                </v-list-item-title>
                <v-list-item-title v-if="isMultifacorAuthenticationEnabled" class="mb-1 text-color font-weight-medium textSize caption infoTextStyle">
                  <div>
                    {{ $t('authentication.multifactor.second.block.description') }}
                  </div>
                </v-list-item-title>
                <v-list-item-action class="mr-4">
                  <label class="switch">
                    <input
                      v-model="isMultifacorAuthenticationEnabled"
                      type="checkbox"
                      @click="switchAuthenticationStatus">
                    <div class="slider round"><span class="absolute-activate">{{ $t(`authentication.multifactor.button.yes`) }}</span></div>
                    <span class="absolute-deactivated">{{ $t(`authentication.multifactor.button.no`) }}</span>
                  </label>
                </v-list-item-action>
              </div>
              <v-list-item-subtitle v-if="isMultifacorAuthenticationEnabled" class="text-sub-title infoTextStyle textLigneHeight font-italic textSize caption">
                <div>
                  <v-icon
                    class="me-1"
                    color="grey"
                    size="24">
                    mdi-information-outline
                  </v-icon>
                  {{ $t('authentication.multifactor.label') }}
                </div>
              </v-list-item-subtitle>
              <v-row v-if="isMultifacorAuthenticationEnabled">
                <v-col cols="3">
                  <v-select
                    ref="selectItem"
                    v-model="select"
                    :items="items"
                    class="authenticationInput"
                    outlined
                    dense />
                </v-col>
              </v-row>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <div v-if="isMultifacorAuthenticationEnabled" class="mt-6">
          <v-list>
            <div class="d-flex ml-6">
              <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle">
                {{ $t('authentication.multifactor.protected.resources') }}
              </v-list-item-title>
              <template>
                <v-btn
                  class="mr-3"
                  small
                  icon
                  @click="$root.$emit('isManage2faPage', isManage2faPage)">
                  <v-icon size="24" class="text-sub-title">
                    {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
                  </v-icon>
                </v-btn>
              </template>
            </div>
            <v-list-item-subtitle class="text-sub-title text-justify font-italic textSize caption ml-6 infoTextStyle textLigneHeight">
              <div>
                {{ $t('authentication.multifactor.protected.resources.label') }}
              </div>
            </v-list-item-subtitle>
          </v-list>
        </div>
        <div v-if="isMultifacorAuthenticationEnabled" class="mt-6">
          <v-list>
            <div class="d-flex ml-6">
              <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle">
                {{ $t('authentication.multifactor.protected.group') }}
              </v-list-item-title>
              <template>
                <v-btn
                  class="mr-3"
                  icon
                  outlined
                  small
                  @click="$root.$emit('protectedGroupsUsers', protectedGroupsUsers)">
                  <i class="uiIconEdit uiIconLightBlue pb-2"></i>
                </v-btn>
              </template>
            </div>
            <v-list-item-subtitle class="text-sub-title text-justify font-italic textSize caption ml-6 infoTextStyle textLigneHeight">
              <div>
                {{ $t('authentication.multifactor.protected.group.label') }}
              </div>
            </v-list-item-subtitle>
            <div class="ml-5">
              <v-chip-group
                active-class="primary--text"
                column>
                <v-row no-gutters>
                  <v-chip
                    v-for="group in selectedGroups"
                    :key="group"
                    outlined
                    class="my-1">
                    {{ group }}
                  </v-chip>
                </v-row>
              </v-chip-group>
            </div>
          </v-list>
        </div>
      </v-card>

      <div class="my-8 mr-4 border-radius secondBlock" flat>
        <template>
          <v-expansion-panels v-model="panel1" multiple>
            <v-expansion-panel>
              <v-expansion-panel-header expand-icon="mdi-menu-down" class="panelBlock">
                <div class="d-flex">
                  <v-icon
                    class="me-1"
                    color="grey"
                    size="24">
                    mdi-information-outline
                  </v-icon>
                  <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle">
                    {{ $t('authentication.multifactor.second.block.label') }}
                  </v-list-item-title>
                </div>
              </v-expansion-panel-header>
              <v-expansion-panel-content>
                <v-list-item-subtitle class="text-sub-title text-left font-italic textSize caption infoTextStyle textLigneHeight">
                  <div>
                    {{ $t('authentication.multifactor.second.block.description') }}
                  </div>
                </v-list-item-subtitle>
              </v-expansion-panel-content>
            </v-expansion-panel>
          </v-expansion-panels>
        </template>
        <template v-if="isMultifacorAuthenticationEnabled">
          <v-expansion-panels
            v-model="panel"
            class="mt-3"
            multiple>
            <v-expansion-panel>
              <v-expansion-panel-header expand-icon="mdi-menu-down" class="panelBlock">
                <div class="d-flex">
                  <v-list-item-content v-if="isOTP">
                    <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle ml-0">
                      <v-icon
                        color="grey"
                        size="24">
                        mdi-information-outline
                      </v-icon>
                      {{ $t('authentication.multifactor.activated.opt.label') }}
                    </v-list-item-title>
                  </v-list-item-content>
                  <v-list-item-content v-if="isSuperGluu">
                    <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle ml-0">
                      <v-icon
                        color="grey"
                        size="24">
                        mdi-information-outline
                      </v-icon>
                      {{ $t('authentication.multifactor.activated.supergluu.label') }}
                    </v-list-item-title>
                  </v-list-item-content>
                  <v-list-item-content v-if="isFido">
                    <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle ml-0">
                      <v-icon
                        color="grey"
                        size="24">
                        mdi-information-outline
                      </v-icon>
                      {{ $t('authentication.multifactor.activated.fido2.label') }}
                    </v-list-item-title>
                  </v-list-item-content>
                </div>
              </v-expansion-panel-header>
              <v-expansion-panel-content>
                <v-list-item class="pl-0">
                  <v-list-item-subtitle v-if="isOTP" class="text-sub-title text-left font-italic textSize caption infoTextStyle textLigneHeight">
                    <div class="textStyle">
                      {{ $t('authentication.multifactor.activated.opt.message.one') }}
                    </div>
                    <div>
                      {{ $t('authentication.multifactor.activated.opt.message.two') }}
                    </div>
                    <div>
                      {{ $t('authentication.multifactor.activated.opt.message.step.one') }}
                    </div>
                    <div>
                      {{ $t('authentication.multifactor.activated.opt.message.step.two') }}
                    </div>
                    <div>
                      {{ $t('authentication.multifactor.activated.opt.message.step.three') }}
                    </div>
                  </v-list-item-subtitle>
                  <v-list-item-subtitle v-if="isSuperGluu" class="text-sub-title text-left font-italic textSize caption infoTextStyle textLigneHeight">
                    <div>
                      {{ $t('authentication.multifactor.activated.supergluu.message.one') }}
                    </div>
                    <div>
                      {{ $t('authentication.multifactor.activated.supergluu.message.step.one') }}
                    </div>
                    <div>
                      {{ $t('authentication.multifactor.activated.supergluu.message.step.two') }}
                    </div>
                    <div class="mb-3 font-weight-bold">
                      {{ $t('authentication.multifactor.activated.supergluu.message.step.three') }}
                    </div>
                  </v-list-item-subtitle>
                  <v-list-item-subtitle v-if="isFido" class="text-sub-title text-left font-italic textSize caption infoTextStyle textLigneHeight">
                    <div>
                      {{ $t('authentication.multifactor.activated.fido2.message.one') }}
                    </div>
                    <div class="mb-3 font-weight-bold">
                      {{ $t('authentication.multifactor.activated.supergluu.message.step.three') }}
                    </div>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-expansion-panel-content>
            </v-expansion-panel>
          </v-expansion-panels>
        </template>
      </div>
    </div>
  </v-app>
</template>
<script>
import {changeMfaFeatureActivation, getMfaStatus} from '../multiFactorServices';
export default {
  data: () => ({
    isMultifacorAuthenticationEnabled: true,
    isManage2faPage: false,
    protectedGroupsUsers: null,
    selectedGroups: null,
    items: ['OTP', 'SuperGluu', 'Fido 2'],
    select: 'OTP',
    panel: [0, 1],
    panel1: [0, 1],
  }),
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  created() {
    this.$root.$on('protectedGroupsList', this.protectedGroupsList);
    this.getMfaStatus();
  },
  computed: {
    isFido (){
      return this.select === 'Fido 2';
    },
    isOTP (){
      return this.select === 'OTP';
    },
    isSuperGluu (){
      return this.select === 'SuperGluu';
    },
  },
  methods: {
    switchAuthenticationStatus() {
      changeMfaFeatureActivation( !this.isMultifacorAuthenticationEnabled);
      this.isMultifacorAuthenticationEnabled = !this.isMultifacorAuthenticationEnabled;
    },
    protectedGroupsList(selectedGroups) {
      this.selectedGroups = selectedGroups;
    },
    getMfaStatus() {
      getMfaStatus().then(status => {
        this.isMultifacorAuthenticationEnabled = status.mfaStatus === 'true';
      });
    },
  }
};
</script>
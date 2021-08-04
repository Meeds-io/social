<template>
  <v-app id="multifactorAuthentication">
    <v-alert
      v-model="alert"
      :type="type"
      dismissible>
      {{ message }}
    </v-alert>
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
            <v-list-item-content class="pt-0">
              <div class="d-flex">
                <v-list-item-title v-if="!isMultifacorAuthenticationEnabled" class="mb-1 text-color font-weight-medium textSize  ml-2 caption infoTextStyle">
                  {{ $t('authentication.multifactor.header') }}
                </v-list-item-title>
                <v-list-item-subtitle v-if="isMultifacorAuthenticationEnabled" class="mt-3 text-sub-title infoTextStyle textLigneHeight font-italic textSize caption">
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
              <v-row v-if="isMultifacorAuthenticationEnabled">
                <v-col cols="3">
                  <v-select
                    ref="selectItem"
                    v-model="currentMfaSystem"
                    :items="items"
                    @change="switchMfaSystem($event)"
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
        <div class="mt-6">
          <v-list>
            <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle ml-6">
              {{ $t('authentication.multifactor.revocation.title') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title text-justify font-italic textSize caption ml-6 infoTextStyle textLigneHeight">
              {{ $t('authentication.multifactor.revocation.subtitle') }}
            </v-list-item-subtitle>
            <v-list id="revocationRequestsList">
              <v-list-item
                class="revocationRequestItem"
                v-for="request in revocationRequests"
                :key="request.id">
                <v-list-item-content class="flex-nowrap">
                  <div class="flex-shrink-1">
                    <exo-user-avatar
                      :username="request.username"
                      :fullname="request.fullname"
                      :external="request.isExternal"
                      :retrieve-extra-information="false" />
                  </div>
                  <v-btn
                    v-exo-tooltip.bottom.body="$t('authentication.multifactor.revocation.action.accept')"
                    icon
                    class="flex-shrink-1 mb-0"
                    @click="updateRevocationRequest($event,request.id,'confirm')">
                    <v-icon class="acceptRevocationIcon">
                      mdi-checkbox-marked-circle
                    </v-icon>
                  </v-btn>
                  <v-btn
                    v-exo-tooltip.bottom.body="$t('authentication.multifactor.revocation.action.decline')"
                    icon
                    class="flex-shrink-1 mb-0"
                    @click="updateRevocationRequest($event,request.id,'cancel')">
                    <v-icon class="refuseRevocationIcon">
                      mdi-close-circle
                    </v-icon>
                  </v-btn>
                </v-list-item-content>
              </v-list-item>
            </v-list>
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
                <v-list-item-content v-if="currentMfaSystemHelpTitle != null">
                  <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle ml-0">
                    <v-icon
                      color="grey"
                      size="24">
                      mdi-information-outline
                    </v-icon>
                    <span v-html="currentMfaSystemHelpTitle"></span>
                  </v-list-item-title>
                </v-list-item-content>
              </v-expansion-panel-header>
              <v-expansion-panel-content>
                <v-list-item class="pl-0" v-if="currentMfaSystemHelpContent != null">
                  <v-list-item-subtitle
                    class="text-sub-title text-left font-italic textSize caption infoTextStyle textLigneHeight">
                    <span v-html="currentMfaSystemHelpContent"></span>
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
import {changeMfaFeatureActivation, getRevocationRequests, updateRevocationRequest, getCurrentMfaSystem, changeMfaSytem, getProtectedGroups,getAvailableMfaSystem} from '../multiFactorServices';
export default {
  data: () => ({
    isMultifacorAuthenticationEnabled: true,
    isManage2faPage: false,
    protectedGroupsUsers: null,
    revocationRequests: [],
    selectedGroups: [],
    items: [],
    currentMfaSystem: null,
    currentMfaSystemHelpTitle: null,
    currentMfaSystemHelpContent: null,
    panel: [0, 1],
    panel1: [0, 1],
    alert: false,
    type: '',
    message: ''
  }),
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  created() {
    this.$root.$on('show-alert', message => {
      this.displayMessage(message);
    });
    this.$root.$on('protectedGroupsList', this.protectedGroupsList);
    this.getRevocationRequest();
    this.getMfaFeatureStatus();
    this.getCurrentMfaSystem();
    this.getAvailableMfaSystems();
    this.getProtectedGroups();
  },
  methods: {
    switchAuthenticationStatus() {
      changeMfaFeatureActivation(!this.isMultifacorAuthenticationEnabled);
      this.isMultifacorAuthenticationEnabled = !this.isMultifacorAuthenticationEnabled;
    },
    protectedGroupsList(selectedGroups) {
      this.selectedGroups = selectedGroups;
    },
    getMfaFeatureStatus() {
      this.$featureService.isFeatureEnabled('mfa').then(status => {
        this.isMultifacorAuthenticationEnabled = status;
      });
    },
    getProtectedGroups() {
      getProtectedGroups().then(data => {
        for (const group of data.protectedGroups) {
          this.selectedGroups.push(group);
        }
      });
    },
    switchMfaSystem(system) {
      changeMfaSytem(system).then(() => {
        this.getCurrentMfaSystem();
      });
    },
    getCurrentMfaSystem() {
      getCurrentMfaSystem().then(settings => {
        this.currentMfaSystem=settings.mfaSystem;
        this.currentMfaSystemHelpTitle=settings.helpTitle;
        this.currentMfaSystemHelpContent=settings.helpContent;
      });
    },
    getAvailableMfaSystems() {
      getAvailableMfaSystem().then(data => {
        for (const system of data.available) {
          this.items.push(system);
        }
      });
    },
    getRevocationRequest() {
      getRevocationRequests().then(revocationRequests => {
        const promiseArray = [];
        for (const [index, revocationRequest] of revocationRequests.requests.entries()) {
          const userPromise = this.$userService.getUser(revocationRequest.username).then(user => {
            revocationRequest.fullname = user.fullname;
            revocationRequest.isExternal = user.external === 'true';
            revocationRequests.requests[index] = revocationRequest;
          });
          promiseArray.push(userPromise);
        }
        Promise.all(promiseArray).then(() => {
          this.revocationRequests=revocationRequests.requests;
        });
      });
    },
    updateRevocationRequest(event, id,status) {
      event.preventDefault();
      event.stopPropagation();
      updateRevocationRequest(id,status).then(resp => {
        let message='';
        let type='';
        if (resp && resp.ok) {
          if (status === 'confirm') {
            message = this.$t('mfa.otp.access.revocation.confirm.success');
          } else {
            message = this.$t('mfa.otp.access.revocation.cancel.success');
          }
          type='success';
        } else {
          type='error';
          if (status === 'confirm') {
            message = this.$t('mfa.otp.access.revocation.confirm.error');
          } else {
            message = this.$t('mfa.otp.access.revocation.cancel.error');
          }
        }
        this.$root.$emit('show-alert', {
          type: type,
          message: message
        });
        this.getRevocationRequest();
      });
    },
    displayMessage(message) {
      this.message=message.message;
      this.type=message.type;
      this.alert = true;
      window.setTimeout(() => this.alert = false, 5000);
    }
  }
};
</script>

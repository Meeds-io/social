<template>
  <v-app class="white">
    <multifactor-authentication-component v-if="!isManage2faPage" />
    <div class="d-flex">
      <div
        v-if="isManage2faPage"
        class="my-11 mr-4 ml-4 border-radius firstBlock"
        flat>
        <v-card flat>
          <div class="d-flex">
            <v-list-item>
              <template>
                <v-btn
                  class="mr-3"
                  small
                  icon
                  @click="openManage2fa">
                  <v-icon size="20" class="text-sub-title">
                    {{ $vuetify.rtl && 'mdi-arrow-right' || ' mdi-arrow-left' }}
                  </v-icon>
                </v-btn>
              </template>
              <v-list-item-content>
                <v-list-item-title class="title text-color font-weight-bold subtitle-1 ml-2 infoTextStyle">
                  {{ $t('authentication.multifactor.manage.group') }}
                </v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-scale-transition>
              <!--v-text-field
                :placeholder="$t('authentication.multifactor.manage.filter')"
                prepend-inner-icon="fa-filter"
                single-line
                hide-details
                class="inputFilter pa-0 me-3 my-auto" /-->
            </v-scale-transition>
          </div>
          <v-row no-gutters>
            <div class="my-auto col-4 ml-4 d-flex">
              <h4 class="title text-color font-weight-bold subtitle-1 ml-2 infoTextStyle">{{ $t('authentication.multifactor.protected.resources') }}</h4>
              <div class="my-auto ml-5">
                <v-btn
                  icon
                  outlined
                  small
                  @click="$root.$emit('protectedResource', protectedResource)">
                  <i class="uiIconSocSimplePlus uiIconLightBlue"></i>
                </v-btn>
              </div>
            </div>
          </v-row>
          <protected-resource-component v-if="isManage2faPage" />
        </v-card>
      </div>
      <div
        v-if="isManage2faPage"
        class="my-8 mr-4 border-radius secondBlock"
        flat>
        <v-expansion-panels
          v-model="panel"
          class="mt-3"
          multiple>
          <v-expansion-panel>
            <v-expansion-panel-header expand-icon="mdi-menu-down" class="panelBlock pa-1">
              <div class="d-flex">
                <v-list-item-content>
                  <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle ml-3">
                    <v-icon
                      color="grey"
                      size="24">
                      mdi-information-outline
                    </v-icon>
                    {{ $t('authentication.multifactor.protected.resources') }}
                  </v-list-item-title>
                </v-list-item-content>
              </div>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-list-item class="pa-0">
                <v-list-item-subtitle class="text-sub-title text-left font-italic textSize caption infoTextStyle textLigneHeight">
                  <div class="textStyle">
                    {{ $t('authentication.multifactor.protected.group.label.one') }}
                  </div>
                  <div>
                    {{ $t('authentication.multifactor.protected.group.label.two') }}
                  </div>
                  <div>
                    {{ $t('authentication.multifactor.protected.group.label.three') }}
                  </div>
                </v-list-item-subtitle>
              </v-list-item>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </div>
    </div>
    <protected-resource-drawer />
    <protected-groups-users-drawer />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    isManage2faPage: false,
    protectedResource: null,
    panel: [0, 1],
  }),
  created() {
    this.$root.$on('isManage2faPage', this.openManage2fa);
  },

  methods: {
    openManage2fa() {
      this.isManage2faPage = !this.isManage2faPage;
    },
  }
};
</script>

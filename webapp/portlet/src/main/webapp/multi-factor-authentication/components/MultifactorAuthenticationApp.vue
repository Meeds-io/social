<template>
  <v-app id="multifactorAuthentication">
    <div class="d-flex">
      <v-card class="my-8 mr-4 ml-12 border-radius firstBlock" flat>
        <v-list>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle">
                {{ $t('authentication.multifactor.title') }}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title v-if="!isMultifacorAuthenticationEnabled" class="mb-1 text-color font-weight-medium textSize caption">
                {{ $t('authentication.multifactor.header') }}
              </v-list-item-title>
              <v-list-item-title v-if="isMultifacorAuthenticationEnabled" class="mb-1 text-color font-weight-medium textSize caption">
                <div>
                  {{ $t('authentication.multifactor.second.block.description') }}
                </div>
              </v-list-item-title>
              <v-list-item-subtitle v-if="isMultifacorAuthenticationEnabled" class="text-sub-title infoTextStyle font-italic textSize caption my-3">
                <div>
                  <v-icon
                    class="me-1 ml-2"
                    color="grey"
                    size="24">
                    mdi-information-outline
                  </v-icon>
                  {{ $t('authentication.multifactor.label') }}
                </div>
              </v-list-item-subtitle>
              <v-row v-if="isMultifacorAuthenticationEnabled" class="ml-5">
                <v-col cols="3">
                  <v-combobox
                    v-model="select"
                    :items="items"
                    class="authenticationInput"
                    autofocus
                    outlined
                    dense />
                </v-col>
              </v-row>
            </v-list-item-content>
            <v-list-item-action>
              <div>
                <label class="switch">
                  <input
                    type="checkbox">
                  <div class="slider round"><span class="absolute-activate">{{ $t(`authentication.multifactor.button.yes`) }}</span></div>
                  <span class="absolute-deactivated">{{ $t(`authentication.multifactor.button.no`) }}</span>
                </label>
              </div>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </v-card>
      <v-card class="my-8 mx-4 border-radius secondBlock" flat>
        <v-list v-if="!isMultifacorAuthenticationEnabled">
          <v-list-item>
            <v-list-item-content>
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
            </v-list-item-content>
          </v-list-item>
          <v-list-item>
            <v-list-item-subtitle class="text-sub-title font-italic textSize infoTextStyle caption textLigneHeight">
              <p>
                {{ $t('authentication.multifactor.second.block.description') }}
              </p>
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
        <v-list v-if="isMultifacorAuthenticationEnabled">
          <v-list-item>
            <v-list-item-content v-if="otp">
              <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle">
                <v-icon
                  color="grey"
                  size="24">
                  mdi-information-outline
                </v-icon>
                {{ $t('authentication.multifactor.activated.opt.label') }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-content v-if="superGluu">
              <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle">
                <v-icon
                  color="grey"
                  size="24">
                  mdi-information-outline
                </v-icon>
                {{ $t('authentication.multifactor.activated.supergluu.label') }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-content v-if="fido">
              <v-list-item-title class="title text-color font-weight-bold subtitle-1 infoTextStyle">
                <v-icon
                  color="grey"
                  size="24">
                  mdi-information-outline
                </v-icon>
                {{ $t('authentication.multifactor.activated.fido2.label') }}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item>
            <v-list-item-subtitle v-if="otp" class="text-sub-title text-justify font-italic textSize caption infoTextStyle textLigneHeight">
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
            <v-list-item-subtitle v-if="superGluu" class="text-sub-title text-justify font-italic textSize caption infoTextStyle textLigneHeight">
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
            <v-list-item-subtitle v-if="fido" class="text-sub-title text-justify font-italic textSize caption infoTextStyle textLigneHeight">
              <div>
                {{ $t('authentication.multifactor.activated.fido2.message.one') }}
              </div>
              <div class="mb-3">
                {{ $t('authentication.multifactor.activated.supergluu.message.step.three') }}
              </div>
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card>
    </div>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    isMultifacorAuthenticationEnabled: true,
    items: ['OTP', 'SuperGlu', 'Fido 2'],
    select: 'OTP',
    fido: false,
    superGluu: true,
    otp: false,
  }),
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
};
</script>
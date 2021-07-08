<template>
  <v-card
    class="ma-4"
    flat>
    <v-toolbar
      class="border-box-sizing"
      flat>
      <v-btn
        class="mx-1"
        icon
        height="36"
        width="36"
        @click="$emit('back')">
        <v-icon size="20">
          {{ $vuetify.rtl && 'mdi-arrow-right' || 'mdi-arrow-left' }}
        </v-icon>
      </v-btn>
      <v-toolbar-title class="ps-0">
        {{ $t('MobileSettings.connect') }}
      </v-toolbar-title>
      <v-spacer />
    </v-toolbar>
    <div class="d-flex py-6 my-6 justify-space-between align-center text-left">
      <div>
        <v-list-item>
          <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-5">
            <v-list-item-title class="dark-grey-color font-weight-bold">
              {{ $t('MobileSettings.subtitle') }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-2">
            <v-list-item-title class="text-color px-0 pb-0 pt-2 mt-auto mb-2">
              {{ $t('MobileSettings.step.one') }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-2">
            <v-list-item-title class="text-color px-0 pb-0 pt-2 mt-auto mb-2">
              {{ $t('MobileSettings.step.two') }}<img src="/social-portlet/images/scanhelper.png" :alt="$t('MobileSettings.icon')"><span class="logoTitle subtitle-2 font-weight-bold text-truncate">{{ $t('MobileSettings.bg') }}</span>
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-2">
            <v-list-item-title class="text-color px-0 pb-0 pt-2 mt-auto mb-2">
              {{ $t('MobileSettings.step.three') }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </div>
      <div class="mx-16" id="qrcode"></div>
    </div>
  </v-card>
</template>

<script>
export default {
  mounted(){
    // Options
    const options = {
      text: ((window.location.port > 0) ? `${window.location.protocol}//${window.location.hostname}:${window.location.port}/portal/login?username=${eXo.env.portal.userName}` : `${window.location.protocol}//${window.location.hostname}/portal/login?username=${eXo.env.portal.userName}`)
    };

    // Create new QRCode Object
    new QRCode(document.getElementById('qrcode'), options);
  },
};
</script>
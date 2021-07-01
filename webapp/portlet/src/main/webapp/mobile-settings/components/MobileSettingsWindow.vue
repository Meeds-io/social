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
    <v-list-item>
      <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-5">
        <v-list-item-title class="px-0 pb-0 pt-2 mt-auto mb-2">
          {{ $t('MobileSettings.subtitle') }}
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-list-item>
      <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-2">
        <v-list-item-title class="px-0 pb-0 pt-2 mt-auto mb-2">
          {{ $t('MobileSettings.step.one') }}
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-list-item>
      <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-2">
        <v-list-item-title class="px-0 pb-0 pt-2 mt-auto mb-2">
          {{ $t('MobileSettings.step.two') }}<img src="/social/images/scan-helper.png"> {{ $t('MobileSettings.bg') }}
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-list-item>
      <v-list-item-content class="px-11 pb-0 pt-2 mt-auto mb-2">
        <v-list-item-title class="px-0 pb-0 pt-2 mt-auto mb-2">
          {{ $t('MobileSettings.step.three') }}
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
  </v-card>
</template>
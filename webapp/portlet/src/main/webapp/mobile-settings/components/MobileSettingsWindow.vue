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
        <span> Connect to the eXo mobile application via QR code </span>
      </v-toolbar-title>
      <v-spacer />
    </v-toolbar>
    <v-list-item>
      <v-list-item-content class="px-0 pb-0 pt-2 mt-auto mb-2">
        <v-list-item-title class="text-color text-wrap">
          <span>To use eXo platform on your phone</span>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
  </v-card>
</template>
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
        <v-icon size="20">mdi-arrow-left</v-icon>
      </v-btn>
      <v-toolbar-title class="pl-0">
        {{ $t('SpaceSettings.applications') }}
      </v-toolbar-title>
      <v-spacer />
    </v-toolbar>

    <v-container class="border-box-sizing">
      <v-row dense>
        <v-col
          v-for="(app, index) in applications"
          :key="app"
          class="SpaceApplicationCard ma-3">
          <space-setting-application-card
            :application="app"
            :space="space"
            :index="index"
            :length="applications.length"
            @moveAfter="moveAfter(app)"
            @moveBefore="moveBefore(app)"
            @remove="remove(app)" />
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    saving: false,
  }),
  computed: {
    applications() {
      return this.space && this.space.applications || [];
    },
  },
  methods: {
    
  },
};
</script>


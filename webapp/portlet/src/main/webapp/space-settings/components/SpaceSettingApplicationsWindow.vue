<template>
  <v-card
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
          :key="app.id"
          class="SpaceApplicationCard ma-1">
          <space-setting-application-card
            :application="app"
            :space-id="spaceId"
            :index="index"
            :length="applications.length"
            @moveAfter="moveAfter(app)"
            @moveBefore="moveBefore(app)"
            @remove="remove(app)" />
        </v-col>
        <v-col class="SpaceApplicationCard dashed-border ma-1">
          <v-btn
            class="primary--text"
            width="100%"
            height="100%"
            text
            @click="openDrawer">
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <space-setting-add-application-drawer
      ref="spaceSettingAddApplicationDrawer"
      :space-id="spaceId"
      @refresh="$emit('refresh')" />
  </v-card>
</template>

<script>
export default {
  props: {
    spaceId: {
      type: String,
      default: null,
    },
    applications: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    saving: false,
  }),
  methods: {
    moveAfter(application) {
      this.$spaceService.moveApplicationDown(this.spaceId, application.id)
        .then(() => this.$emit('refresh'));
    },
    moveBefore(application) {
      this.$spaceService.moveApplicationUp(this.spaceId, application.id)
        .then(() => this.$emit('refresh'));
    },
    remove(application) {
      this.$spaceService.removeApplication(this.spaceId, application.id)
        .then(() => this.$emit('refresh'));
    },
    openDrawer() {
      this.$refs.spaceSettingAddApplicationDrawer.open();
    },
  },
};
</script>


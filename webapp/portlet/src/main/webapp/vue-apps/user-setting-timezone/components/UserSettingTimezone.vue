<template>
  <v-app v-if="displayed">
    <v-card class="my-3 border-radius" flat>
      <v-list two-line>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('UserSettings.timezone') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title text-capitalize font-italic">
              {{ timezoneLabel }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              small
              icon
              @click="openDrawer">
              <i class="uiIconEdit uiIconLightBlue pb-2"></i>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <user-timezone-drawer
        ref="timezonesDrawer"
        v-model="timezoneOffset"
        :timezones="timezones" />
    </v-card>
  </v-app>
</template>

<script>
export default {
  props: {
    timezones: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    id: `Settings${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    timezoneOffset: eXo.env.portal.timezoneOffset,
    selectedTimezone: null,
    displayed: true,
  }),
  computed: {
    timezoneLabel() {
      return this.selectedTimezone && this.selectedTimezone.text;
    },
  },
  created() {
    this.selectedTimezone = this.timezones.find(tmp => tmp.offset === this.timezoneOffset);
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
    this.$nextTick().then(() => this.$root.$applicationLoaded());
  },
  methods: {
    openDrawer() {
      this.$refs.timezonesDrawer.open();
    },
  },
};
</script>
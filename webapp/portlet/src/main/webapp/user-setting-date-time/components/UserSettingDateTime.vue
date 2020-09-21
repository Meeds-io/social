<template>
  <v-app v-if="displayed">
    <v-card class="ma-4 border-radius" flat>
      <v-list two-line>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
                {{ skeleton && '&nbsp;' || $t('UserSettings.dateTime') }}
              </div>
            </v-list-item-title>
            <user-setting-date-time-info
              :skeleton="skeleton"
              :timezone-label="timezoneLabel"
              :time-format-label="timeFormat"
              :date-format-label="dateFormat" ></user-setting-date-time-info>

            <v-divider class="mx-4" />
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :class="skeleton && 'skeleton-background'"
              small
              icon
              @click="openDrawer">
              <i v-if="!skeleton" class="uiIconEdit uiIconLightBlue pb-2"></i>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <user-date-time-drawer
        ref="dateTimeDrawer"
        :timezones="timezones" />
    </v-card>
  </v-app>
</template>

<script>
export default {
  props: {
    timezones: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    id: `Settings${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    timezoneOffset: eXo.env.portal.timezoneOffset,
    dateFormat: eXo.env.portal.dateFormat,
    timeFormat: eXo.env.portal.timeFormat,
    selectedTimezone: null,
    displayed: true,
    skeleton: true,
  }),
  computed: {
    timezoneLabel() {
      return this.selectedTimezone && this.selectedTimezone.text;
    },
    DateFormatLabel() {
      return this.$t(`UserSettings.agenda.drawer.dateFormat.${this.dateFormat}`);
    },
    timeFormatLabel() {
      return this.$t(`UserSettings.agenda.drawer.timeFormat.${this.timeFormat}`);
    },
  },
  created() {
    this.selectedTimezone = this.timezones.find(tmp => tmp.offset === this.timezoneOffset);
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
    this.skeleton = false;
  },
  methods: {
    openDrawer() {
      this.$refs.dateTimeDrawer.open();
    },
  },
};
</script>
<template>
  <exo-drawer
    ref="userDateTimeDrawer"
    class="userTimezoneDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('UserSettings.dateTime') }}
    </template>
    <template slot="content">
      <v-tabs
        v-model="settingTab"
        fixed-tabs
        centered
        class="py-4">
        <v-tabs-slider />
        <v-tab href="#timezone" dark>
          {{ $t('UserSettings.dateTime.timeZone') }}
        </v-tab>
        <v-tab href="#dateFormat" dark>
          {{ $t('UserSettings.dateTime.dateFormat') }}
        </v-tab>
        <v-tab href="#timeFormat" dark>
          {{ $t('UserSettings.dateTime.timeFormat') }}
        </v-tab>
        <v-tab-item value="timezone">
          <user-setting-time-zone-tab
            ref="timeZoneTab"
            v-model="timezoneOffset"
            :timezones="timezones"
            class="pa-5" />
        </v-tab-item>
        <v-tab-item value="dateFormat">
          <user-setting-date-tab
            ref="dateFormatTab"
            v-model="dateFormat"
            class="pa-5" />
        </v-tab-item>
        <v-tab-item
          value="timeFormat"
          class="pa-5" >
          <user-setting-time-tab
            ref="timeFormatTab"
            v-model="timeFormat"/>
        </v-tab-item>
      </v-tabs>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn mr-2"
          @click="cancel">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="saving"
          class="btn btn-primary"
          @click="saveDateTimeSettings">
          {{ $t('UserSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
const ONE_HOUR_DST = 3600000;
export default {
  props: {
    timezones: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    timezoneOffset: eXo.env.portal.timezoneOffset,
    dateFormat: eXo.env.portal.dateFormat,
    timeFormat: eXo.env.portal.timeFormat,
    timezoneDSTSavings: eXo.env.portal.timezoneDSTSavings,
    enableTimezoneDSTSavings: false,
    saving: false,
    settingTab: null,
  }),
  computed: {
    selectedTimezone() {
      return this.timezones.find(tmp => tmp.offset === this.timezoneOffset);
    },
  },
  created() {
    this.enableTimezoneDSTSavings = !!this.timezoneDSTSavings;
  },
  methods: {
    open() {
      this.$refs.userDateTimeDrawer.open();
    },
    close() {
      this.$refs.userDateTimeDrawer.close();
    },
    saveDateTimeSettings() {
      this.saving = true;
      this.$refs.userDateTimeDrawer.startLoading();
      this.timezoneDSTSavings = this.enableTimezoneDSTSavings && ONE_HOUR_DST || 0;
      this.$userService.updateProfileFields(eXo.env.portal.userName, {
        timeZone: this.selectedTimezone.id,
        timeZoneDSTSavings: String(this.timezoneDSTSavings),
        dateFormat: String(this.dateFormat),
        timeFormat: String(this.timeFormat),
      }, ['timeZone', 'timeZoneDSTSavings', 'dateFormat' , 'timeFormat'])
        .then(() => {
          window.location.replace(window.location.href);
        })
        .finally(() => {
          this.$refs.userDateTimeDrawer.endLoading();
          this.saving = false;
        });
    },
    cancel() {
      this.$refs.userDateTimeDrawer.close();
    },
  },
};
</script>


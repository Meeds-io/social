<template>
  <exo-drawer
    ref="userTimezoneDrawer"
    class="userTimezoneDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('UserSettings.timezone') }}
    </template>
    <template v-if="timezones && timezones.length" slot="content">
      <v-switch
        v-model="enableTimezoneDSTSavings"
        :label="$t('UserSettings.timezone.dayLightSavings')"
        class="mx-5" />
      <v-radio-group v-model="value" class="px-4">
        <v-radio
          v-for="timezone in timezones"
          :key="timezone.value"
          :label="timezone.text"
          :value="timezone.offset"
          class="text-capitalize" />
      </v-radio-group>
      <div class="d-flex flex-column mb-5">
        <label class="switch-label-text mt-1 text-subtitle-1">{{ $t('UserSettings.agenda.drawer.label.DateFormat') }}</label>
        <select v-model="value.agendaDateFormat" class="width-auto my-auto pr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
          <option value="MM/DD/YY">{{ $t('UserSettings.agenda.drawer.dateFormat.monthDayYearBySlash') }}</option>
          <option value="MM-DD-YY">{{ $t('UserSettings.agenda.drawer.dateFormat.monthDayYearByHyphen') }}</option>
          <option value="DD/MM/YY">{{ $t('UserSettings.agenda.drawer.timeFormat.dayMonthYearBySlash') }}</option>
          <option value="DD-MM-YY">{{ $t('UserSettings.agenda.drawer.timeFormat.dayMonthYearByHyphen') }}</option>
        </select>
      </div>
      <div class="d-flex flex-column mb-5">
        <label class="switch-label-text mt-1 text-subtitle-1">{{ $t('UserSettings.agenda.drawer.label.TimeFormat') }}</label>
        <select v-model="value.agendaTimeFormat" class="width-auto my-auto pr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
          <option>{{ $t('UserSettings.agenda.drawer.timeFormat.AMPM') }}</option>
          <option>{{ $t('UserSettings.agenda.drawer.timeFormat.24hours') }}</option>
        </select>
      </div>
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
          @click="saveTimezone">
          {{ $t('UserSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
const ONE_HOUSR_DST = 3600000;

export default {
  props: {
    timezones: {
      type: Array,
      default: null,
    },
    value: {
      type: Number,
      default: 0,
    },
  },
  data: () => ({
    timezoneDSTSavings: eXo.env.portal.timezoneDSTSavings,
    enableTimezoneDSTSavings: false,
    saving: false,
  }),
  computed: {
    selectedTimezone() {
      return this.timezones.find(tmp => tmp.offset === this.value);
    },
  },
  created() {
    this.enableTimezoneDSTSavings = !!this.timezoneDSTSavings;
  },
  methods: {
    open() {
      this.$refs.userTimezoneDrawer.open();
    },
    saveTimezone() {
      this.saving = true;
      this.$refs.userTimezoneDrawer.startLoading();
      this.timezoneDSTSavings = this.enableTimezoneDSTSavings && ONE_HOUSR_DST || 0;
      this.$userService.updateProfileFields(eXo.env.portal.userName, {
        timeZone: this.selectedTimezone.id,
        timeZoneDSTSavings: String(this.timezoneDSTSavings),
      }, ['timeZone', 'timeZoneDSTSavings'])
        .then(() => {
          window.location.replace(window.location.href);
        })
        .finally(() => {
          this.$refs.userTimezoneDrawer.endLoading();
          this.saving = false;
        });
    },
    cancel() {
      this.$refs.userTimezoneDrawer.close();
    },
  },
};
</script>


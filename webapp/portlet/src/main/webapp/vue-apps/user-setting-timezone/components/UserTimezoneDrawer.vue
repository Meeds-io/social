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
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
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


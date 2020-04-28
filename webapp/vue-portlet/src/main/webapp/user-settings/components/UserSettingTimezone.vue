<template>
  <v-card class="ma-4 border-radius" flat>
    <v-list two-line>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="title text-color">
            {{ $t('UserSettings.timezone') }}
          </v-list-item-title>
          <v-list-item-subtitle class="text-sub-title">
            {{ timezoneLabel }}
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn icon @click="openDrawer">
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
    timezoneOffset: eXo.env.portal.timezoneOffset,
    selectedTimezone: null,
  }),
  computed: {
    timezoneLabel() {
      return this.selectedTimezone && this.selectedTimezone.text;
    },
  },
  created() {
    this.selectedTimezone = this.timezones.find(tmp => tmp.offset === this.timezoneOffset);
  },
  methods: {
    openDrawer() {
      this.$refs.timezonesDrawer.open();
    },
  },
};
</script>
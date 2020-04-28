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
          <v-btn icon>
            <i class="uiIconEdit uiIconLightBlue pb-2"></i>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  computed: {
    timezoneLabel() {
      const langTimezone = new Intl.DateTimeFormat().resolvedOptions().timeZone;
      return langTimezone;
    },
  },
  watch: {
    value() {
      this.$emit('input', this.value);
    },
  },
};
</script>


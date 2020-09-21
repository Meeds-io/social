<template>
  <v-radio-group v-model="value">
    <v-radio
      :label="$t('UserSettings.agenda.drawer.dateFormat.monthDayYearBySlash')"
      value="MM/DD/YY"
      class="text-capitalize" />
    <v-radio
      :label="$t('UserSettings.agenda.drawer.dateFormat.monthDayYearByHyphen')"
      value="MM-DD-YY"
      class="text-capitalize" />
    <v-radio
      :label="$t('UserSettings.agenda.drawer.timeFormat.dayMonthYearBySlash')"
      value="DD/MM/YY"
      class="text-capitalize" />
    <v-radio
      :label="$t('UserSettings.agenda.drawer.timeFormat.dayMonthYearByHyphen')"
      value="DD-MM-YY"
      class="text-capitalize" />
  </v-radio-group>
</template>

<script>
export default {
  props: {
    value: {
      type: Number,
      default: 0,
    },
  },
  watch: {
    value: function(val) {
      this.$emit('input', val);
    }
  }
};
</script>
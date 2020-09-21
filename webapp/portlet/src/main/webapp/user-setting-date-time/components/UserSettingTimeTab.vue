<template>
  <v-radio-group v-model="value">
    <v-radio
      :label="$t('UserSettings.agenda.drawer.timeFormat.AMPM')"
      value="AMPM"
      class="text-capitalize" />
    <v-radio
      :label="$t('UserSettings.agenda.drawer.timeFormat.24hours')"
      value="24hours"
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
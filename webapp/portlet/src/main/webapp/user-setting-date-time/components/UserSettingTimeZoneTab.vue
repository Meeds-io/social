<template>
  <div class="d-flex flex-column mb-5">
    <v-switch
      v-model="enableTimezoneDSTSavings"
      :label="$t('UserSettings.timezone.dayLightSavings')" />
    <v-radio-group v-model="value">
      <v-radio
        v-for="timezone in timezones"
        :key="timezone.value"
        :label="timezone.text"
        :value="timezone.offset"
        class="text-capitalize" />
    </v-radio-group>
  </div>
</template>

<script>
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
  watch: {
    value: function(val) {
      this.$emit('input', val);
    }
  }
};
</script>
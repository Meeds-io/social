<template>
  <v-combobox
    ref="timePicker"
    v-model="selectedTime"
    :items="timeValues"
    :disabled="disabled"
    class="pt-0"
    outlined
    dense
    @blur="close()">
  </v-combobox>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    timeValues: [],
    selectedTime: ''
  }),
  watch: {
    selectedTime() {
      this.emitSelectedTime();
    },
    value() {
      this.selectedTime = this.value;
    }
  },
  created() {
    this.timeValues = this.$dateUtil.generateTimePickerValues();
  },
  methods: {
    emitSelectedTime () {
      this.$emit('input', this.selectedTime);
    },
    close() {
      this.$refs.timePicker.blur();
      this.emitSelectedTime();
    }
  }
};
</script>
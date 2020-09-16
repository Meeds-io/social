<template>
  <v-combobox
    ref="timePicker"
    v-model="selectedTime"
    :items="time"
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
  },
  data: () => ({
    time: [],
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
    this.time = this.$dateUtil.generateTimePickerValues();
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
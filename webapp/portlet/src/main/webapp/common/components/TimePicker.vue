<template>
  <select
    v-model="timeValue"
    class="width-auto my-auto ml-4 ignore-vuetify-classes">
    <option
      v-for="time in timeOptions"
      :key="time.value"
      :value="time.value">
      {{ time.text }}
    </option>
  </select>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: () => new Date(),
    },
    intervalMinutes: {
      type: Number,
      default: 15,
    },
    lang: {
      type: String,
      default: function() {
        return eXo.env.portal.language;
      },
    },
  },
  data: () => ({
    timeFormat: {
      hour: '2-digit',
      minute: '2-digit',
    },
    timeOptions: [],
    timeOptionsByValue: {},
    timeValue: '00:00',
    valueType: 'date',
  }),
  watch: {
    value() {
      this.computeSelectedValue();
    },
    timeValue(newVal, oldVal) {
      if (newVal === oldVal) {
        return;
      }
      if (!newVal) {
        this.$emit('change', null);
      }
      const timeOption = this.timeOptionsByValue[this.timeValue];
      if (this.valueType === 'date') {
        const date = new Date(this.value);
        date.setHours(timeOption.hours);
        date.setMinutes(timeOption.minutes);
        this.$emit('input', date);
      } else if (this.valueType === 'time') {
        this.$emit('input', this.timeValue);
      } else if (this.valueType === 'datetime') {
        const date = new Date(this.value);
        date.setHours(timeOption.hours);
        date.setMinutes(timeOption.minutes);
        this.$emit('input', date.toLocaleString(this.lang));
      } else if (this.valueType === 'number') {
        const date = new Date(this.value);
        date.setHours(timeOption.hours);
        date.setMinutes(timeOption.minutes);
        this.$emit('input', date.getTime());
      }
    },
  },
  mounted() {
    this.computeSelectedValue();
    for (let i = 0; i < 24; i++) {
      for (let j = 0; j < 60; j++) {
        const time = i * 60 + j;
        if (time % this.intervalMinutes === 0) {
          const timeOption = {
            value: this.getTimeValue(i, j),
            text: this.getTimeLabel(i, j),
            hours: i,
            minutes: j,
          };
          this.timeOptions.push(timeOption);
          this.timeOptionsByValue[timeOption.value] = timeOption;
        }
      }
    }
  },
  methods: {
    computeSelectedValue() {
      if (typeof this.value === 'string' && this.value.length === 5) {
        this.valueType = 'time';
        this.timeValue = this.value;
      } else if (typeof this.value === 'string' && this.value.length > 5) {
        this.valueType = 'datetime';
        this.timeValue = this.getTimeValueByDate(new Date(this.value));
      } else if (typeof this.value === 'object' && this.value.getTime) {
        this.valueType = 'date';
        this.timeValue = this.getTimeValueByDate(this.value);
      } else if (typeof this.value === 'number') {
        this.valueType = 'number';
        this.timeValue = this.getTimeValueByDate(new Date(this.value));
      }
    },
    getTimeValue(hour, minute) {
      const date = new Date(2003, 11, 20, hour, minute);
      return this.getTimeValueByDate(date);
    },
    getTimeValueByDate(date) {
      return this.$dateUtil.formatDateObjectToDisplay(date, this.timeFormat, 'fr');
    },
    getTimeLabel(hour, minute) {
      const date = new Date(2003, 11, 20, hour, minute);
      return this.$dateUtil.formatDateObjectToDisplay(date, this.timeFormat, this.lang);
    },
  },
};
</script>
<template>
  <select
    v-model="timeValue"
    class="width-auto my-auto ms-4 ignore-vuetify-classes">
    <option
      v-for="time in filteredTimeOptions"
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
    min: {
      type: Object,
      default: null,
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
  computed: {
    filteredTimeOptions() {
      return this.timeOptions && this.timeOptions.filter(timeOption => timeOption.enabled) || [];
    },
    minTimeInMinutes() {
      if (!this.min) {
        return 0;
      }
      if (typeof this.min === 'string' && this.min.length === 5) {
        const minHours = Number(this.min.substring(0, 2));
        const minMinutes = Number(this.min.substring(3, 5));
        return minHours * 60 + minMinutes;
      } else if (typeof this.min === 'string' && this.min.length > 5
          || typeof this.min === 'number'
          || typeof this.min === 'object' && this.min.getTime) {
        const date = new Date(this.min);
        if (!Number.isNaN(date.getTime())) {
          const minHours = date.getHours();
          const minMinutes = date.getMinutes();
          return minHours * 60 + minMinutes;
        }
      }
      return 0;
    },
    minTimeValue() {
      const minHours = parseInt(this.minTimeInMinutes / 60);
      const minMinutes = this.minTimeInMinutes % 60;
      return this.getTimeValue(minHours, minMinutes);
    },
  },
  watch: {
    minTimeValue() {
      this.computeOptions();
      this.computeTimeValue();
    },
    value() {
      this.computeSelectedValue();
    },
    timeValue(newVal, oldVal) {
      if (newVal === oldVal) {
        return;
      }
      if (!this.timeValue) {
        this.$emit('input', null);
        return;
      }
      const timeOption = this.timeOptionsByValue[this.timeValue];
      if (this.valueType === 'date') {
        const date = new Date(this.value);
        date.setHours(timeOption.hours);
        date.setMinutes(timeOption.minutes);
        date.setSeconds(0);
        this.$emit('input', date);
      } else if (this.valueType === 'time') {
        this.$emit('input', this.timeValue);
      } else if (this.valueType === 'datetime') {
        const date = new Date(this.value);
        date.setHours(timeOption.hours);
        date.setMinutes(timeOption.minutes);
        date.setSeconds(0);
        this.$emit('input', date.toLocaleString(this.lang));
      } else if (this.valueType === 'number') {
        const date = new Date(this.value);
        date.setHours(timeOption.hours);
        date.setMinutes(timeOption.minutes);
        date.setSeconds(0);
        this.$emit('input', date.getTime());
      }
    },
  },
  mounted() {
    this.computeOptions();
    this.computeSelectedValue();
  },
  methods: {
    computeOptions() {
      this.timeOptions = [];
      for (let i = 0; i < 24; i++) {
        for (let j = 0; j < 60; j++) {
          const time = i * 60 + j;
          if (time % this.intervalMinutes === 0) {
            const timeOption = {
              value: this.getTimeValue(i, j),
              text: this.getTimeLabel(i, j),
              hours: i,
              minutes: j,
              enabled: this.minTimeInMinutes <= time,
            };
            this.timeOptions.push(timeOption);
            this.timeOptionsByValue[timeOption.value] = timeOption;
          }
        }
      }
    },
    computeSelectedValue() {
      if (this.value) {
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
      }
      if (!this.timeValue) {
        this.timeValue = this.getTimeValueByDate(new Date());
      }
      this.computeTimeValue();
    },
    computeTimeValue() {
      const selectedOption = this.timeOptionsByValue[this.timeValue];
      if (selectedOption && !selectedOption.enabled) {
        this.timeValue = this.minTimeValue;
      }
      if (!this.timeValue) {
        this.timeValue = this.minTimeValue;
      }
    },
    getTimeValue(hour, minute) {
      const date = new Date(2003, 11, 20, hour, minute);
      return this.getTimeValueByDate(date);
    },
    getTimeValueByDate(date) {
      const time = parseInt((date.getHours() * 60 + date.getMinutes()) / this.intervalMinutes) * this.intervalMinutes;
      const minutes = time % 60;
      const hours = parseInt(time / 60);
      date.setHours(hours);
      date.setMinutes(minutes);
      return this.$dateUtil.formatDateObjectToDisplay(date, this.timeFormat, 'fr');
    },
    getTimeLabel(hour, minute) {
      const date = new Date(2003, 11, 20, hour, minute);
      return this.$dateUtil.formatDateObjectToDisplay(date, this.timeFormat, this.lang);
    },
  },
};
</script>
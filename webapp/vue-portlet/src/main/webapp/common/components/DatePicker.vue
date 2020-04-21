<template>
  <v-flex
    :id="id"
    class="datePickerComponent">
    <v-menu
      ref="selectDateMenu"
      v-model="menu"
      close-on-content-click
      close-on-click
      transition="scale-transition"
      offset-y
      eager
      class="datePickerMenu">
      <input
        v-slot:activator="{ on }"
        slot="activator"
        slot-scope="{ on }"
        v-model="dateFormatted"
        :placeholder="placeholder"
        :prepend-icon="prependIcon"
        readonly
        type="text"
        class="ignore-vuetify-classes datePickerText flex-grow-0"
        v-on="on" />
      <v-date-picker
        v-model="date"
        :first-day-of-week="1"
        :type="periodType"
        :locale="lang"
        class="border-box-sizing"
        @input="menu = false" />
    </v-menu>
  </v-flex>
</template>

<script>
import * as DateUtil from '../js/DateUtil.js';

export default {
  props: {
    placeholder: {
      type: String,
      default: function() {
        return null;
      },
    },
    prependIcon: {
      type: String,
      default: function() {
        return 'event';
      },
    },
    periodType: {
      type: String,
      default: function() {
        return 'date';
      },
    },
    value: {
      type: String,
      default: function() {
        return null;
      },
    },
    lang: {
      type: String,
      default: function() {
        return eXo.env.portal.language;
      },
    },
    format: {
      type: Object,
      default: function() {
        return {
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        };
      },
    },
  },
  data: () => ({
    id: `DatePicker${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    date: null,
    dateFormatted: null,
    dateValue: null,
    menu: false,
  }),
  watch: {
    value(newVal, oldVal) {
      if (oldVal !== newVal) {
        this.computeDates();
      }
    },
    date() {
      const dateObj = this.date && new Date(this.date);
      this.dateValue = dateObj && dateObj.getTime() || null;
      if (this.dateValue) {
        this.dateFormatted = DateUtil.formatDateObjectToDisplay(dateObj, this.format, this.lang);
      } else {
        this.dateFormatted = null;
      }
      this.$emit('input', this.dateValue);
    },
  },
  mounted() {
    this.computeDates();
    $(document).on('click', () => this.menu = false);
  },
  methods: {
    computeDates() {
      if (this.value) {
        const dateObj = DateUtil.getDateObjectFromString(this.value, true);
        this.date = DateUtil.getISODate(dateObj);
      } else {
        this.date = DateUtil.getISODate(new Date());
      }
    },
  },
};
</script>

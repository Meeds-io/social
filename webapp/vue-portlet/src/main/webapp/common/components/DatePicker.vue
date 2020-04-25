<template>
  <v-flex
    :id="id"
    class="datePickerComponent">
    <v-menu
      ref="selectDateMenu"
      v-model="menu"
      :close-on-content-click="false"
      :disabled="disabled"
      class="datePickerMenu"
      transition="scale-transition"
      offset-y
      eager>
      <input
        v-slot:activator="{ on }"
        slot="activator"
        slot-scope="{ on }"
        v-model="dateFormatted"
        :disabled="disabled"
        :placeholder="placeholder"
        :required="required"
        class="ignore-vuetify-classes datePickerText flex-grow-0"
        readonly
        type="text"
        v-on="on" />
      <v-date-picker
        v-model="date"
        :first-day-of-week="1"
        :type="periodType"
        :locale="lang"
        :min="minDate"
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
        return 'uiIconDatePicker';
      },
    },
    periodType: {
      type: String,
      default: function() {
        return 'date';
      },
    },
    minValue: {
      type: String,
      default: function() {
        return null;
      },
    },
    value: {
      type: String,
      default: function() {
        return null;
      },
    },
    minValueErrorMessage: {
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
    disabled: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    required: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    returnIso: {
      type: Boolean,
      default: function() {
        return false;
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
    minDate: null,
    dateFormatted: null,
    dateValue: null,
    menu: false,
  }),
  computed: {
    minDate() {
      if (this.minValue) {
        const dateObj = DateUtil.getDateObjectFromString(this.minValue, true);
        return DateUtil.getISODate(dateObj);
      } else {
        return null;
      }
    }
  },
  watch: {
    value(newVal, oldVal) {
      if (oldVal !== newVal) {
        this.computeDate();
      }
    },
    disabled() {
      this.emitDateValue();
    },
    date() {
      this.emitDateValue();
    },
  },
  mounted() {
    $(document).on('click', (e) => {
      if (e.target && !$(e.target).parents('.v-menu__content').length) {
        this.menu = false;
      }
    });

    this.computeDate();
  },
  methods: {
    emitDateValue() {
      const dateObj = this.date && new Date(this.date) || null;
      if (this.disabled) {
        this.dateValue = null;
      } else {
        if (this.returnIso) {
          this.dateValue = this.date;
        } else {
          this.dateValue = dateObj && dateObj.getTime() || null;
        }
      }
      if (this.dateValue) {
        this.dateFormatted = DateUtil.formatDateObjectToDisplay(dateObj, this.format, this.lang);
      } else {
        this.dateFormatted = null;
      }
      this.$emit('input', this.dateValue);
    },
    computeDate() {
      if (this.value && String(this.value).trim()) {
        const dateObj = DateUtil.getDateObjectFromString(String(this.value).trim(), true);
        this.date = DateUtil.getISODate(dateObj);
      } else {
        this.date = DateUtil.getISODate(new Date());
      }
    },
  },
};
</script>

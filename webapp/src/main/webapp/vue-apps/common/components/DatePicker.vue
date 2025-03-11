<template>
  <v-flex
    :id="id"
    class="datePickerComponent">
    <v-menu
      ref="selectDateMenu"
      v-model="menu"
      :allow-overflow="allowOverflow"
      :attach="attach"
      class="datePickerMenu"
      :close-on-content-click="false"
      :content-class="menuId"
      :disabled="disabled"
      eager
      :left="left"
      offset-y
      :top="top"
      transition="scale-transition">
      <template #activator="{ on }">
        <input
        
        
          v-model="dateFormatted"
          class="ignore-vuetify-classes datePickerText flex-grow-0"
          :disabled="disabled"
          :placeholder="placeholder"
          readonly
          :required="required"
          type="text"
          v-on="on">
      </template>
      <v-date-picker
        v-model="date"
        class="border-box-sizing"
        :first-day-of-week="1"
        :locale="lang"
        :max="maxDate"
        :min="minDate"
        :type="periodType"
        @input="menu = false">
        <template v-if="$slots.footer">
          <slot name="footer"></slot>
        </template>
      </v-date-picker>
    </v-menu>
  </v-flex>
</template>

<script>

  export default {
    props: {
      placeholder: {
        type: String,
        default () {
          return null;
        },
      },
      prependIcon: {
        type: String,
        default () {
          return 'uiIconDatePicker';
        },
      },
      periodType: {
        type: String,
        default () {
          return 'date';
        },
      },
      minValue: {
        type: String,
        default () {
          return null;
        },
      },
      maxValue: {
        type: String,
        default () {
          return null;
        },
      },
      value: {
        type: String,
        default () {
          return null;
        },
      },
      minValueErrorMessage: {
        type: String,
        default () {
          return null;
        },
      },
      lang: {
        type: String,
        default () {
          return eXo.env.portal.language;
        },
      },
      disabled: {
        type: Boolean,
        default () {
          return false;
        },
      },
      required: {
        type: Boolean,
        default () {
          return false;
        },
      },
      returnIso: {
        type: Boolean,
        default () {
          return false;
        },
      },
      top: {
        type: Boolean,
        default () {
          return false;
        },
      },
      left: {
        type: Boolean,
        default () {
          return false;
        },
      },
      attach: {
        type: Boolean,
        default () {
          return true;
        },
      },
      allowOverflow: {
        type: Boolean,
        default () {
          return true;
        },
      },
      format: {
        type: Object,
        default () {
          return {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
          };
        },
      },
      defaultValue: {
        type: Boolean,
        default: true,
      },
    },
    data: () => ({
      id: `DatePicker${parseInt(Math.random() * 10000)}`,
      menuId: `DatePickerMenu${parseInt(Math.random() * 10000)}`,
      date: null,
      dateFormatted: null,
      dateValue: null,
      menu: false,
    }),
    computed: {
      minDate () {
        if (this.minValue) {
          const dateObj = this.$dateUtil.getDateObjectFromString(this.minValue, true);
          return this.$dateUtil.getISODate(dateObj);
        } else {
          return null;
        }
      },
      maxDate () {
        if (this.maxValue) {
          const dateObj = this.$dateUtil.getDateObjectFromString(this.maxValue, true);
          return this.$dateUtil.getISODate(dateObj);
        } else {
          return null;
        }
      },
    },
    watch: {
      value (newVal, oldVal) {
        if (oldVal !== newVal) {
          this.computeDate();
        }
      },
      disabled () {
        this.emitDateValue();
      },
      date () {
        this.emitDateValue();
      },
    },
    mounted () {
      // Force to close other DatePicker menus when opening a new one 
      $('.datePickerComponent input').on('click', e => {
        if (e.target && !$(e.target).parents(`#${this.id}`).length) {
          this.menu = false;
        }
      });

      // Force to close DatePickers when clicking outside
      $(document).on('click', e => {
        if (e.target && !$(e.target).parents(`.${this.menuId}`).length) {
          this.menu = false;
        }
      });

      this.computeDate();
    },
    methods: {
      emitDateValue () {
        const dateObj = this.date && this.$dateUtil.getDateObjectFromString(this.date, true) || null;
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
          this.dateFormatted = this.$dateUtil.formatDateObjectToDisplay(dateObj, this.format, this.lang);
        } else {
          this.dateFormatted = null;
        }
        this.$emit('input', this.dateValue);
      },
      computeDate () {
        if (this.value && String(this.value).trim()) {
          const dateObj = this.$dateUtil.getDateObjectFromString(String(this.value).trim(), true);
          this.date = this.$dateUtil.getISODate(dateObj);
        } else {
          if ( this.defaultValue ) {
            this.date = this.$dateUtil.getISODate(new Date());
          } else {
            this.date = null;
          }
        }
      },
    },
  };
</script>

<!--
  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-menu
    v-model="menu"
    :content-class="`${menuId} white select-period-menu-content`"
    :close-on-content-click="false"
    class="select-period-menu"
    transition="scale-transition"
    :left="left"
    :right="right"
    offset-y
    attach
    min-width="auto">
    <template #activator="{ on, attrs }">
      <v-text-field
        :value="rangeDate"
        :title="rangeDateTimeTitle"
        :placeholder="placeholder"
        prepend-inner-icon="fa-calendar-alt fa-lg"
        class="select-period-input pt-0 mt-0"
        rel="tooltip"
        hide-details
        readonly
        v-bind="attrs"
        v-on="on" />
    </template>
    <div class="d-flex flex-column">
      <v-date-picker
        v-model="dates"
        :locale="lang"
        :max="maxDate"
        width="100%"
        show-current
        show-week
        first-day-of-week="1"
        class="select-period-date-picker pb-2"
        range
        scrollable
        @input="selectCustomDates" />
      <v-divider />
      <v-btn-toggle
        v-model="selectedPeriodName"
        class="d-flex flex-wrap justify-space-between select-period-selection"
        tile
        color="primary"
        background-color="primary"
        group>
        <v-btn
          v-for="(item, index) in periodOptions"
          :key="index"
          :value="item.value"
          class="ma-0"
          small
          @click="selectPeriodItem(item.value)">
          <div>{{ item.text }}</div>
        </v-btn>
      </v-btn-toggle>
      <v-divider />
      <div class="select-period-date-time-selection caption mx-auto pa-2 text-center muted">
        <span class="text-capitalize">{{ labels.from || 'From' }}</span>
        <span class="primary--text">{{ fromDateTitle }}</span>
        <input
          v-if="!hideTime"
          v-model="fromTime"
          name="fromTime"
          type="time"
          class="ignore-vuetify-classes select-period-time-picker"
          @change="updatePeriodTime">
        <span class="text-capitalize">{{ labels.to || 'To' }}</span>
        <span class="primary--text">{{ toDateTitle }}</span>
        <input
          v-if="!hideTime"
          v-model="toTime"
          name="toTime"
          type="time"
          class="ignore-vuetify-classes select-period-time-picker"
          @change="updatePeriodTime">
      </div>
    </div>
  </v-menu>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    labels: {
      type: Object,
      default: () => ({}),
    },
    hideTime: {
      type: Boolean,
      default: false,
    },
    right: {
      type: Boolean,
      default: false,
    },
    left: {
      type: Boolean,
      default: false,
    },
    placeholder: {
      type: String,
      default: null,
    },
    defaultPeriod: {
      type: String,
      default: 'thisMonth',
    },
  },
  data() {
    return {
      menu: false,
      lang: eXo.env.portal.language && eXo.env.portal.language.replace('_', '-'),
      dates: [],
      fromTime: '00:00:00',
      toTime: '23:59:59',
      selectedPeriodName: this.defaultPeriod,
      menuId: `SelectPeriodMenu${parseInt(Math.random() * 10000).toString()}`,
      shortDateFormat: {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      },
      dateFormat: {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      },
    };
  },
  computed: {
    periodOptions() {
      return [
        { value: 'thisYear',     text: this.labels.thisYear     || 'This year' },
        { value: 'thisSemester', text: this.labels.thisSemester || 'This semester' },
        { value: 'thisQuarter',  text: this.labels.thisQuarter  || 'This quarter' },
        { value: 'thisMonth',    text: this.labels.thisMonth    || 'This month' },
        { value: 'thisWeek',     text: this.labels.thisWeek     || 'This week' },
        { value: 'today',        text: this.labels.today        || 'Today' },
      ];
    },
    fromDate() {
      return this.value && new Date(this.value.min);
    },
    toDate() {
      return this.value && new Date(this.value.max);
    },
    maxDate() {
      return this.toLocalISOString(new Date()).slice(0, 10);
    },
    maxDateTime() {
      const today = new Date();
      today.setHours(23);
      today.setMinutes(59);
      return today;
    },
    fromDateTitle() {
      return this.fromDate && this.fromDate.toLocaleString(this.lang, this.dateFormat);
    },
    toDateTitle() {
      return this.toDate && this.toDate.toLocaleString(this.lang, this.dateFormat);
    },
    rangeDateTimeTitle() {
      return `${this.fromFullDateFormat} ~ ${this.toFullDateFormat}`;
    },
    rangeDate() {
      if (!this.fromDate || !this.toDate) {
        return '';
      }
      return `${this.fromDate.toLocaleString(this.lang, this.dateFormat)}~${this.toDate.toLocaleString(this.lang, this.dateFormat)}`;
    },
    fromFullDateFormat() {
      return this.fromDate && this.fromDate.toLocaleString(this.lang, this.shortDateFormat);
    },
    toFullDateFormat() {
      return this.toDate && this.toDate.toLocaleString(this.lang, this.shortDateFormat);
    },
  },
  watch: {
    menu(newVal, oldVal) {
      if (newVal && !oldVal && newVal !== oldVal) {
        if (this.value) {
          this.dates = [
            this.fromDate.toLocaleDateString(),
            this.toDate.toLocaleDateString(),
          ];
          this.fromTime = this.fromDate.toLocaleTimeString().substring(0, 5);
          this.toTime = this.toDate.toLocaleTimeString().substring(0, 5);
        }
      }
    },
  },
  mounted() {
    $(document).on('click', (e) => {
      if (e.target && !$(e.target).parents(`.${this.menuId}`).length) {
        this.close();
      }
    });
    this.selectPeriodItem(this.selectedPeriodName);
  },
  methods: {
    toLocalISOString(date) {
      const offset = date.getTimezoneOffset();
      const localDate = new Date(date.getTime() - (offset * 60 * 1000));
      const isoStr = localDate.toISOString().slice(0, -1);
      if (offset === 0) {
        return `${isoStr}Z`;
      } else {
        const sign = offset > 0 ? '-' : '+';
        const absOffset = Math.abs(offset);
        const hours = String(Math.floor(absOffset / 60)).padStart(2, '0');
        const minutes = String(absOffset % 60).padStart(2, '0');
        return `${isoStr}${sign}${hours}:${minutes}`;
      }
    },
    selectDates() {
      const selectedPeriod = {};
      if (this.selectedPeriodName) {
        selectedPeriod.period = this.selectedPeriodName;

        this.fromTime = '00:00:00';
        this.toTime = '23:59:59';

        const today = new Date();

        switch (this.selectedPeriodName) {
        case 'today': {
          const todayDate = this.toLocalISOString(today).slice(0, 10);
          this.dates = [todayDate, todayDate];
          break;
        }
        case 'thisWeek': {
          const day = today.getDay();
          const diff = today.getDate() - day + (day === 0 && -6 || 1);
          const startOfWeek = new Date(new Date().setDate(diff));
          let endOfWeek = new Date(new Date(startOfWeek).setDate(startOfWeek.getDate() + 6));
          if (endOfWeek > this.maxDateTime) {
            endOfWeek = this.maxDateTime;
          }
          this.dates = [
            this.toLocalISOString(startOfWeek).slice(0, 10),
            this.toLocalISOString(endOfWeek).slice(0, 10),
          ];
          break;
        }
        case 'thisMonth': {
          const startOfMonth = new Date(new Date().setDate(1));
          let endOfMonth = new Date(new Date(new Date().setMonth(today.getMonth() + 1)).setDate(0));
          if (endOfMonth > this.maxDateTime) {
            endOfMonth = this.maxDateTime;
          }
          this.dates = [
            this.toLocalISOString(startOfMonth).slice(0, 10),
            this.toLocalISOString(endOfMonth).slice(0, 10),
          ];
          break;
        }
        case 'thisQuarter': {
          const startOfQuarterMonth = today.getMonth() - (today.getMonth() - 1) % 3 - 1;
          const startOfQuarter = new Date(new Date(new Date().setMonth(startOfQuarterMonth)).setDate(1));
          let endOfQuarter = new Date(new Date(new Date(startOfQuarter).setMonth(startOfQuarterMonth + 3)).setDate(0));
          if (endOfQuarter > this.maxDateTime) {
            endOfQuarter = this.maxDateTime;
          }
          this.dates = [
            this.toLocalISOString(startOfQuarter).slice(0, 10),
            this.toLocalISOString(endOfQuarter).slice(0, 10),
          ];
          break;
        }
        case 'thisSemester': {
          const startOfSemesterMonth = today.getMonth() - (today.getMonth() - 1) % 6 - 1;
          const startOfSemester = new Date(new Date(new Date().setMonth(startOfSemesterMonth)).setDate(1));
          let endOfSemester = new Date(new Date(new Date(startOfSemester).setMonth(startOfSemesterMonth + 6)).setDate(0));
          if (endOfSemester > this.maxDateTime) {
            endOfSemester = this.maxDateTime;
          }
          this.dates = [
            this.toLocalISOString(startOfSemester).slice(0, 10),
            this.toLocalISOString(endOfSemester).slice(0, 10),
          ];
          break;
        }
        case 'thisYear': {
          this.dates = [
            `${today.getYear() + 1900}-01-01`,
            this.maxDate,
          ];
          break;
        }
        }
      }
      if (this.dates && this.dates.length === 2) {
        if (new Date(this.dates[0]) > new Date(this.dates[1])) {
          const tmp = this.dates[0];
          this.dates[0] = this.dates[1];
          this.dates[1] = tmp;
        }
        selectedPeriod.min = new Date(`${this.dates[0]}T${this.fromTime || '00:00'}`).getTime();
        selectedPeriod.max = new Date(`${this.dates[1]}T${this.toTime || '23:59:59'}.999`).getTime();
        this.$emit('input', selectedPeriod);
        return true;
      }
      return false;
    },
    selectCustomDates() {
      this.selectedPeriodName = null;
      this.fromTime = '00:00:00';
      this.toTime = '23:59:59';
      this.selectDates();
    },
    updatePeriodTime() {
      this.selectedPeriodName = null;
      this.selectDates();
    },
    selectPeriodItem(periodName) {
      if (periodName) {
        this.selectedPeriodName = periodName;
        const periodChanged = this.selectDates();
        if (periodChanged) {
          this.close();
        }
      }
    },
    close() {
      this.$nextTick().then(() => {
        this.menu = false;
      });
    },
  },
};
</script>

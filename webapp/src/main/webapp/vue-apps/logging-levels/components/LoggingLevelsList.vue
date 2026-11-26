<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <div class="px-5">
    <v-simple-table
      v-if="loggers.length"
      ref="table"
      class="transparent"
      dense>
      <thead>
        <tr>
          <th scope="col" class="text-start">{{ $t('loggingLevels.column.logger') }}</th>
          <th scope="col" class="text-start">{{ $t('loggingLevels.column.level') }}</th>
          <th scope="col" class="text-start hidden-xs-only">{{ $t('loggingLevels.column.logbackXml') }}</th>
          <th scope="col" class="text-end">{{ $t('loggingLevels.column.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="logger in loggers"
          :key="`${logger.name}-${version}`">
          <td class="text-break font-weight-bold">{{ logger.name }}</td>
          <td class="py-1" style="width: 140px">
            <v-select
              :value="logger.level || logger.effectiveLevel"
              :items="levels"
              :aria-label="$t('loggingLevels.levelOf', {0: logger.name})"
              :title="logger.editable ? null : $t('loggingLevels.notEditable')"
              :disabled="loading || readOnly || !logger.editable"
              class="pt-0 font-weight-bold"
              hide-details
              dense
              @change="$emit('level-changed', logger, $event)" />
          </td>
          <td class="hidden-xs-only">
            <v-chip
              v-if="logger.overridden"
              class="text-color"
              small>
              {{ $t('loggingLevels.changed', {0: logger.configuredLevel || $t('loggingLevels.inherited')}) }}
            </v-chip>
            <span
              v-if="logger.revertsAt"
              class="text-sub-title text-caption ms-2">
              {{ $t('loggingLevels.revertsAt', {0: formatTime(logger.revertsAt)}) }}
            </span>
            <span v-else-if="!logger.editable" class="text-sub-title text-caption">
              {{ $t('loggingLevels.notEditable') }}
            </span>
            <span v-else class="text--secondary">
              {{ logger.level || $t('loggingLevels.inherited') }}
            </span>
          </td>
          <td class="text-end">
            <v-btn
              v-if="logger.overridden"
              :title="$t('loggingLevels.reset')"
              :aria-label="$t('loggingLevels.resetOf', {0: logger.name})"
              :disabled="loading || readOnly"
              icon
              small
              @click="$emit('reset', logger)">
              <v-icon size="16">fa-undo</v-icon>
            </v-btn>
          </td>
        </tr>
      </tbody>
    </v-simple-table>
    <div v-else-if="!loading" class="d-flex justify-center text-header full-width my-8">
      {{ $t('loggingLevels.noData') }}
    </div>
    <v-btn
      v-if="loggers.length < total"
      :loading="loading"
      class="btn mt-4 px-0 width-full"
      @click="$emit('load-more')">
      {{ $t('loggingLevels.loadMore') }}
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    loggers: {
      type: Array,
      default: () => [],
    },
    loading: {
      type: Boolean,
      default: false,
    },
    total: {
      type: Number,
      default: 0,
    },
    // the levels are listed, no change is accepted (meeds.logging.levels.readOnly)
    readOnly: {
      type: Boolean,
      default: false,
    },
    // changes with every answer of the server, so a row is drawn again (a level
    // picked in a select that the server refused is not kept on screen)
    version: {
      type: Number,
      default: 0,
    },
  },
  data: () => ({
    levels: ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'OFF'],
  }),
  mounted() {
    this.nameTable();
  },
  updated() {
    this.nameTable();
  },
  methods: {
    formatTime(time) {
      const date = new Date(time);
      const language = eXo?.env?.portal?.language || 'en';
      // the day too when it is not today: a time alone reads as today's
      if (date.toDateString() === new Date().toDateString()) {
        return date.toLocaleTimeString(language, {hour: '2-digit', minute: '2-digit'});
      }
      return date.toLocaleString(language, {dateStyle: 'short', timeStyle: 'short'});
    },
    nameTable() {
      // Vuetify puts the aria-label of a table component on a wrapper div: the <table> itself is named here
      this.$refs.table?.$el?.querySelector?.('table')?.setAttribute?.('aria-label', this.$t('loggingLevels.title'));
    },
  },
};
</script>

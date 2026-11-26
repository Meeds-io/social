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
  <v-app>
    <v-card class="application-body position-static pb-5" flat>
      <h4 class="text-title px-5 pt-5 ma-0">
        {{ $t('loggingLevels.title') }}
      </h4>
      <logging-levels-alerts
        :enabled="enabled"
        :forbidden="forbidden"
        :read-only="readOnly"
        :revert-after-minutes="revertAfterMinutes" />
      <logging-levels-toolbar
        v-if="usable"
        :read-only="readOnly"
        @filter-changed="query = $event"
        @filter-changed-end-typing="search"
        @add="$refs.addDrawer.open()"
        @reset-all="$refs.resetAllDialog.open()" />
      <div v-if="usable" class="d-flex align-center px-5 pt-1">
        <v-switch
          v-model="explicitOnly"
          :label="$t('loggingLevels.explicitOnly')"
          class="my-0 pt-0"
          hide-details
          dense
          @change="search()" />
        <v-spacer />
        <span class="text-caption text--secondary">
          {{ $t('loggingLevels.count', {0: loggers.length, 1: total}) }}
        </span>
      </div>
      <logging-levels-list
        v-if="usable"
        :read-only="readOnly"
        :loggers="loggers"
        :loading="loading"
        :total="total"
        :version="listVersion"
        @level-changed="setLevel"
        @reset="resetLevel"
        @load-more="loadMore" />
    </v-card>
    <logging-level-add-drawer
      ref="addDrawer"
      @saved="search" />
    <exo-confirm-dialog
      ref="resetAllDialog"
      :title="$t('loggingLevels.resetAll.title')"
      :message="$t('loggingLevels.resetAll.message')"
      :ok-label="$t('loggingLevels.confirm')"
      :cancel-label="$t('loggingLevels.cancel')"
      @ok="resetAll" />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    loggers: [],
    total: 0,
    query: null,
    explicitOnly: true,
    loading: false,
    pageSize: 20,
    loadCount: 0,
    listVersion: 0,
    appliedQuery: null,
    appliedExplicitOnly: true,
    enabled: true,
    readOnly: false,
    forbidden: false,
    revertAfterMinutes: 0,
  }),
  computed: {
    usable() {
      return this.enabled && !this.forbidden;
    },
  },
  async created() {
    await this.loadStatus();
    if (this.usable) {
      this.search();
    }
  },
  methods: {
    async loadStatus() {
      try {
        const status = await this.$loggingLevelService.getStatus();
        this.enabled = status.enabled;
        this.readOnly = status.readOnly;
        this.revertAfterMinutes = status.revertAfterMinutes || 0;
      } catch (e) {
        if (e?.status === 403) {
          // a role that is not allowed is refused by the server
          this.forbidden = true;
        } else {
          this.error('loggingLevels.loadError');
        }
      }
    },
    search() {
      // "Load more" goes on with the query that made the list, not the one being typed
      this.appliedQuery = this.query;
      this.appliedExplicitOnly = this.explicitOnly;
      return this.load(0);
    },
    loadMore() {
      return this.load(this.loggers.length);
    },
    async load(offset) {
      // an answer that comes after a newer request is dropped
      const loadId = ++this.loadCount;
      this.loading = true;
      try {
        const page = await this.$loggingLevelService.getLoggers(this.options(offset));
        if (loadId === this.loadCount) {
          // a change moves rows between two pages: a row is shown once
          const known = new Set(offset ? this.loggers.map(logger => logger.name) : []);
          const added = page.loggers.filter(logger => !known.has(logger.name));
          this.loggers = offset ? this.loggers.concat(added) : added;
          this.total = page.total;
          this.listVersion++;
        }
      } catch (e) {
        if (loadId === this.loadCount) {
          this.error('loggingLevels.loadError');
        }
      } finally {
        if (loadId === this.loadCount) {
          this.loading = false;
        }
      }
    },
    options(offset) {
      return {
        query: this.appliedQuery?.trim?.(),
        explicitOnly: this.appliedExplicitOnly,
        offset,
        limit: this.pageSize,
      };
    },
    async setLevel(logger, level) {
      await this.run(() => this.$loggingLevelService.setLevel(logger.name, level), 'loggingLevels.levelSet', 'loggingLevels.setError');
    },
    async resetLevel(logger) {
      await this.run(() => this.$loggingLevelService.resetLevel(logger.name), 'loggingLevels.levelReset', 'loggingLevels.resetError');
    },
    async resetAll() {
      await this.run(() => this.$loggingLevelService.resetAll(), 'loggingLevels.allReset', 'loggingLevels.resetError');
    },
    async run(action, successKey, errorKey) {
      // the rows and their buttons wait for the answer: no second request for the same change
      this.loading = true;
      try {
        await action();
        this.$root.$emit('alert-message', this.$t(successKey), 'success');
      } catch (e) {
        this.error(e?.message === 'logging.overrides.tooMany' ? 'loggingLevels.tooMany' : errorKey);
        // draws the rows again at once: a level picked in a select that was refused is not kept on screen
        this.listVersion++;
      }
      await this.search();
    },
    error(key) {
      this.$root.$emit('alert-message', this.$t(key), 'error');
    },
  },
};
</script>

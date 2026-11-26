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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    class="loggingLevelAddDrawer"
    right>
    <template #title>
      {{ $t('loggingLevels.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-5">
        <div class="mb-2 text-header">{{ $t('loggingLevels.drawer.name') }}</div>
        <v-text-field
          v-model="name"
          :placeholder="$t('loggingLevels.drawer.namePlaceholder')"
          :rules="nameRules"
          class="pt-0"
          maxlength="200"
          outlined
          dense
          @keyup.enter="valid && !saving && save()" />
        <div class="mb-2 text-header">{{ $t('loggingLevels.drawer.level') }}</div>
        <v-select
          v-model="level"
          :items="levels"
          class="pt-0"
          outlined
          dense />
        <v-alert
          v-if="trimmedName && broad && verbose"
          type="warning"
          dense
          outlined>
          <div class="text-color">
            {{ $t('loggingLevels.drawer.broadWarning') }}
          </div>
        </v-alert>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('loggingLevels.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!valid"
          :loading="saving"
          class="btn btn-primary"
          @click="save">
          {{ $t('loggingLevels.drawer.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    saving: false,
    name: '',
    level: 'DEBUG',
    levels: ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'OFF'],
  }),
  computed: {
    trimmedName() {
      return this.name?.trim?.() || '';
    },
    valid() {
      return /^[A-Za-z_$][A-Za-z0-9_$-]*(\.[A-Za-z_$][A-Za-z0-9_$-]*)*$/.test(this.trimmedName) || this.trimmedName === 'ROOT';
    },
    nameRules() {
      return [() => !this.trimmedName || this.valid || this.$t('loggingLevels.drawer.nameInvalid')];
    },
    broad() {
      return this.trimmedName === 'ROOT' || this.trimmedName.split('.').length < 2;
    },
    verbose() {
      return this.level === 'TRACE' || this.level === 'DEBUG';
    },
  },
  methods: {
    open() {
      this.name = '';
      this.level = 'DEBUG';
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    async save() {
      this.saving = true;
      try {
        await this.$loggingLevelService.setLevel(this.trimmedName, this.level);
        this.$root.$emit('alert-message', this.$t('loggingLevels.levelSet'), 'success');
        this.$emit('saved');
        this.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t(e?.message === 'logging.overrides.tooMany' ? 'loggingLevels.tooMany' : 'loggingLevels.setError'), 'error');
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>

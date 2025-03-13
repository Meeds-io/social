<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <div
    class="border-color-transparent"
    :class="{ 'border-primary-dashed': isDragging }"
    @dragenter.prevent="onDragEnter"
    @dragleave="onDragLeave"
    @dragover.prevent="onDragOver"
    @drop="handleDrop">
    <v-data-table
      class="px-5"
      dense
      :headers="filteredHeaders"
      hide-default-footer
      item-class="py-2 align-center"
      item-key="id"
      :items="imageList"
      :items-per-page="-1"
      :loading="loading"
      sort-by="creationDate"
      sort-desc>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.thumbnail="{ item }">
        <v-progress-circular
          v-if="item?.progress < 100"
          color="primary"
          :rotate="-90"
          :size="35"
          :value="item.progress"
          :width="3">
          <span class="caption">
            {{ `${item.progress}%` }}
          </span>
        </v-progress-circular>
        <v-img
          v-else-if="item.thumbnail"
          :alt="item.name"
          aspect-ratio="2"
          class="clickable"
          contain
          max-height="26"
          max-width="61"
          :src="item.thumbnail"
          @click="$emit('open-preview', item)" />
      </template>
      <template #item.name="{ item }">
        <p
          class="text-truncate-2 mb-0 clickable"
          @click="$emit('open-preview', item)">
          {{ item.name }}
        </p>
      </template>
      <template #item.creationDate="{ item }">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <span
              v-bind="attrs"
              v-on="on">
              <date-format
                :format="dateFormat"
                :value="item.creationDate" />
            </span>
          </template>
          <span>
            {{ formatDate(item.creationDate) }}
          </span>
        </v-tooltip>
      </template>
      <template #item.size="{ item }">
        {{ formatFileSize(item.size) }}
      </template>
      <template #item.actions="{ item }">
        <div class="d-flex py-1">
          <v-tooltip bottom>
            <template #activator="{ on, attrs }">
              <v-btn
                v-bind="attrs"
                :aria-label="$t('simpleStorage.copyLink.label')"
                class="icon-default-color"
                :disabled="!item.id"
                icon
                v-on="on"
                @click="$emit('copy-link', item.id)">
                <v-icon size="20">
                  fas fa-link
                </v-icon>
              </v-btn>
            </template>
            {{ $t('simpleStorage.copyLink.label') }}
          </v-tooltip>
          <v-tooltip bottom>
            <template #activator="{ on, attrs }">
              <v-btn
                v-bind="attrs"
                :aria-label="$t('simpleStorage.delete.label') "
                class="error-color"
                :disabled="!item.id"
                icon
                v-on="on"
                @click="$emit('delete', item)">
                <v-icon
                  size="20">
                  fas fa-trash
                </v-icon>
              </v-btn>
            </template>
            {{ $t('simpleStorage.delete.label') }}
          </v-tooltip>
        </div>
      </template>
    </v-data-table>
  </div>
</template>

<script>
  export default {
    props: {
      imageList: {
        type: Array,
        default: null,
      },
      loading: {
        type: Boolean,
        default: false,
      },
    },
    data () {
      return {
        lang: eXo.env.portal.language,
        headers: [
          { value: 'thumbnail', sortable: false, align: 'center', width: '60' },
          { text: this.$t('simpleStorage.name.label'), value: 'name' },
          { text: this.$t('simpleStorage.creationDate.label'), value: 'creationDate', width: '18%' },
          { text: this.$t('simpleStorage.size.label'), value: 'size', width: '8%' },
          { text: this.$t('simpleStorage.actions.label'), value: 'actions', sortable: false, width: '8%' },
        ],
        minimalColumns: ['name', 'creationDate', 'actions'],
        dateHourFormat: {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
        },
        dateFormat: {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
        },
        isDragging: false,
      };
    },
    computed: {
      small () {
        return eXo.vuetify.display.width.value < eXo.vuetify.display.thresholds.value.md;
      },
      filteredHeaders () {
        return this.small && this.headers.filter(header => this.minimalColumns.includes(header.value)) || this.headers;
      },
    },
    methods: {
      onDragEnter () {
        this.isDragging = true;
      },
      onDragOver (event) {
        event.preventDefault();
        this.isDragging = true;
      },
      onDragLeave () {
        this.isDragging = false;
      },
      handleDrop (event) {
        event.preventDefault();
        const files = Array.from(event.dataTransfer.files);
        this.isDragging = false;
        this.$root.$emit('handle-upload-images', files);
      },
      formatDate (date) {
        return new Date(date).toLocaleString(this.lang, this.dateHourFormat);
      },
      formatFileSize (sizeInBytes) {
        return sizeInBytes >= 1_048_576
          ? `${parseFloat((sizeInBytes / 1_048_576).toFixed(1))} ${this.$t('simpleStorage.size.MB.label')}`
          : `${parseFloat((sizeInBytes / 1_024).toFixed(1))} ${this.$t('simpleStorage.size.KB.label')}`;
      },
    },
  };
</script>

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
    @dragover.prevent="onDragOver"
    @dragenter.prevent="onDragEnter"
    @dragleave="onDragLeave"
    @drop="handleDrop">
    <v-data-table
      :headers="filteredHeaders"
      :items="imageList"
      :loading="loading"
      item-key="id"
      class="px-5"
      item-class="py-2 align-center"
      :items-per-page="-1"
      hide-default-footer
      dense>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.thumbnail="{ item }">
        <v-progress-circular
          v-if="item?.progress < 100"
          :rotate="-90"
          :size="35"
          :width="3"
          :value="item.progress"
          color="primary">
          <span class="caption">
            {{ `${item.progress}%` }}
          </span>
        </v-progress-circular>
        <v-img
          v-else-if="item.thumbnail"
          :src="item.thumbnail"
          :alt="item.name"
          class="clickable"
          aspect-ratio="2"
          max-width="61"
          max-height="26"
          contain
          @click="$emit('open-preview', item)" />
      </template>
      <template #item.name="{ item }">
        <p class="text-truncate-2 mb-0">
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
                :value="item.creationDate"
                :format="dateFormat" />
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
      <template #item.actions>
        <div class="d-flex py-1">
          <v-btn
            class="icon-default-color"
            icon>
            <v-icon size="20">
              fas fa-link
            </v-icon>
          </v-btn>
          <v-btn
            class="error-color"
            icon>
            <v-icon
              size="20">
              fas fa-trash-alt
            </v-icon>
          </v-btn>
        </div>
      </template>
    </v-data-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      lang: eXo.env.portal.language,
      headers: [
        {value: 'thumbnail', sortable: false, align: 'center', width: '60'},
        {text: this.$t('simpleStorage.name.label'), value: 'name'},
        {text: this.$t('simpleStorage.creationDate.label'), value: 'creationDate', width: '18%'},
        {text: this.$t('simpleStorage.size.label'), value: 'size', width: '8%'},
        {text: this.$t('simpleStorage.actions.label'), value: 'actions', sortable: false, width: '8%'},
      ],
      minimalColumns: ['name', 'creationDate', 'actions'],
      dateHourFormat: {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      },
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
      isDragging: false
    };
  },
  props: {
    imageList: {
      type: Array,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    small() {
      return this.$vuetify.breakpoint.mdAndDown;
    },
    filteredHeaders() {
      return this.small && this.headers.filter(header => this.minimalColumns.includes(header.value)) || this.headers;
    }
  },
  methods: {
    onDragEnter() {
      this.isDragging = true;
    },
    onDragOver(event) {
      event.preventDefault();
      this.isDragging = true;
    },
    onDragLeave() {
      this.isDragging = false;
    },
    handleDrop(event) {
      event.preventDefault();
      const files = Array.from(event.dataTransfer.files).filter(file => file.type.startsWith('image/'));
      this.isDragging = false;
      this.$root.$emit('handle-upload-images', files);
    },
    formatDate(date) {
      return new Date(date).toLocaleString(this.lang, this.dateHourFormat);
    },
    formatFileSize(sizeInBytes) {
      return sizeInBytes >= 1_048_576
        ? `${parseFloat((sizeInBytes / 1_048_576).toFixed(1))} ${this.$t('simpleStorage.size.MB.label')}`
        : `${parseFloat((sizeInBytes / 1_024).toFixed(1))} ${this.$t('simpleStorage.size.KB.label')}`;
    }
  }
};
</script>

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
  <v-card
    flat
    class="pa-0"
    :aria-label="$t('search.access.to.result', {0 :connectorName})">
    <v-list class="pa-0">
      <v-list-item>
        <v-list-item-icon class="me-2">
          <v-sheet class="d-flex align-center justify-center" width="32">
            <v-icon size="32" class=" icon-default-color mt-2">{{ connectorIcon }}</v-icon>
          </v-sheet>
        </v-list-item-icon>

        <v-list-item-content>
          <v-list-item-title class="d-flex flex-row full-width align-center pt-2">
            <h1
              class="flex-grow-1 title pt-1 mb-0 ps-0 my-auto align-center text-start text-truncate">
              {{ connectorName }}
            </h1>
          </v-list-item-title>

          <card-carousel class="d-flex">
            <div
              v-for="result in searchResults"
              :key="result.domId"
              class="pe-2">
              <search-result-card :result="result" :term="term" />
            </div>
          </card-carousel>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-card>
</template>
<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    results: {
      type: String,
      default: null,
    },
  },
  computed: {
    searchResults() {
      return this.results || [];
    },
    connector() {
      return this.searchResults.length && this.searchResults[0]?.connector || {};
    },
    connectorName() {
      return this.connector?.label;
    },
    connectorIcon() {
      return this.connector.icon;
    }
  }
};
</script>
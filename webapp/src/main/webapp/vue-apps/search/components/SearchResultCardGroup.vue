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
    class="pa-0">
    <v-list class="pa-0">
      <v-list-item>
        <v-list-item-icon class="ms-n1 me-2">
          <v-icon size="32" class="icon-default-color mt-2">{{ connectorIcon }}</v-icon>
        </v-list-item-icon>

        <v-list-item-content>
          <v-list-item-title class="d-flex flex-row full-width align-center">
            <p
              :title="connectorName"
              class="flex-grow-1 title font-weight-bold pt-1 mb-0 ps-0 my-auto align-center text-start text-truncate"
              v-sanitized-html="connectorName">
            </p>
          </v-list-item-title>

          <component :is="$root.isMobile && 'div' || 'card-carousel'" :class="$root.isMobile && 'd-flex flex-column' || 'd-flex'">
            <div
              v-for="result in searchResults"
              :key="result.domId"
              :class="$root.isMobile && 'pb-2' || 'pe-2'">
              <search-result-card :result="result" :term="term" />
            </div>
          </component>
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
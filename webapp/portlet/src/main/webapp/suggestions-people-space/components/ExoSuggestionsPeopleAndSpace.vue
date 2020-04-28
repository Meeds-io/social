<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
  <v-app v-if="spacesSuggestionsList.length > 0 || peopleSuggestionsList.length > 0" id="SuggestionsPeopleAndSpace">
    <v-flex d-flex xs12 sm12>
      <v-layout row wrap mx-0>
        <v-flex d-flex xs12>
          <v-card flat class="flex suggestions-wrapper">
            <v-card-title class="suggestions-title subtitle-1 text-uppercase pb-0">
              <span
                :class="firstLoadingSpaces && firstLoadingPeoples && 'skeleton-background skeleton-text skeleton-border-radius skeleton-header'">{{ $t('suggestions.label') }}</span>
            </v-card-title>
            <v-list v-if="peopleSuggestionsList.length > 0 && suggestionsType !== 'space'" dense class="suggestions-list people-list py-4 mx-4">
              <exo-suggestions-people-list-item
                v-for="people in peoplesToDisplay"
                :key="people.suggestionId"
                :people="people"
                :people-suggestions-list="peopleSuggestionsList"
                :skeleton="firstLoadingPeoples"/>
            </v-list>
            <v-list v-if="spacesSuggestionsList.length > 0 && suggestionsType !== 'people'" dense class="suggestions-list space-list py-4 mx-4">
              <exo-suggestions-space-list-item
                v-for="space in spacesToDisplay"
                :key="space.spaceId"
                :space="space"
                :spaces-suggestions-list="spacesSuggestionsList"
                :skeleton="firstLoadingSpaces"/>
            </v-list>
          </v-card>
        </v-flex>
      </v-layout>
    </v-flex>
  </v-app>
</template>
<script>
export default {
  props: {
    suggestionsType: {
      type: String,
      default: 'all',
    },
  },
  data () {
    return {
      peopleSuggestionsList: [],
      spacesSuggestionsList: [],
      firstLoadingSpaces: true,
      firstLoadingPeoples: true,
    };
  },
  computed : {
    displayPeopleSuggestions() {
      return !this.suggestionsType || this.suggestionsType === 'all' || this.suggestionsType === 'user';
    },
    displaySpacesSuggestions() {
      return !this.suggestionsType || this.suggestionsType === 'all' || this.suggestionsType === 'space';
    },
    peoplesToDisplay() {
      return this.peopleSuggestionsList.slice(0, 2);
    },
    spacesToDisplay() {
      return this.spacesSuggestionsList.slice(0, 2);
    }
  },
  created() {
    if (this.displayPeopleSuggestions) {
      this.initPeopleSuggestionsList();
      if (this.firstLoadingSpaces) {
        this.firstLoadingSpaces = false;
      }
    } else {
      this.firstLoadingSpaces = false;
    }
    if (this.displaySpacesSuggestions) {
      this.initSpaceSuggestionsList();
      if (this.firstLoadingPeoples) {
        this.firstLoadingPeoples = false;
      }
    } else {
      this.firstLoadingPeoples = false;
    }
  },
  methods : {
    initPeopleSuggestionsList() {
      this.$userService.getSuggestionsUsers().then(data => {
        this.peopleSuggestionsList = data.items;
      });
    },
    initSpaceSuggestionsList() {
      this.$spaceService.getSuggestionsSpace().then(data => {
        this.spacesSuggestionsList = data.items;
      });
    },
  },
};
</script>


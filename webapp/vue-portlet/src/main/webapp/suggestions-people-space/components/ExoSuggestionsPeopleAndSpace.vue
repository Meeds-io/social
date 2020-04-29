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
import * as userService from '../../common/js/UserService.js';
import * as spaceService from '../../common/js/SpaceService.js';
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
      userService.getSuggestionsUsers().then(data => {
        this.peopleSuggestionsList = data.items;
      });
    },
    initSpaceSuggestionsList() {
      spaceService.getSuggestionsSpace().then(data => {
        this.spacesSuggestionsList = data.items;
      });
    },
  },
};
</script>


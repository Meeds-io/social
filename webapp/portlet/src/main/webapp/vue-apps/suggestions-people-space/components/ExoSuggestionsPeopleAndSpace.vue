<template>
  <v-app class="hiddenable-widget">
    <v-flex
      :class="appVisibilityClass"
      xs12
      sm12>
      <v-layout
        row
        wrap
        mx-0>
        <v-flex d-flex xs12>
          <v-card flat class="flex suggestions-wrapper">
            <v-card-title class="suggestions-title subtitle-1 text-uppercase pb-0">
              <span>{{ $t('suggestions.label') }}</span>
            </v-card-title>
            <v-list
              v-if="peopleSuggestionsList.length > 0 && suggestionsType !== 'space'"
              dense
              class="suggestions-list people-list py-4 mx-4">
              <exo-suggestions-people-list-item
                v-for="people in peoplesToDisplay"
                :key="people.suggestionId"
                :people="people"
                :people-suggestions-list="peopleSuggestionsList" />
            </v-list>
            <v-list
              v-if="spacesSuggestionsList.length > 0 && suggestionsType !== 'people'"
              dense
              class="suggestions-list space-list py-4 mx-4">
              <exo-suggestions-space-list-item
                v-for="space in spacesToDisplay"
                :key="space.spaceId"
                :space="space"
                :spaces-suggestions-list="spacesSuggestionsList" />
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
      loading: 2,
    };
  },
  computed: {
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
    },
    appVisibilityClass() {
      if (!(this.spacesSuggestionsList && this.spacesSuggestionsList.length)
          && !(this.peopleSuggestionsList && this.peopleSuggestionsList.length)) {
        return 'd-none';
      }
      return 'd-flex';
    },
  },
  watch: {
    loading(newVal, oldVal) {
      if (newVal !== oldVal && !newVal) {
        this.$nextTick().then(() => this.$root.$applicationLoaded());
      }
    },
  },
  created() {
    if (this.displayPeopleSuggestions) {
      this.initPeopleSuggestionsList();
    } else {
      this.loading--;
    }
    if (this.displaySpacesSuggestions) {
      this.initSpaceSuggestionsList();
    } else {
      this.loading--;
    }
  },
  methods: {
    initPeopleSuggestionsList() {
      return this.$userService.getUserSuggestions()
        .then(data => {
          this.peopleSuggestionsList = data.items;
        })
        .finally(() => {
          this.loading--;
        });
    },
    initSpaceSuggestionsList() {
      return this.$spaceService.getSuggestionsSpace()
        .then(data => {
          this.spacesSuggestionsList = data.items;
        })
        .finally(() => {
          this.loading--;
        });
    },
  },
};
</script>
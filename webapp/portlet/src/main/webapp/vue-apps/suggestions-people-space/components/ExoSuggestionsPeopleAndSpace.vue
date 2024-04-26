<template>
  <v-app class="hiddenable-widget">
    <widget-wrapper 
      v-if="isVisible"
      :title="$t('suggestions.label')"
      :extra-class="'suggestions-wrapper'">
      <v-list
        v-if="peopleSuggestionsList.length > 0 && suggestionsType !== 'space'"
        dense
        class="suggestions-list people-list pa-0">
        <exo-suggestions-people-list-item
          v-for="people in peoplesToDisplay"
          :key="people.suggestionId"
          :people="people"
          :people-suggestions-list="peopleSuggestionsList" />
      </v-list>
      <v-divider 
        v-if="spacesSuggestionsList.length && peopleSuggestionsList.length" 
        class="my-2" />
      <v-list
        v-if="spacesSuggestionsList.length > 0 && suggestionsType !== 'people'"
        dense
        class="suggestions-list space-list pa-0">
        <exo-suggestions-space-list-item
          v-for="space in spacesToDisplay"
          :key="space.spaceId"
          :space="space"
          :spaces-suggestions-list="spacesSuggestionsList" />
      </v-list>
    </widget-wrapper>
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
    isVisible() {
      return this.spacesSuggestionsList?.length || this.peopleSuggestionsList?.length;
    },
  },
  watch: {
    loading(newVal, oldVal) {
      if (newVal !== oldVal && !newVal) {
        this.$nextTick().then(() => this.$root.$applicationLoaded());
      }
    },
    isVisible() {
      this.$root.$updateApplicationVisibility(this.isVisible);
    }
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
  mounted() {
    if (!this.isVisible) {
      this.$root.$updateApplicationVisibility(false);
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
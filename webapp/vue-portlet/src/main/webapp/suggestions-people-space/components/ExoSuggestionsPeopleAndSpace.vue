<template>
  <v-app v-if="spacesSuggestionsList.length > 0 || peopleSuggestionsList.length > 0" id="SuggestionsPeopleAndSpace">
    <v-flex d-flex xs12 sm12>
      <v-layout row wrap mx-0>
        <v-flex d-flex xs12>
          <v-card flat class="flex suggestions-wrapper">
            <v-card-title class="suggestions-title subtitle-1 text-uppercase pb-0"> {{ $t('suggestions.label') }}</v-card-title>
            <v-list v-if="peopleSuggestionsList.length > 0" dense class="suggestions-list people-list py-4 mx-4">
              <v-list-item v-for="(people, index) in getLastSuggestionsPeople"
                           :key="index"
                           :id="'people-item-'+people.suggestionId"
                           class="suggestions-list-item suggestions-people-list-item pa-0">
                <v-list-item-avatar size="37">
                  <v-img :src="people.avatar"></v-img>
                </v-list-item-avatar>
                <v-list-item-content class="pb-3">
                  <v-list-item-title class="body-2 font-weight-bold suggestions-list-item-title">{{ people.suggestionName }}</v-list-item-title>
                  <v-list-item-subtitle class="caption suggestions-list-item-subtitle">{{ people.number }} {{ $t('connection.label') }}</v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action class="suggestions-list-item-actions">
                  <v-btn-toggle class="transparent" dark>
                    <a text icon small min-width="auto" class="px-0 suggestions-btn-action connexion-accept-btn" @click="connectionRequest(people)">
                      <i class="uiIconInviteUser"></i>
                    </a>
                    <a text small min-width="auto" class="px-0 suggestions-btn-action connexion-refuse-btn" @click="ignoredConnection(currentUser, people)">
                      <i class="uiIconCloseCircled"></i>
                    </a>
                  </v-btn-toggle>
                </v-list-item-action>
              </v-list-item>
            
            </v-list>
            <v-list v-if="spacesSuggestionsList.length > 0" dense class="suggestions-list space-list py-4 mx-4">
              <v-list-item v-for="(space, index) in getLastSuggestionsSpace"
                           :key="index"
                           class="suggestions-list-item suggestions-space-list-item pa-0">
                <v-list-item-avatar tile size="37">
                  <v-img :src="space.spaceAvatarUrl"></v-img>
                </v-list-item-avatar>
                <v-list-item-content class="pb-3">
                  <v-list-item-title class="body-2 font-weight-bold suggestions-list-item-title">{{ space.displayName }}</v-list-item-title>
                  <v-list-item-subtitle class="caption suggestions-list-item-subtitle">{{ space.members }} {{ $t('spacemember.Label') }}</v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action class="suggestions-list-item-actions">
                  <v-btn-toggle class="transparent" dark>
                    <a text icon small min-width="auto" class="px-0 suggestions-btn-action connexion-accept-btn" @click="joinSpace(space)">
                      <i class="uiIconPlusLight"></i>
                    </a>
                    <a text small min-width="auto" class="px-0 suggestions-btn-action connexion-refuse-btn" @click="ignoredSuggestionSpace(space)">
                      <i class="uiIconCloseCircled"></i>
                    </a>
                  </v-btn-toggle>
                </v-list-item-action>
              </v-list-item>
            </v-list>
          </v-card>
        </v-flex>
      </v-layout>
    </v-flex>
  </v-app>
</template>
<script>
import * as suggestionsServices from '../suggestionsServices.js';
export default {
  data () {
    return {
      peopleSuggestionsList: [],
      spacesSuggestionsList: [],
      currentUser: ''
    };
  },
  computed : {
    getLastSuggestionsPeople() {
      return this.peopleSuggestionsList.slice(0, 2);
    },
    getLastSuggestionsSpace() {
      return this.spacesSuggestionsList.slice(0, 2);
    }
  },
  created() {
    this.initPeopleSuggestionsList();
    this.initSpaceSuggestionsList();
  },
  methods : {
    initPeopleSuggestionsList() {
      suggestionsServices.getPeopleSuggestions().then(data => {
        this.peopleSuggestionsList = data.items;
        this.currentUser = data.username;
      });
    },
    initSpaceSuggestionsList() {
      suggestionsServices.getSpaceSuggestions().then(data => {
        this.spacesSuggestionsList = data.items;
      });
    },
    connectionRequest(item) {
      suggestionsServices.sendConnectionRequest(item.suggestionId);
      this.peopleSuggestionsList.splice(this.peopleSuggestionsList.indexOf(item),1);
    },
    ignoredConnection(sender, receiverItem) {
      suggestionsServices.ignoredSuggestionConnection(sender, receiverItem.username);
      this.peopleSuggestionsList.splice(this.peopleSuggestionsList.indexOf(receiverItem),1);
    },
    joinSpace(item) {
      suggestionsServices.joinSpace(item.spaceId);
      this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
    },
    ignoredSuggestionSpace(item) {
      suggestionsServices.ignoredSuggestionSpace(item);
      this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
    },
  },
};
</script>


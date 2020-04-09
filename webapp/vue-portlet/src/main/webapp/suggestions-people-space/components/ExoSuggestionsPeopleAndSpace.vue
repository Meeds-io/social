<template>
  <v-app v-if="spacesSuggestionsList.length > 0 || peopleSuggestionsList.length > 0" id="SuggestionsPeopleAndSpace">
    <v-flex d-flex xs12 sm12>
      <v-layout row wrap mx-0>
        <v-flex d-flex xs12>
          <v-card flat class="flex suggestions-wrapper">
            <v-card-title class="suggestions-title subtitle-1 text-uppercase pb-0"> {{ $t('suggestions.label') }}</v-card-title>
            <v-list v-if="peopleSuggestionsList.length > 0 && suggestionsType !== 'space'" dense class="suggestions-list people-list py-4 mx-4">
              <transition-group name="fade" tag="div">
                <exo-user-avatar
                  v-for="people in peoplesToDisplay"
                  :key="people"
                  :username="people.username"
                  :fullname="people.suggestionName"
                  :size="iconSize"
                  :avatarurl="people.avatar"
                  class="mx-auto py-2">
                  <template slot="subTitle">
                    {{ people.number }} {{ $t('connection.label') }}
                  </template>
                  <template slot="actions">
                    <v-btn-toggle class="transparent" dark>
                      <a
                        text
                        icon
                        small
                        min-width="auto"
                        class="px-0 suggestions-btn-action connexion-accept-btn"
                        @click="connectionRequest(people)">
                        <i class="uiIconInviteUser"></i>
                      </a>
                      <a
                        text
                        small
                        min-width="auto"
                        class="px-0 suggestions-btn-action connexion-refuse-btn ml-2"
                        @click="ignoredConnection(currentUser, people)">
                        <i class="uiIconCloseCircled tertiary-color"></i>
                      </a>
                    </v-btn-toggle>
                  </template>
                </exo-user-avatar>
              </transition-group>
            </v-list>
            <v-list v-if="spacesSuggestionsList.length > 0 && suggestionsType !== 'people'" dense class="suggestions-list space-list py-4 mx-4">
              <transition-group name="fade" tag="div">
                <exo-user-avatar
                  v-for="space in spacesToDisplay"
                  :key="space"
                  :fullname="space.displayName"
                  :size="iconSize"
                  :avatarurl="space.spaceAvatarUrl"
                  :tile="true"
                  class="mx-auto py-2">
                  <template slot="subTitle">
                    {{ space.members }} {{ $t('spacemember.Label') }}
                  </template>
                  <template slot="actions">
                    <v-btn-toggle class="transparent" dark>
                      <a
                        text
                        icon
                        small
                        min-width="auto"
                        class="px-0 suggestions-btn-action connexion-accept-btn"
                        @click="joinSpace(space)">
                        <i class="uiIconPlusLight"></i>
                      </a>
                      <a
                        text
                        small
                        min-width="auto"
                        class="px-0 suggestions-btn-action connexion-refuse-btn ml-2"
                        @click="ignoredSuggestionSpace(space)">
                        <i class="uiIconCloseCircled tertiary-color"></i>
                      </a>
                    </v-btn-toggle>
                  </template>
                </exo-user-avatar>
              </transition-group>
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
  props: {
    suggestionsType: {
      type: String,
      default: null,
    },
  },
  data () {
    return {
      peopleSuggestionsList: [],
      spacesSuggestionsList: [],
      currentUser: ''
    };
  },
  computed : {
    peoplesToDisplay() {
      return this.peopleSuggestionsList.slice(0, 2);
    },
    spacesToDisplay() {
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
      suggestionsServices.sendConnectionRequest(item.suggestionId).then(
        ()=> {
          this.peopleSuggestionsList.splice(this.peopleSuggestionsList.indexOf(item),1);
        }
      );
    },
    ignoredConnection(sender, receiverItem) {
      suggestionsServices.ignoredSuggestionConnection(sender, receiverItem.username).then(
        () => {
          this.peopleSuggestionsList.splice(this.peopleSuggestionsList.indexOf(receiverItem),1);
        }
      );
    },
    joinSpace(item) {
      suggestionsServices.joinSpace(item.spaceId).then(
        () => {
          this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
        }
      );
    },
    ignoredSuggestionSpace(item) {
      suggestionsServices.ignoredSuggestionSpace(item).then(
        () => {
          this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
        }
      );
    },
  },
};
</script>


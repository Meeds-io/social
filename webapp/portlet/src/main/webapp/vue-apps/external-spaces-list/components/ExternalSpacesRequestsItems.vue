<template>
  <v-layout
    row
    wrap
    pa-1
    pb-1
    mx-0>
    <v-flex
      xs12>
      <v-list v-if="spacesRequests?.length">
        <template v-for="item in spacesRequests">
          <v-list-item
            :key="item.id"
            class="py-0 px-2">
            <v-list-item-avatar
              class="my-0 ps-2"
              tile>
              <v-avatar :size="avatarSize" tile>
                <v-img
                  :src="item.avatar"
                  :height="avatarSize"
                  :width="avatarSize"
                  :max-height="avatarSize"
                  :max-width="avatarSize"
                  class="mx-auto spaceAvatar"
                  role="presentation" />
              </v-avatar>
            </v-list-item-avatar>
            <v-list-item-content class="py-0">
              <v-list-item-title class="text-color text-truncate requestSpaceName" v-text="item.displayName" />
              <v-list-item-subtitle class="caption grey-color" v-text="item.description" />
            </v-list-item-content>
            <v-list-item-action>
              <v-btn-toggle
                class="transparent"
                dark>
                <v-btn
                  :title="$t('externalSpacesList.tooltip.AcceptToJoin')"
                  text
                  icon
                  small
                  min-width="auto"
                  class="px-0"
                  @click="replyInvitationToJoinSpace(item.id, 'approved')">
                  <v-icon color="green" size="20">mdi-checkbox-marked-circle</v-icon>
                </v-btn>
                <v-btn
                  :title="$t('externalSpacesList.tooltip.DeclineInvitation')"
                  text
                  icon
                  small
                  min-width="auto"
                  class="px-0"
                  @click="replyInvitationToJoinSpace(item.id, 'ignored')">
                  <v-icon color="red" size="20">mdi-close-circle</v-icon>
                </v-btn>
              </v-btn-toggle>
            </v-list-item-action>
          </v-list-item>
        </template>
      </v-list>
    </v-flex>
  </v-layout>
</template>
<script>
import * as externalSpacesListService from '../externalSpacesListService.js';

export default {
  props: {
    avatarSize: {
      type: Number,
      default: () => 37,
    },
  },
  data() {
    return {
      spacesRequests: [],
    };
  },
  created(){
    this.getSpacesRequests();
  },

  methods: {
    getSpacesRequests() {
      this.spacesRequests = [];
      externalSpacesListService.getExternalSpacesRequests().then(
        (data) => {
          for (let i = 0; i < data.spacesMemberships.length; i++) {
            const spaceRequest = {};
            spaceRequest.id = data.spacesMemberships[i].id;
            fetch(`${data.spacesMemberships[i].space}`, {
              method: 'GET',
              credentials: 'include',
            }).then((resp) => {
              if (resp && resp.ok) {
                return resp.json();
              }
              else {
                throw new Error ('Error when getting space');
              }
            }).then((data) => {
              spaceRequest.avatar = data.avatarUrl || `/portal/rest/v1/social/spaces/${spaceRequest.id.split(':')[0]}/avatar`;
              spaceRequest.displayName = data.displayName;
              this.spacesRequests.splice(i, 0, spaceRequest);
            });
          }
        }
      );
    },
    replyInvitationToJoinSpace(spaceId, reply) {
      externalSpacesListService.replyInvitationToJoinSpace(spaceId, reply)
        .then(() => {
          if (reply === 'approved') {
            const confirmedRequest = this.spacesRequests.filter(request => request.id === spaceId)[0];
            const space = {
              id: confirmedRequest.id,
              displayName: confirmedRequest.displayName,
              avatarUrl: confirmedRequest.avatar,

            };
            this.$emit('invitationReplied', space);
          }
          this.getSpacesRequests();
        });
    },
  }
};
</script>

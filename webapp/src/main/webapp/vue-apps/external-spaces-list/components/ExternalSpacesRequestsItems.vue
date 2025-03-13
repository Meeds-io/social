<template>
  <v-layout
    mx-0
    pa-1
    pb-1
    row
    wrap>
    <v-flex
      xs12>
      <v-list v-if="spacesRequests?.length">
        <v-list-item
          v-for="item in spacesRequests"
          :key="item.space.id"
          class="py-0 px-2">
          <v-list-item-avatar
            class="my-0 ps-2"
            tile>
            <v-avatar
              :size="avatarSize"
              tile>
              <v-img
                class="mx-auto spaceAvatar"
                :height="avatarSize"
                :max-height="avatarSize"
                :max-width="avatarSize"
                role="presentation"
                :src="item.space.avatar"
                :width="avatarSize" />
            </v-avatar>
          </v-list-item-avatar>
          <v-list-item-content class="py-0">
            <v-list-item-title class="text-color text-truncate requestSpaceName">
              {{ item.space.displayName }}
            </v-list-item-title>
            <v-list-item-subtitle
              v-sanitized-html="item.description"
              class="caption grey-color" />
          </v-list-item-content>
          <v-list-item-action>
            <v-btn-toggle
              class="transparent"
              dark>
              <v-btn
                class="px-0"
                icon
                :loading="saving"
                min-width="auto"
                small
                text
                :title="$t('externalSpacesList.tooltip.AcceptToJoin')"
                @click="replyInvitationToJoinSpace(item.space, 'approved')">
                <v-icon
                  color="success"
                  size="20">
                  mdi-checkbox-marked-circle
                </v-icon>
              </v-btn>
              <v-btn
                class="px-0"
                icon
                :loading="saving"
                min-width="auto"
                small
                text
                :title="$t('externalSpacesList.tooltip.DeclineInvitation')"
                @click="replyInvitationToJoinSpace(item.space, 'ignored')">
                <v-icon
                  color="error"
                  size="20">
                  mdi-close-circle
                </v-icon>
              </v-btn>
            </v-btn-toggle>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-flex>
  </v-layout>
</template>
<script>
  export default {
    props: {
      avatarSize: {
        type: Number,
        default: () => 37,
      },
    },
    data: () => ({
      saving: false,
      spacesRequests: [],
    }),
    created (){
      this.getSpacesRequests();
    },
    methods: {
      getSpacesRequests () {
        return eXo.$spaceService.getSpaceMemberships({
          user: eXo.env.portal.userName,
          status: 'invited',
          expand: 'spaces',
          returnSize: true,
          limit: -1,
        }).then(data => this.spacesRequests = data?.spacesMemberships || []);
      },
      async replyInvitationToJoinSpace (item, reply) {
        this.saving = true;
        try {
          if (reply === 'approved') {
            await eXo.$spaceService.accept(item.space.id);
            this.$emit('invitationReplied', {
              id: item.id,
              displayName: item.space.displayName,
              avatarUrl: item.space.avatar,
            });
          } else if (reply === 'ignored') {
            await eXo.$spaceService.deny(item.space.id);
          }
          this.getSpacesRequests();
        } finally {
          this.saving = false;
        }
      },
    },
  };
</script>

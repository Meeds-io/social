<template>
  <exo-drawer
    ref="spaceInvitationDrawer"
    right
    class="spaceInvitationDrawer">
    <template slot="title">
      {{ $t('peopleList.title.usersToInvite') }}
    </template>
    <template slot="content">
      <form
        v-if="!invitationSent"
        ref="form3"
        :disabled="savingSpace || spaceSaved"
        @keypress="checkExternalInvitation($event)">
        <exo-identity-suggester
          ref="autoFocusInput3"
          v-model="invitedMembers"
          :labels="suggesterLabels"
          :disabled="savingSpace || spaceSaved"
          :search-options="{
            spaceURL: spacePrettyName,
          }"
          :items="users"
          name="inviteMembers"
          type-of-relations="user_to_invite"
          class="ma-4"
          include-users
          include-spaces
          multiple />
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </form>
      <v-alert
        v-else
        class="ma-4 pa-3 successfulInvitation"
        outlined
        text>
        {{ $t('peopleList.label.successfulInvitation', {0: invitedUserFullName}) }}
      </v-alert>
      <v-list class="mx-4 rounded externalList" subheader>
        <v-alert
          v-if="alreadyExistAlert"
          outlined
          text
          class="mb-0 pa-2 text-center alreadyExistAlert"
          v-html="alreadyExistAlert" />
        <v-alert
          v-if="alreadyInvitedAlert"
          outlined
          text
          class="mb-0 pa-2 text-center alreadyExistAlert"
          v-html="alreadyInvitedAlert" />
        <v-list-item
          v-for="user in externalInvitedUsers"
          :key="user">
          <v-badge
            bottom
            color="white"
            bordered
            offset-x="33"
            offset-y="26"
            class="externalBadge pa-0">
            <span slot="badge"><i class="uiIconSocUserProfile"></i><v-icon class="helpIcon">mdi-help</v-icon></span>
            <v-list-item-avatar class="ms-0">
              <v-img
                :src="defaultAvatar" />
            </v-list-item-avatar>
          </v-badge>
          <v-list-item-content>
            <v-list-item-title class="externalUserEmail" v-text="user" />
            <v-list-item-subtitle class="subEmail">{{ $t('peopleList.label.pending') }}</v-list-item-subtitle>
          </v-list-item-content>
          <v-btn
            v-exo-tooltip.bottom.body="$t('peopleList.label.clickToDecline')"
            icon
            @click="removeExternalInvitation(user)">
            <v-icon>
              mdi-close-circle
            </v-icon>
          </v-btn>
        </v-list-item>
      </v-list>
      <v-list
        v-if="externalInvitationsSent.length > 0"
        class="mx-4 mt-0 rounded externalList"
        subheader>
        <v-list-item
          v-for="invitation in externalInvitationsSent"
          :key="invitation">
          <v-badge
            bottom
            bordered
            color="white"
            offset-x="33"
            offset-y="26"
            class="externalBadge pa-0">
            <span slot="badge"><i class="uiIconSocUserProfile"></i><v-icon color="white" class="helpIcon">mdi-help</v-icon></span>
            <v-list-item-avatar class="ms-0">
              <v-img
                :src="defaultAvatar" />
            </v-list-item-avatar>
          </v-badge>
          <v-list-item-content>
            <v-list-item-title class="externalUserEmail" v-text="invitation.userEmail" />
            <v-list-item-subtitle v-if="!invitation.expired" class="subEmail">{{ $t('peopleList.label.invitationSent') }}</v-list-item-subtitle>
            <v-list-item-subtitle
              v-exo-tooltip.bottom.body="$t('peopleList.label.invitationExpiredToolTip')"
              v-else
              class="subExpired">
              {{ $t('peopleList.label.invitationExpired') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-btn
            v-exo-tooltip.bottom.body="$t('peopleList.label.clickToDecline')"
            icon
            @click="declineInvitation(invitation)">
            <v-icon>
              mdi-close-circle
            </v-icon>
          </v-btn>
        </v-list-item>
      </v-list>
    </template>

    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="savingSpace || spaceSaved"
          class="btn me-2"
          @click="cancel">
          {{ $t('peopleList.label.cancel') }}
        </v-btn>
        <v-btn
          :loading="savingSpace"
          :disabled="saveButtonDisabled"
          class="btn btn-primary"
          @click="inviteUsers">
          {{ $t('peopleList.button.inviteUsers') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
const IDLE_TIME = 3000;

export default {
  props: {
    isExternalFeatureEnabled: {
      type: Boolean,
      default: true,
    }
  },
  data: () => ({
    users: [],
    savingSpace: false,
    spaceSaved: false,
    error: null,
    spacePrettyName: eXo.env.portal.spaceName,
    invitedMembers: [],
    includeExternalUser: false,
    externalInvitedUsers: [],
    invitationSent: false,
    alreadyExistAlert: '',
    alreadyInvitedAlert: '',
    externalInvitationsSent: [],
  }),
  computed: {
    saveButtonDisabled() {
      return this.savingSpace || this.spaceSaved || !this.invitedMembers || !this.invitedMembers.length && !this.includeExternalUser;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('peopleList.label.inviteMembers'),
        noDataLabel: this.$t('peopleList.label.noDataLabel'),
      };
    },
    defaultAvatar() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`;
    },
    invitedUser() {
      return this.invitedMembers.length === 1 && this.invitedMembers[0] && this.invitedMembers[0].profile;
    },
    invitedUserFullName() {
      return this.invitedUser && this.invitedUser.fullName || this.$t('peopleList.label.users');
    },
  },
  watch: {
    savingSpace() {
      if (this.savingSpace) {
        this.$refs.spaceInvitationDrawer.startLoading();
      } else {
        this.$refs.spaceInvitationDrawer.endLoading();
      }
    },
  },
  methods: {
    open() {
      this.savingSpace = false;
      this.spaceSaved = false;
      this.includeExternalUser = false;
      this.error = null;
      this.spacePrettyName = eXo.env.portal.spaceName;
      this.invitedMembers = [];
      this.externalInvitedUsers = [];
      this.$refs.spaceInvitationDrawer.open();
      this.$spaceService.findSpaceExternalInvitationsBySpaceId(eXo.env.portal.spaceId)
        .then(invitations => {
          this.externalInvitationsSent = invitations;
        });
    },
    cancel() {
      this.$refs.spaceInvitationDrawer.close();
    },
    inviteUsers(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      if (this.spaceSaved || this.savingSpace) {
        return;
      }
      this.error = null;
      this.savingSpace = true;
      this.$spaceService.updateSpace({
        id: eXo.env.portal.spaceId,
        invitedMembers: this.invitedMembers,
        externalInvitedUsers: this.externalInvitedUsers
      })
        .then(() => {
          this.spaceSaved = true;
          this.$emit('refresh');
          this.externalInvitedUsers = [];
          this.invitationSent = true;

          window.setTimeout(() => {
            this.$refs.spaceInvitationDrawer.close();
            this.invitationSent = false;
          }, IDLE_TIME);
        })
        .catch(e => {
          // eslint-disable-next-line no-console
          console.warn('Error updating space ', this.invitedMembers, e);
          this.error = this.$t('peopleList.error.errorWhenSavingSpace');
        })
        .finally(() => this.savingSpace = false);
    },
    checkExternalInvitation(event) {
      const self = this;
      $('form').on('focusout', function(event) {
        setTimeout(function() {
          if (!event.delegateTarget.contains(document.activeElement)) {
            self.getExternalEmail();
          }
        }, 1);
      });
      // eslint-disable-next-line eqeqeq
      if (event.key == 'Enter') {
        event.preventDefault();
        this.getExternalEmail();
      }
      // eslint-disable-next-line eqeqeq
      if (event.keyCode == '32') {
        this.getExternalEmail();
      }
    },
    getExternalEmail() {
      const reg = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,24}))$/;
      const input = this.$refs.autoFocusInput3.searchTerm;
      const words = input!== null ? input.split(' ') : '';
      const email = words[words.length - 1];
      if (reg.test(email)) {
        this.$userService.getUserByEmail(email)
          .then(user => {
            if (user.id !== 'null') {
              this.$spaceService.isSpaceMember(eXo.env.portal.spaceId, user.remoteId).then(data => {
                if (data.isMember === 'true') {
                  $(`#${this.$refs.autoFocusInput3.id} input`)[0].blur();
                  this.alreadyExistAlert = `<span style="font-style: italic;">${email}</span> ${this.$t('peopleList.label.alreadyMember')}`;
                  setTimeout(() => this.alreadyExistAlert ='', 3000);
                } else {
                  this.users.push(user);
                  const indexOfuser = this.invitedMembers.findIndex(u => u.remoteId === user.remoteId);
                  if (indexOfuser === -1) {
                    setTimeout(() => this.invitedMembers.push(user), 0);
                  }
                }
              });
              this.$refs.autoFocusInput3.searchTerm = null;
            } else {
              if (this.isExternalFeatureEnabled) {
                this.includeExternalUser = true;
                const user = this.externalInvitationsSent.find(invited => invited.userEmail === email);
                if (user) {
                  $(`#${this.$refs.autoFocusInput3.id} input`)[0].blur();
                  this.alreadyInvitedAlert = this.$t('peopleList.label.alreadyInvited');
                  setTimeout(() => this.alreadyInvitedAlert ='', 3000);
                } else if (this.externalInvitedUsers.indexOf(email) === -1) {
                  this.externalInvitedUsers.push(email);
                }
                this.$refs.autoFocusInput3.searchTerm = null;
              }
            }
          });
      }
    },
    removeExternalInvitation(user) {
      const index = this.externalInvitedUsers.indexOf(user);
      if (index > -1) {
        this.externalInvitedUsers.splice(index, 1);
      }
    },
    declineInvitation(invitation) {
      this.externalInvitationsSent.splice(this.externalInvitationsSent.indexOf(invitation),1);
      this.$spaceService.declineExternalInvitation(eXo.env.portal.spaceId, invitation.invitationId);
    }
  },
};
</script>

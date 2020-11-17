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
        @keypress="checkEternal($event)"
        @submit="inviteUsers">
        <exo-identity-suggester
          ref="autoFocusInput3"
          v-model="invitedMembers"
          :labels="suggesterLabels"
          :disabled="savingSpace || spaceSaved"
          :search-options="{
            spaceURL: spacePrettyName,
          }"
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
        class="ma-4"
        outlined
        text
        color="green"
      >
        {{ this.$t('peopleList.label.successfulInvitation') }}
      </v-alert>
      <v-list v-if="externalUsers.length > 0" class="ma-4 rounded" style="background-color: #F6F8FA" subheader>
        <v-list-item
          v-for="user in externalUsers"
          :key="user"
        >
          <v-list-item-avatar>
            <v-img
              :src="defaultAvatar"
            ></v-img>
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title v-text="user"></v-list-item-title>
          </v-list-item-content>

          <v-btn icon @click="removeEternal(user)">
            <v-icon color="#BE4141">
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
          class="btn mr-2"
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
const IDLE_TIME = 2000;

export default {
  data: () => ({
    savingSpace: false,
    spaceSaved: false,
    error: null,
    spacePrettyName: eXo.env.portal.spaceName,
    invitedMembers: [],
    isExternalUser: false,
    externalUsers: [],
    invitationSent:false
  }),
  computed: {
    saveButtonDisabled() {
      return this.savingSpace || this.spaceSaved || !this.invitedMembers || !this.invitedMembers.length && !this.isExternalUser;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('peopleList.label.inviteMembers'),
        noDataLabel: this.$t('peopleList.label.noDataLabel'),
      };
    },
    defaultAvatar() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`;
    }
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
      this.error = null;
      this.spacePrettyName = eXo.env.portal.spaceName;
      this.invitedMembers = [];
      this.$refs.spaceInvitationDrawer.open();
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
        externalInvitedUsers:this.externalUsers
      })
        .then(() => {
          this.spaceSaved = true;
          this.$emit('refresh');
          this.externalUsers = [];
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
    checkEternal(event) {
      const reg = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,24}))$/;
      // eslint-disable-next-line eqeqeq
      if(event.keyCode == '32'){ // Press space
        const input = $(`#${this.$refs.autoFocusInput3.id} input`)[0];
        const words = input.value.split(' ');
        const email = words[words.length - 1];
        if (reg.test(email)) {
          this.isExternalUser = true;
          this.externalUsers.push(email);
          input.value = '';
        }
      }
      // eslint-disable-next-line eqeqeq
      if (event.keyCode == '13') {
        event.preventDefault();
      }
    },
    removeEternal(user) {
      const index = this.externalUsers.indexOf(user);
      if (index > -1) {
        this.externalUsers.splice(index, 1);
      }
    },
  },
};
</script>
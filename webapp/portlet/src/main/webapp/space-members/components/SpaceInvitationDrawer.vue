<template>
  <exo-drawer
    ref="spaceFormDrawer"
    right
    class="spaceFormDrawer">
    <template slot="title">
      {{ $t('peopleList.title.usersToInvite') }}
    </template>
    <template slot="content">
      <form
        ref="form3"
        :disabled="savingSpace || spaceSaved"
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
          height="100"
          class="ma-4"
          include-users
          include-spaces
          multiple />
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </form>
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
const IDLE_TIME = 200;

export default {
  data: () => ({
    savingSpace: false,
    spaceSaved: false,
    error: null,
    spacePrettyName: eXo.env.portal.spaceName,
    invitedMembers: [],
  }),
  computed: {
    saveButtonDisabled() {
      return this.savingSpace || this.spaceSaved || !this.invitedMembers || !this.invitedMembers.length;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('peopleList.label.inviteMembers'),
        noDataLabel: this.$t('peopleList.label.noDataLabel'),
      };
    }
  },
  watch: {
    savingSpace() {
      if (this.savingSpace) {
        this.$refs.spaceFormDrawer.startLoading();
      } else {
        this.$refs.spaceFormDrawer.endLoading();
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
      this.$refs.spaceFormDrawer.open();
    },
    cancel() {
      this.$refs.spaceFormDrawer.close();
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
      })
        .then(() => {
          this.spaceSaved = true;
          this.$emit('refresh');

          window.setTimeout(() => {
            this.$refs.spaceFormDrawer.close();
          }, IDLE_TIME);
        })
        .catch(e => {
          // eslint-disable-next-line no-console
          console.warn('Error updating space ', this.invitedMembers, e);
          this.error = this.$t('peopleList.error.errorWhenSavingSpace');
        })
        .finally(() => this.savingSpace = false);
    }
  },
};
</script>
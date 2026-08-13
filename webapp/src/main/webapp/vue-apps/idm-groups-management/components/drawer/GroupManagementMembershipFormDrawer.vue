<template>
  <exo-drawer
    id="membershipFormDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    right>
    <template #title>
      {{ title }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="membershipForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveMembership">
        <v-card-text class="d-flex membershipNameLabel flex-grow-1 text-no-wrap pb-2">
          {{ $t('UsersManagement.membershipType') }}
        </v-card-text>
        <v-card-text class="d-flex membershipNameField py-0">
          <template v-if="newMembership">
            <select
              ref="membershipTypeInput"
              v-model="membershipType"
              :disabled="saving"
              class="input-block-level ignore-vuetify-classes"
              required>
              <option
                v-for="membershipType in membershipTypes"
                :key="membershipType.name"
                :value="membershipType.name">
                {{ membershipType.name }}
              </option>
            </select>
          </template>
          <template v-else>
            <select
              ref="membershipTypeInput"
              v-model="membership.membershipType"
              :disabled="saving"
              class="input-block-level ignore-vuetify-classes"
              required>
              <option
                v-for="membershipType in membershipTypes"
                :key="membershipType.name"
                :value="membershipType.name">
                {{ membershipType.name }}
              </option>
            </select>
          </template>
        </v-card-text>

        <v-card-text class="d-flex membershipUser flex-grow-1 text-no-wrap pb-2">
          {{ $t('GroupsManagement.user') }}
        </v-card-text>
        <v-card-text class="d-flex membershipUserField py-0">
          <v-flex v-if="newMembership">
            <v-autocomplete
              id="membershipUserNameInput"
              ref="membershipUserNameInput"
              v-model="selectedUsers"
              autofocus
              item-text="fullName"
              item-value="userName"
              menu-props="closeOnClick, closeOnContentClick, maxHeight = 100"
              class="identitySuggester identitySuggesterInputStyle"
              content-class="identitySuggesterContent"
              append-icon=""
              width="100%"
              max-width="100%"
              chips
              dense
              flat
              hide-selected
              multiple
              cache-items
              :disabled="saving || !newMembership"
              :loading="loadingSuggestions > 0"
              :items="users"
              :search-input.sync="searchTerm"
              :placeholder="$t('GroupsManagement.addMember.label')"
              :required="!selectedUsers.length"
              :return-object="false"
              @change="clearSearch"
              @update:search-input="searchTerm = $event">
              <template slot="no-data">
                <v-list-item class="pa-0">
                  <v-list-item-title class="px-2">
                    {{ $t('GroupsManagement.label.addMembers') }}
                  </v-list-item-title>
                </v-list-item>
              </template>
              <template slot="selection" slot-scope="{item, selected}">
                <v-chip
                  :input-value="selected"
                  :close="newMembership"
                  class="identitySuggesterItem"
                  @click:close="removeMemberShip(item)">
                  <span class="text-truncate">
                    {{ item.fullName }}
                  </span>
                </v-chip>
              </template>
              <template slot="item" slot-scope="{ item }">
                <v-list-item-title class="text-truncate identitySuggestionMenuItemText" v-text="item.fullName" />
              </template>
            </v-autocomplete>
          </v-flex>
        </v-card-text>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="cancel">
          {{ $t('GroupsManagement.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="saving"
          :loading="saving"
          class="btn btn-primary"
          @click="saveMembership">
          {{ $t('GroupsManagement.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    fieldError: false,
    drawer: false,
    newMembership: false,
    saving: false,
    confirmNewPassword: null,
    previousSearchTerm: null,
    searchTerm: null,
    loadingSuggestions: 0,
    membershipTypes: [],
    users: [],
    memberships: [],
    selectedUsers: [],
    group: {},
    membership: {},
    membershipType: null,
  }),
  computed: {
    title() {
      if (this.newMembership) {
        return this.$t('GroupsManagement.addMemberInGroup');
      } else {
        return this.$t('GroupsManagement.editMembership');
      }
    },
  },
  watch: {
    searchTerm(value) {
      if (value?.length) {
        window.setTimeout(() => {
          if (this.previousSearchTerm === this.searchTerm) {
            this.users = [];

            this.loadingSuggestions++;
            this.$userService.getUsersByStatus(value, 0, 20, 'ENABLED')
              .then(data => {
                this.users = data && data.entities || [];
              })
              .finally(() => this.loadingSuggestions--);
          }
          this.previousSearchTerm = this.searchTerm;
        }, 400);
      } else {
        this.users = [];
      }
    },
    selectedUsers() {
      this.selectedUsers.forEach(user => {
        if (!this.memberships.some(membership => membership.userName === user)) {
          this.memberships.push({
            groupId: this.group.id,
            membershipType: this.membershipType,
            userName: user
          });
        }
      });
    },
  },
  created() {
    this.$root.$on('addNewMembership', this.addNewMembership);

    return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/membershipTypes`, {
      method: 'GET',
      credentials: 'include',
    }).then(resp => {
      if (!resp || !resp.ok) {
        throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
      } else {
        return resp.json();
      }
    }).then(data => {
      this.membershipTypes = data || [];
    });
  },
  methods: {
    resetCustomValidity() {
      this.$refs.membershipTypeInput.setCustomValidity('');
    },
    addNewMembership(group) {
      this.membershipType = 'member';
      this.selectedUsers = [];
      this.membership = {};
      this.group = group;
      this.newMembership = true;
      this.drawer = true;
    },
    saveMembership(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.fieldError = false;
      this.resetCustomValidity();
      if (!this.$refs.membershipForm.validate() // Vuetify rules
          || !this.$refs.membershipForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }
      this.saving = true;
      this.membership.groupId = this.group.id;
      // set the membershipType for each membership
      this.memberships.forEach(membership => membership.membershipType = this.membershipType);
      const input = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/${this.newMembership ? 'memberships/bulk' : 'memberships'}?membershipId=${this.membership.id || ''}`;
      const init = {
        method: this.newMembership && 'POST' || 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.newMembership ? this.memberships : this.membership),
      };
      return fetch(input, init).then(resp => {
        if (!resp || !resp.ok) {
          if (resp.status === 400) {
            return resp.text().then(error => {
              this.fieldError = error;
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          }
        }
      }).then(() => this.$root.$emit('refresh-group-members'))
        .then(() => this.$refs.drawer.close())
        .catch(this.handleError)
        .finally(() => {
          this.memberships = [];
          this.saving = false;
        });
    },
    cancel() {
      this.drawer = false;
    },
    handleError(error) {
      this.resetCustomValidity();

      if (error) {
        if (this.fieldError && this.fieldError === 'MEMBERSHIP:ALREADY_EXISTS') {
          this.$refs.membershipTypeInput.setCustomValidity(this.$t('GroupsManagement.message.sameMembershipAlreadyExists'));
        } else {
          this.error = String(error);
          this.$root.$emit('alert-message', error, 'error');
        }

        window.setTimeout(() => {
          if (!this.$refs.membershipForm.validate() // Vuetify rules
              || !this.$refs.membershipForm.$el.reportValidity()) { // Standard HTML rules
            return;
          }
        }, 200);
      }
    },
    refreshUserSelection(value) {
      if (value) {
        this.loadingSuggestions++;
        this.$userService.getUsersByStatus(value, 0, 20, 'ANY')
          .then(data => this.users = data && data.entities || [])
          .finally(() => this.loadingSuggestions--);
      }
    },
    removeMemberShip(user) {
      this.memberships.splice(this.memberships.findIndex(membership => membership.userName === user.userName), 1);
      this.selectedUsers.splice(this.selectedUsers.findIndex(userName => userName === user.userName), 1);
    },
    clearSearch() {
      this.searchTerm = null;
    }
  },
};
</script>

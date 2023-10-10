<template>
  <exo-drawer
    id="membershipTypeFormDrawer"
    ref="membershipTypeFormDrawer"
    right
    @closed="drawer = false">
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-form
        ref="membershipTypeForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveMembershipType">
        <v-card-text class="d-flex membershipTypeNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('MembershipTypesManagement.name') }}<template v-if="newMembershipType">*</template>
        </v-card-text>
        <v-card-text class="d-flex membershipTypeNameField py-0">
          <input
            ref="nameInput"
            v-model="membershipType.name"
            :disabled="saving || !newMembershipType"
            :autofocus="drawer"
            :placeholder="$t('MembershipTypesManagement.namePlaceholder')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex descriptionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('MembershipTypesManagement.description') }}
        </v-card-text>
        <v-card-text class="d-flex descriptionField py-0">
          <textarea
            ref="descriptionInput"
            v-model="membershipType.description"
            :disabled="saving"
            :placeholder="$t('MembershipTypesManagement.descriptionPlaceholder')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1 textarea-no-resize"
            maxlength="2000"
            required></textarea>
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
          {{ $t('MembershipTypesManagement.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="saving"
          :loading="saving"
          class="btn btn-primary"
          @click="saveMembershipType">
          {{ $t('MembershipTypesManagement.button.save') }}
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
    newMembershipType: false,
    saving: false,
    confirmNewPassword: null,
    membershipType: {},
  }),
  computed: {
    title() {
      if (this.newMembershipType) {
        return this.$t('MembershipTypesManagement.addMembershipType');
      } else {
        return this.$t('MembershipTypesManagement.editMembershipType');
      }
    },
  },
  watch: {
    confirmNewPassword() {
      this.resetCustomValidity();
    },
    saving() {
      if (this.saving) {
        this.$refs.membershipTypeFormDrawer.startLoading();
      } else {
        this.$refs.membershipTypeFormDrawer.endLoading();
      }
    },
    drawer() {
      if (this.drawer) {
        this.$refs.membershipTypeFormDrawer.open();
        window.setTimeout(() => {
          if (this.newMembershipType) {
            this.$refs.nameInput.focus();
          } else {
            this.$refs.descriptionInput.focus();
          }
        }, 200);
      } else {
        this.$refs.membershipTypeFormDrawer.close();
      }
    },
  },
  created() {
    this.$root.$on('addNewMembershipType', this.addNewMembershipType);
    this.$root.$on('editMembershipType', this.editMembershipType);
  },
  methods: {
    resetCustomValidity() {
      this.$refs.nameInput.setCustomValidity('');
    },
    addNewMembershipType() {
      this.membershipType = {};
      this.newMembershipType = true;
      this.drawer = true;
    },
    editMembershipType(membershipType) {
      this.membershipType = Object.assign({}, membershipType);
      this.newMembershipType = false;
      this.drawer = true;
    },
    saveMembershipType(event) {
      if (!this.membershipType.name || this.membershipType.name.length < 3 || this.membershipType.name.length > 30) {
        this.handleError(this.$t('MembershipTypesManagement.error.invalidRoleName'));
        return ;
      }
      if (!this.membershipType.description || this.membershipType.description.length < 3 || this.membershipType.description.length > 255) {
        this.handleError(this.$t('MembershipTypesManagement.error.invalidRoleDescription'));
        return ;
      }
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.fieldError = false;
      this.resetCustomValidity();

      if (!this.$refs.membershipTypeForm.validate() // Vuetify rules
          || !this.$refs.membershipTypeForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.saving = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/membershipTypes`, {
        method: this.newMembershipType && 'POST' || 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.membershipType),
      }).then(resp => {
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
      }).then(() => this.$root.$emit('refreshMembershipTypes'))
        .then(() => this.$refs.membershipTypeFormDrawer.close())
        .catch(this.handleError)
        .finally(() => this.saving = false);
    },
    cancel() {
      this.drawer = false;
    },
    handleError(error) {
      this.resetCustomValidity();

      if (error) {
        if (this.fieldError && this.fieldError === 'NAME:ALREADY_EXISTS') {
          this.$refs.nameInput.setCustomValidity(this.$t('MembershipTypesManagement.message.sameNameAlreadyExists'));
        } else {
          this.$root.$emit('alert-message', error, 'error');
        }

        window.setTimeout(() => {
          if (!this.$refs.membershipTypeForm.validate() // Vuetify rules
              || !this.$refs.membershipTypeForm.$el.reportValidity()) { // Standard HTML rules
            return;
          }
        }, 200);
      }
    },
  },
};
</script>
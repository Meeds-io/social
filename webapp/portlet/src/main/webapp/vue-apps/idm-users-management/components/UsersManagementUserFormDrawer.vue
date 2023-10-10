<template>
  <exo-drawer
    id="userFormDrawer"
    ref="userFormDrawer"
    right
    @closed="drawer = false">
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-form
        ref="userForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveUser">
        <v-card-text class="d-flex userNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UsersManagement.userName') }}<template v-if="newUser">*</template>
        </v-card-text>
        <v-card-text class="d-flex userNameField py-0">
          <input
            ref="userNameInput"
            v-model="user.userName"
            :disabled="saving || !newUser"
            :autofocus="drawer"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex firstNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UsersManagement.firstName') }}*
        </v-card-text>
        <v-card-text class="d-flex firstNameField py-0">
          <input
            ref="firstNameInput"
            v-model="user.firstName"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex lastNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UsersManagement.lastName') }}*
        </v-card-text>
        <v-card-text class="d-flex lastNameField py-0">
          <input
            ref="lastNameInput"
            v-model="user.lastName"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex emailLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UsersManagement.email') }}*
        </v-card-text>
        <v-card-text class="d-flex emailField py-0">
          <input
            ref="emailInput"
            v-model="user.email"
            :disabled="saving"
            type="email"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex newPasswordLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UsersManagement.password') }}<template v-if="newUser">*</template>
        </v-card-text>
        <v-card-text class="d-flex newPasswordField py-0">
          <input
            ref="newPasswordInput"
            v-model="user.password"
            :disabled="saving || (!user.isInternal && !newUser)"
            :required="newUser"
            type="password"
            autocomplete="new-password"
            class="ignore-vuetify-classes flex-grow-1">
        </v-card-text>

        <v-card-text class="d-flex confirmPasswordLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UsersManagement.confirmPassword') }}<template v-if="newUser">*</template>
        </v-card-text>
        <v-card-text class="d-flex confirmPasswordField py-0">
          <input
            ref="confirmNewPassword"
            v-model="user.confirmNewPassword"
            :disabled="saving || (!user.isInternal && !newUser)"
            :required="newUser"
            type="password"
            autocomplete="new-password"
            class="ignore-vuetify-classes flex-grow-1">
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
          {{ $t('UsersManagement.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="saving"
          :loading="saving"
          class="btn btn-primary"
          @click="saveUser">
          {{ $t('UsersManagement.button.save') }}
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
    newUser: false,
    saving: false,
    user: {
      enabled: true,
    },
  }),
  computed: {
    title() {
      if (this.newUser) {
        return this.$t('UsersManagement.addUser');
      } else {
        return this.$t('UsersManagement.editUser');
      }
    },
  },
  watch: {
    saving() {
      if (this.saving) {
        this.$refs.userFormDrawer.startLoading();
      } else {
        this.$refs.userFormDrawer.endLoading();
      }
    },
    drawer() {
      if (this.drawer) {
        this.$refs.userFormDrawer.open();
        window.setTimeout(() => {
          if (this.newUser) {
            this.$refs.userNameInput.focus();
          } else {
            this.$refs.firstNameInput.focus();
          }
        }, 200);
      } else {
        this.$refs.userFormDrawer.close();
      }
    },
  },
  created() {
    this.$root.$on('addNewUser', this.addNewUser);
    this.$root.$on('editUser', this.editUser);
  },
  methods: {
    resetCustomValidity() {
      this.$refs.userNameInput.setCustomValidity('');
      this.$refs.firstNameInput.setCustomValidity('');
      this.$refs.lastNameInput.setCustomValidity('');
      this.$refs.emailInput.setCustomValidity('');
      this.$refs.newPasswordInput.setCustomValidity('');
      this.$refs.confirmNewPassword.setCustomValidity('');
    },
    addNewUser() {
      this.user = {
        enabled: true,
      };
      this.newUser = true;
      this.drawer = true;
    },
    editUser(user) {
      this.user = Object.assign({}, user);
      this.newUser = false;
      this.drawer = true;
    },
    saveUser(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.fieldError = false;
      this.resetCustomValidity();

      if (!this.$refs.userForm.validate() // Vuetify rules
          || !this.$refs.userForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      if (this.user.confirmNewPassword && this.user.password && this.user.confirmNewPassword !== this.user.password) {
        this.$refs.confirmNewPassword.setCustomValidity(this.$t('UsersManagement.newPasswordsDoesNotMatch'));
        if (!this.$refs.userForm.$el.reportValidity()) {
          return;
        }
      }

      this.saving = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users`, {
        method: this.newUser && 'POST' || 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.user),
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
      }).then(() => this.$root.$emit('refreshUsers'))
        .then(() => this.$refs.userFormDrawer.close())
        .catch(this.handleError)
        .finally(() => this.saving = false);
    },
    cancel() {
      this.drawer = false;
    },
    handleError(error) {
      this.resetCustomValidity();

      if (error) {
        if (this.fieldError && this.fieldError.indexOf('USERNAME:') === 0) {
          if (this.fieldError === 'USERNAME:ALREADY_EXISTS') {
            this.$refs.userNameInput.setCustomValidity(this.$t('UsersManagement.message.userWithSameNameAlreadyExists'));
          } else if (this.fieldError === 'USERNAME:ALREADY_EXISTS_AS_DELETED') {
            this.$refs.userNameInput.setCustomValidity(this.$t('UsersManagement.message.LoginForDeletedUser'));
          } else {
            const usernameError = this.fieldError.replace('USERNAME:', '');
            this.$refs.userNameInput.setCustomValidity(usernameError);
          }
        } else if (this.fieldError && this.fieldError.indexOf('FIRSTNAME:') === 0) {
          const firstNameError = this.fieldError.replace('FIRSTNAME:', '');
          this.$refs.firstNameInput.setCustomValidity(firstNameError);
        } else if (this.fieldError && this.fieldError.indexOf('LASTNAME:') === 0) {
          const lastNameError = this.fieldError.replace('LASTNAME:', '');
          this.$refs.lastNameInput.setCustomValidity(lastNameError);
        } else if (this.fieldError && this.fieldError.indexOf('EMAIL:') === 0) {
          if (this.fieldError === 'EMAIL:ALREADY_EXISTS') {
            this.$refs.emailInput.setCustomValidity(this.$t('UsersManagement.message.userWithSameEmailAlreadyExists'));
          } else {
            const emailError = this.fieldError.replace('EMAIL:', '');
            this.$refs.emailInput.setCustomValidity(emailError);
          }
        } else if (this.fieldError && this.fieldError.indexOf('PASSWORD:') === 0) {
          const newPasswordError = this.fieldError.replace('PASSWORD:', '');
          this.$refs.newPasswordInput.setCustomValidity(newPasswordError);
        } else {
          error = error.message || String(error);
          const errorI18NKey = `UsersManagement.error.${error}`;
          const errorI18N = this.$t(errorI18NKey, {0: this.user.fullname});
          if (errorI18N !== errorI18NKey) {
            error = errorI18N;
          }
          this.$root.$emit('alert-message', error, 'error');
        }

        window.setTimeout(() => {
          if (!this.$refs.userForm.validate() // Vuetify rules
              || !this.$refs.userForm.$el.reportValidity()) { // Standard HTML rules
            return;
          }
        }, 200);
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
  },
};
</script>

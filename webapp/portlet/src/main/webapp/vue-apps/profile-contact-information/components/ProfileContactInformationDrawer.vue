<template>
  <div>
    <exo-drawer
      ref="profileContactInformationDrawer"
      v-model="drawer"
      body-classes="hide-scroll decrease-z-index-more"
      class="profileContactInformationDrawer"
      right>
      <template slot="title">
        {{ $t('profileContactInformation.contactInformation') }}
      </template>
      <template v-if="drawer" slot="content">
        <v-form
          ref="profileContactForm"
          class="form-horizontal"
          flat>
          <v-flex class="d-flex justify-center">
            <profile-header-avatar
              ref="avatar"
              :user="userToSave"
              :size="150"
              :avatar-data="avatarData"
              owner
              hover
              @edit="$refs.imageCropDrawer.open()" />
          </v-flex>
          <v-card-text v-if="error" class="errorMessage">
            <v-alert type="error">
              {{ error }}
            </v-alert>
          </v-card-text>
          <v-card-text class="d-flex fullnameLabels text-color pb-2">
            <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold me-3">
              {{ $t('profileContactInformation.firstName') }}*
            </div>
            <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold px-3">
              {{ $t('profileContactInformation.lastName') }}*
            </div>
          </v-card-text>
          <v-card-text class="d-flex fullnameFields py-0">
            <div v-exo-tooltip.bottom.body="userToSave.isInternal ? $t('profileContactInformation.firstName') : $t('profileContactInformation.synchronizedUser.tooltip')" class="align-start flex-grow-0 text-no-wrap text-left font-weight-bold me-3">
              <input
                v-model="userToSave.firstname"
                :disabled="saving || !userToSave.isInternal"
                type="text"
                class="ignore-vuetify-classes"
                maxlength="2000"
                required>
            </div>
            <div v-exo-tooltip.bottom.body="userToSave.isInternal ? $t('profileContactInformation.lastName') : $t('profileContactInformation.synchronizedUser.tooltip')" class="align-end flex-grow-0 text-no-wrap text-left font-weight-bold">
              <input
                v-model="userToSave.lastname"
                :disabled="saving || !userToSave.isInternal"
                type="text"
                class="ignore-vuetify-classes"
                maxlength="2000"
                required>
            </div>
          </v-card-text>
          <v-card-text class="d-flex emailLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.email') }}*
          </v-card-text>
          <v-card-text v-exo-tooltip.bottom.body="userToSave.isInternal ? $t('profileContactInformation.email') : $t('profileContactInformation.synchronizedUser.tooltip')" class="d-flex emailField py-0">
            <input
              v-model="userToSave.email"
              :disabled="saving || !userToSave.isInternal"
              type="email"
              class="ignore-vuetify-classes"
              maxlength="2000"
              required>
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.jobTitle') }}*
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.position"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000"
              required>
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.company') }}
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.company"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000">
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.location') }}
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.location"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000">
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.department') }}
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.department"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000">
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.team') }}
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.team"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000">
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.profession') }}
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.profession"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000">
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.country') }}
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.country"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000">
          </v-card-text>
          <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
            {{ $t('profileContactInformation.city') }}
          </v-card-text>
          <v-card-text class="d-flex positionField py-0">
            <input
              v-model="userToSave.city"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000">
          </v-card-text>
          <profile-contact-edit-phone v-model="userToSave.phones" />
          <profile-contact-edit-ims v-model="userToSave.ims" />
          <profile-contact-edit-urls v-model="userToSave.urls" />
        </v-form>
      </template>
      <template slot="footer">
        <div class="d-flex">
          <v-spacer />
          <v-btn
            :disabled="saving"
            class="btn me-2"
            @click="cancel">
            {{ $t('profileContactInformation.button.cancel') }}
          </v-btn>
          <v-btn
            :disabled="saving"
            :loading="saving"
            class="btn btn-primary"
            @click="save">
            {{ $t('profileContactInformation.button.save') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
    <image-crop-drawer
      ref="imageCropDrawer"
      v-if="drawer"
      v-model="userToSave.avatarUploadId"
      :crop-options="cropOptions"
      :max-file-size="maxUploadSizeInBytes"
      :src="avatarData || `${user.avatar}&size=0`"
      max-image-width="350"
      drawer-title="UIChangeAvatarContainer.label.ChangeAvatar"
      circle
      @data="avatarData = $event" />
  </div>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    uploadLimit: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    userToSave: {},
    drawer: false,
    error: null,
    saving: null,
    fieldError: false,
    avatarData: null,
    cropOptions: {
      aspectRatio: 1,
      viewMode: 1,
    },
  }),
  computed: {
    maxUploadSizeInBytes() {
      return this.uploadLimit * 1024 *1024;
    },
  },
  methods: {
    resetCustomValidity() {
      this.$refs.profileContactForm.$el[3].setCustomValidity('');
      this.$refs.profileContactForm.$el[4].setCustomValidity('');
      this.$refs.profileContactForm.$el[5].setCustomValidity('');
    },
    save() {
      this.error = null;
      this.fieldError = false;
      this.resetCustomValidity();
      
      if (this.userToSave.urls.length) {
        if (this.userToSave.urls.some(urlDiv => urlDiv.url && urlDiv.url.length > 100)) {
          this.handleError(this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileContactInformation.urls'),
            1: 10,
            2: 100,
          }));
          return;
        }
        if (this.userToSave.email.length > 100 || this.userToSave.email.length < 10) {
          this.$refs.profileContactForm.$el[5].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileContactInformation.email'),
            1: 10,
            2: 100,
          }));
        } else {
          this.$refs.profileContactForm.$el[5].setCustomValidity('');
        }
      }

      if (!this.$refs.profileContactForm.validate() // Vuetify rules
          || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.$refs.profileContactInformationDrawer.startLoading();
      this.saving = true;
      if (this.userToSave.avatarUploadId) {
        this.userToSave.avatar = this.userToSave.avatarUploadId;
      } else {
        this.userToSave.avatar = null;
      }
      if (this.userToSave.firstname !== this.user.firstname || this.userToSave.lastname !== this.user.lastname) {
        this.userToSave.fullname = `${this.userToSave.firstname} ${this.userToSave.lastname}`;
      }

      const fields = [
        'avatar',
        'firstname',
        'lastname',
        'fullname',
        'email',
        'position',
        'company',
        'location',
        'department',
        'team',
        'profession',
        'country',
        'city',
        'phones',
        'ims',
        'urls'
      ];
      const objectToSend = {};
      for (const i in fields) {
        objectToSend[fields[i]] = this.userToSave[fields[i]];
      }

      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${eXo.env.portal.userName}/profile`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(objectToSend),
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
      }).then(this.refresh)
        .then(() => this.$refs.profileContactInformationDrawer.close())
        .catch(this.handleError)
        .finally(() => {
          this.saving = false;
          this.$refs.profileContactInformationDrawer.endLoading();
        });
    },
    handleImageUploadError(error) {
      if (error) {
        if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
          this.error = this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.uploadLimit});
        } else {
          this.error = String(error);
        }

        window.setTimeout(() => {
          this.error = null;
        }, 5000);
      }
    },
    handleError(error) {
      this.resetCustomValidity();
      
      if (error) {
        if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
          this.error = this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.uploadLimit});
        } else if (this.fieldError && this.fieldError.indexOf('FIRSTNAME:') === 0) {
          const firstNameError = this.fieldError.replace('FIRSTNAME:', '');
          this.$refs.profileContactForm.$el[3].setCustomValidity(firstNameError);
        } else if (this.fieldError && this.fieldError.indexOf('LASTNAME:') === 0) {
          const lastNameError = this.fieldError.replace('LASTNAME:', '');
          this.$refs.profileContactForm.$el[4].setCustomValidity(lastNameError);
        } else if (this.fieldError && this.fieldError.indexOf('EMAIL:') === 0) {
          if (this.fieldError === 'EMAIL:ALREADY_EXISTS') {
            this.$refs.profileContactForm.$el[5].setCustomValidity(this.$t('UsersManagement.message.userWithSameEmailAlreadyExists'));
          } else {
            const emailError = this.fieldError.replace('EMAIL:', '');
            this.$refs.profileContactForm.$el[5].setCustomValidity(emailError);
          }
        } else {
          error = error.message || String(error);
          const errorI18NKey = `UsersManagement.error.${error}`;
          const errorI18N = this.$t(errorI18NKey, {0: this.userToSave.fullname});
          if (errorI18N !== errorI18NKey) {
            error = errorI18N;
          }
          this.error = error;
          window.setTimeout(() => {
            this.error = null;
          }, 5000);
        }

        window.setTimeout(() => {
          if (!this.$refs.profileContactForm.validate() // Vuetify rules
            || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
            return;
          }
        }, 200);
      } else {
        this.error = null;
      }
    },
    refresh() {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'all')
        .then(user => {
          this.user = user;

          document.dispatchEvent(new CustomEvent('userModified', {detail: user}));
        })
        .catch((e) => {
          console.warn('Error while retrieving user details', e); // eslint-disable-line no-console
        });
    },
    cancel() {
      this.$refs.profileContactInformationDrawer.close();
    },
    open() {
      this.error = null;

      if (this.$refs.avatar) {
        this.$refs.avatar.reset();
      }

      // Avoid to change on original user
      this.userToSave = JSON.parse(JSON.stringify(this.user));
      if (!this.userToSave.phones || !this.userToSave.phones.length) {
        this.userToSave.phones =  [{}];
      }
      if (!this.userToSave.ims || !this.userToSave.ims.length) {
        this.userToSave.ims =  [{}];
      }
      if (!this.userToSave.urls || !this.userToSave.urls.length) {
        this.userToSave.urls =  [{}];
      }
      this.$refs.profileContactInformationDrawer.open();
    },
  },
};
</script>

<template>
  <exo-drawer
    ref="profileContactInformationDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    class="profileContactInformationDrawer"
    right>
    <template slot="title">
      {{ $t('profileContactInformation.contactInformation') }}
    </template>
    <template slot="content">
      <v-form
        ref="profileContactForm"
        class="form-horizontal"
        flat>
        <div v-for="property in properties" :key="property.id">
          <profile-contact-edit-multi-field
            v-if="property.multiValued || property?.children?.length"
            :property="property"
            @propertyUpdated="propertyUpdated" />
          <div v-else>
            <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
              {{ getResolvedName(property) }}
              <span v-if="property.required">*</span>
            </v-card-text>
            <v-card-text class="d-flex py-0">
              <v-card-text
                :title="disabledFieldTitle(property)"
                class="d-flex pa-0">
                <input
                  v-model="property.value"
                  :disabled="saving || disabledField(property)"
                  :type="property.propertyName==='email' ? 'email' : 'text'"
                  class="ignore-vuetify-classes"
                  maxlength="2000"
                  :required="property.required"
                  :ref="`${property.propertyName}Input`" 
                  @change="propertyUpdated(property)"
                  @input="propertyUpdated(property)">
              </v-card-text>
              <profile-hide-property-button
                :property="property" />
            </v-card-text>
          </div>
        </div>
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
          :disabled="disabled"
          :loading="saving"
          class="btn btn-primary"
          @click="save">
          {{ $t('profileContactInformation.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    propertiesToSave: [],
    properties: [],
    saving: null,
    fieldError: false,
    disabled: true,
    disabledFields: ['firstName', 'lastName', 'email']
  }),
  created() {
    this.$root.$on('open-profile-contact-information-drawer', this.open);
    this.$root.$on('hide-profile-property', this.hideProperty);
    this.$root.$on('show-profile-property', this.showProperty);
  },
  methods: {
    hideProperty(property) {
      property.hidden = true;
      property.toHide = true;
      this.propertyUpdated(property);
    },
    showProperty(property) {
      property.hidden = false;
      property.toShow = true;
      this.propertyUpdated(property);
    },
    resetCustomValidity() {
      if (this.$refs.emailInput) { this.$refs.emailInput[0].setCustomValidity('');}
      if (this.$refs.firstNameInput) { this.$refs.firstNameInput[0].setCustomValidity('');}
      if (this.$refs.lastNameInput) { this.$refs.lastNameInput[0].setCustomValidity('');}
      this.$root.$emit('reset-custom-validity');   
    },
    disabledSynchronizedField(property) {
      return !property.internal && this.disabledFields.includes(property.propertyName);
    },
    disabledField(property) {
      return !property.editable || this.disabledSynchronizedField(property);
    },
    disabledFieldTitle(property) {
      return this.disabledSynchronizedField(property) && this.$t('profileContactInformation.synchronizedUser.tooltip')
                                                      || this.disabledField(property)
                                                      && this.$t('profileContactInformation.nonEditable.field')
                                                      || this.$t(`profileContactInformation.${property.propertyName}`);
    },
    save() {
      this.fieldError = false;
      
      this.resetCustomValidity();
      let proptocheck = this.propertiesToSave.find(property => property.propertyName === 'urls');
      if (proptocheck && proptocheck.children.length > 0) {
        let errorFound = false;
        proptocheck.children.forEach(property => {
          if (!property.value || property.value.length===0 || !String(property.value).match(/((http(s)?:\/\/.)|www.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}/g)){
            this.$root.$emit('non-valid-url-input', property.value);
            errorFound = true;
          }
        });
        if (!errorFound){
          this.$root.$emit('reset-custom-validity');
        }
      }

      proptocheck = this.propertiesToSave.find(property => property.propertyName === 'email');
      if (proptocheck){
        if ((proptocheck.value && (proptocheck.value.length > 100 || proptocheck.value.length  < 10)) || !proptocheck.value) {
          this.$refs.emailInput[0].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileContactInformation.email'),
            1: 10,
            2: 100,
          }));
        } else {
          this.$refs.emailInput[0].setCustomValidity('');
        }
      } 
      

      if (!this.$refs.profileContactForm.validate() // Vuetify rules
          || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.$refs.profileContactInformationDrawer.startLoading();
      this.saving = true;
     
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${eXo.env.portal.userName}/profile/properties`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.propertiesToSave),
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
          this.disabled = true;
          this.$refs.profileContactInformationDrawer.endLoading();
        });
    },

    handleError(error) {
      this.resetCustomValidity();
      
      if (error) {
        if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
          this.$root.$emit('alert-message', this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.uploadLimit}), 'error');
        } else if (this.fieldError && this.fieldError.indexOf('FIRSTNAME:') === 0) {
          const firstNameError = this.fieldError.replace('FIRSTNAME:', '');
          if (this.$refs.firstNameInput) { this.$refs.firstNameInput[0].setCustomValidity(firstNameError);}
        } else if (this.fieldError && this.fieldError.indexOf('LASTNAME:') === 0) {
          const lastNameError = this.fieldError.replace('LASTNAME:', '');
          if (this.$refs.lastNameInput) { this.$refs.lastNameInput[0].setCustomValidity(lastNameError);}
        } else if (this.fieldError && this.fieldError.indexOf('EMAIL:') === 0) {
          if (this.fieldError === 'EMAIL:ALREADY_EXISTS') {
            if (this.$refs.emailInput) { this.$refs.emailInput[0].setCustomValidity(this.$t('UsersManagement.message.userWithSameEmailAlreadyExists'));}
          } else {
            const emailError = this.fieldError.replace('EMAIL:', '');
            if (this.$refs.emailInput) { this.$refs.emailInput[0].setCustomValidity(emailError); }
          }
        } else {
          error = error.message || String(error);
          const errorI18NKey = `UsersManagement.error.${error}`;
          const errorI18N = this.$t(errorI18NKey, {0: this.userToSave.fullname});
          if (errorI18N !== errorI18NKey) {
            error = errorI18N;
          }
          this.$root.$emit('alert-message', error, 'error');
        }

        window.setTimeout(() => {
          if (!this.$refs.profileContactForm.validate() // Vuetify rules
            || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
            return;
          }
        }, 200);
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
    refresh() {
      this.propertiesToSave = [];
      document.dispatchEvent(new CustomEvent('userPropertiesModified'));
    }, 
    cancel() {
      this.refresh();
      this.$refs.profileContactInformationDrawer.close();
    },
    open(event) {
      this.error = null;
      if (event) {
        this.properties = JSON.parse(JSON.stringify(event));
      }
      this.$refs.profileContactInformationDrawer.open();
    },
    propertyUpdated(item){
      this.disabled = false;
      if (!this.propertiesToSave.some(e => e.id === item.id)) {
        this.propertiesToSave.push(item);
      }    
    },
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    }
  },
};
</script>

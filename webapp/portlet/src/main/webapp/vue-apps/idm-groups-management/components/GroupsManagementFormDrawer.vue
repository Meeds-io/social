<template>
  <exo-drawer
    id="groupFormDrawer"
    ref="groupFormDrawer"
    right
    @closed="drawer = false">
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-form
        ref="groupForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveGroup">
        <v-card-text class="d-flex groupNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('GroupsManagement.name') }}<template v-if="newGroup">*</template>
        </v-card-text>
        <v-card-text class="d-flex groupNameField py-0">
          <input
            ref="nameInput"
            v-model="group.groupName"
            :disabled="saving || !newGroup"
            :autofocus="drawer"
            :placeholder="$t('GroupsManagement.namePlaceholder')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex groupNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('GroupsManagement.label') }}*
        </v-card-text>
        <v-card-text class="d-flex groupNameField py-0">
          <input
            ref="labelInput"
            v-model="group.label"
            :disabled="saving"
            :placeholder="$t('GroupsManagement.labelPlaceholder')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex descriptionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('GroupsManagement.description') }}
        </v-card-text>
        <v-card-text class="d-flex descriptionField py-0">
          <textarea
            ref="descriptionInput"
            v-model="group.description"
            :disabled="saving"
            :placeholder="$t('GroupsManagement.descriptionPlaceholder')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1 textarea-no-resize"
            maxlength="2000"></textarea>
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
          @click="saveGroup">
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
    newGroup: false,
    saving: false,
    confirmNewPassword: null,
    group: {},
    parentGroup: null
  }),
  computed: {
    title() {
      if (this.newGroup) {
        return this.$t('GroupsManagement.addGroup');
      } else {
        return this.$t('GroupsManagement.editGroup');
      }
    },
  },
  watch: {
    confirmNewPassword() {
      this.resetCustomValidity();
    },
    saving() {
      if (this.saving) {
        this.$refs.groupFormDrawer.startLoading();
      } else {
        this.$refs.groupFormDrawer.endLoading();
      }
    },
    drawer() {
      if (this.drawer) {
        this.$refs.groupFormDrawer.open();
        window.setTimeout(() => {
          if (this.newGroup) {
            this.$refs.nameInput.focus();
          } else {
            this.$refs.labelInput.focus();
          }
        }, 200);
      } else {
        this.$refs.groupFormDrawer.close();
      }
    },
  },
  created() {
    this.$root.$on('addNewGroup', this.addNewGroup);
    this.$root.$on('editGroup', this.editGroup);
  },
  methods: {
    resetCustomValidity() {
      this.$refs.nameInput.setCustomValidity('');
    },
    addNewGroup(parentGroup) {
      this.parentGroup = parentGroup;
      this.group = {
        parentId: this.parentGroup && this.parentGroup.id || null,
      };
      this.newGroup = true;
      this.drawer = true;
    },
    editGroup(group) {
      this.group = {
        id: group.id,
        parentId: group.parentId,
        groupName: group.groupName,
        label: group.label,
        description: group.description,
      };
      this.newGroup = false;
      this.drawer = true;
    },
    saveGroup(event) {
      const regex =  /(?!^\d)^[a-zA-Z0-9-_]+$/;
      const isValid = regex.test(this.group.groupName);
      
      if (this.group.groupName) {
        if (!isValid) {
          this.$refs.nameInput.setCustomValidity(this.$t('GroupsManagement.error.invalidField', {0: this.$t('GroupsManagement.name')}));
        } else {
          if (this.group.groupName.length < 3 || this.group.groupName.length > 30) {
            this.$refs.nameInput.setCustomValidity(this.$t('GroupsManagement.message.invalidFieldLength', {
              0: this.$t('GroupsManagement.name'),
              1: 3,
              2: 50,
            }));
          } else {                  
            this.$refs.nameInput.setCustomValidity('');
          }
        }
      }
      
      if (this.group.label) {
        if (this.group.label.length < 3 || this.group.label.length > 50) {
          this.$refs.labelInput.setCustomValidity(this.$t('GroupsManagement.message.invalidFieldLength', {
            0: this.$t('GroupsManagement.label'),
            1: 3,
            2: 50,
          }));
        } else {
          this.$refs.labelInput.setCustomValidity('');
        }
      }

      if (this.group.description) {
        if (this.group.description.length < 3 || this.group.description.length > 255) {
          this.$refs.descriptionInput.setCustomValidity(this.$t('GroupsManagement.message.invalidFieldLength', {
            0: this.$t('GroupsManagement.description'),
            1: 0,
            2: 255,
          }));
        } else {
          this.$refs.descriptionInput.setCustomValidity('');
        }
      }
      
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.fieldError = false;

      if (!this.$refs.groupForm.validate() // Vuetify rules
          || !this.$refs.groupForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.saving = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups`, {
        method: this.newGroup && 'POST' || 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.group),
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
      }).then(() => this.$root.$emit('refreshGroup', this.group, this.parentGroup))
        .then(() => this.$refs.groupFormDrawer.close())
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
          this.$refs.nameInput.setCustomValidity(this.$t('GroupsManagement.message.sameNameAlreadyExists'));
        } else {
          this.$root.$emit('alert-message', error, 'error');
        }

        window.setTimeout(() => {
          if (!this.$refs.groupForm.validate() // Vuetify rules
              || !this.$refs.groupForm.$el.reportValidity()) { // Standard HTML rules
            return;
          }
        }, 200);
      }
    },
  },
};
</script>
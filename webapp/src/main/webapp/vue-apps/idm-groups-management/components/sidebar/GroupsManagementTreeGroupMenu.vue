<template>
  <div :id="groupMenuParentId">
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('GroupsManagement.title.confirmDelete')"
      :ok-label="$t('GroupsManagement.button.ok')"
      :cancel-label="$t('GroupsManagement.button.cancel')"
      @ok="deleteConfirm()" />
    <v-menu
      ref="actionMenu"
      v-model="displayActionMenu"
      :attach="`#${groupMenuParentId}`"
      transition="slide-x-reverse-transition"
      content-class="groupActionMenu"
      right
      offset-x
      offset-y>
      <template #activator="{ on }">
        <v-btn
          icon
          text
          v-on="on"
          @blur="closeMenu()">
          <v-icon size="18">fa-ellipsis-v</v-icon>
        </v-btn>
      </template>
      <v-list class="pa-0" dense>
        <v-list-item @click="emitEvent($event, 'editGroup')">
          <v-list-item-title>
            <i class="uiIcon uiIconEdit"></i>
            {{ $t('GroupsManagement.edit') }}
          </v-list-item-title>
        </v-list-item>
        <v-list-item @click="emitEvent($event, 'addNewGroup')">
          <v-list-item-title>
            <i class="uiIcon uiIconGroup"></i>
            {{ $t('GroupsManagement.addSubGroup') }}
          </v-list-item-title>
        </v-list-item>
        <v-list-item @click="emitEvent($event, 'addNewMembership')">
          <v-list-item-title>
            <i class="uiIcon uiIconSocConnectUser"></i>
            {{ $t('GroupsManagement.addMember') }}
          </v-list-item-title>
        </v-list-item>
        <v-list-item @click="deleteGroup">
          <v-list-item-title>
            <i class="uiIcon uiIconTrash"></i>
            {{ $t('GroupsManagement.delete') }}
          </v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
export default {
  props: {
    group: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    displayActionMenu: false,
  }),
  computed: {
    deleteConfirmMessage() {
      return this.$t('GroupsManagement.message.confirmDelete', {0: this.group.label});
    },
    groupMenuParentId() {
      return `SpaceApplications${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`;
    },
  },
  methods: {
    closeMenu(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.displayActionMenu = false;
    },
    emitEvent(event, eventName) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$root.$emit(eventName, this.group);
    },
    deleteGroup(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$refs.deleteConfirmDialog.open();
    },
    deleteConfirm() {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups?groupId=${this.group.id}`, {
        method: 'DELETE',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp && resp.status === 400) {
            return resp.text().then(error => {
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          }
        }
        return this.$root.$emit('removeGroup', this.group);
      }).catch(error => {
        error = error.message || String(error);
        const errorI18NKey = `GroupsManagement.error.${error}`;
        const errorI18N = this.$t(errorI18NKey, {0: this.group.label});
        if (errorI18N !== errorI18NKey) {
          error = errorI18N;
        }
        this.$root.$emit('alert-message', error, 'error');
      }).finally(() => this.loading = false);
    },
  },
};
</script>

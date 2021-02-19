<template>
  <v-app>
    <v-card-text>
      <div>
        <v-chip-group
          active-class="primary--text"
          column
        >
          <v-row no-gutters>
            <div class="my-auto permissionsTitleClass">
              <h4 class="font-weight-bold ma-0">{{ $t('items.dlp.edit.permissions') }}</h4>
            </div>
            <v-chip
              v-for="permission in permissions"
              :key="permission"
              outlined
              class="my-1"
            >
              {{ permission.groupName }}
            </v-chip>

            <div class="my-auto">
              <v-btn
                v-exo-tooltip.bottom.body="$t('items.dlp.edit.permissions.tooltip')"
                v-if="isAdmin"
                icon
                outlined
                small
                @click="editPermissions">
                <i class="uiIconEdit uiIconLightBlue pb-2" />
              </v-btn>
            </div>
          </v-row>
        </v-chip-group>
      </div>
    </v-card-text>
    <exo-drawer
      ref="EditPermissionsDrawer"
      class="EditPermissionsDrawer"
      right
      @closed="cleanInput">
      <template slot="title">
        {{ $t('items.dlp.edit.permissions.title') }}
      </template>
      <template slot="content">
        <v-card-text class="pa-4">
          <v-container class="px-0">
            <v-autocomplete
              ref="permissionsInput"
              v-model="selectedItems"
              :loading="searchLoading"
              :items="searchResults"
              :search-input.sync="searchFiled"
              :placeholder="$t('items.dlp.permissions.SearchLabel')"
              menu-props="closeOnClick, maxHeight = 100"
              return-object
              class="searchPermissions pt-0"
              flat
              hide-no-data
              hide-details
              solo-inverted
              hide-selected
              chips
              multiple
              attach
              dense
              dark
              item-text="groupName"
              item-value="id"
              @input="selectionChange"
              @change="clearSearch"
              @update:search-input="searchFiled = $event">
              <template slot="selection" slot-scope="data">
                <v-chip-group
                  active-class="primary--text"
                  column
                >
                  <v-chip
                    :input-value="data"
                    close
                    light
                    small
                    class="chip--select-multi"
                    @click:close="removeSelection(data.item)">
                    {{ data.item.groupName }}
                  </v-chip>
                </v-chip-group>
              </template>
              <template
                slot="item"
                slot-scope="{ item, parent }"
                class="permissionsItem">
                <v-list-tile-avatar><img :src="item.avatarUrl" class="permissionsItemAvatar"></v-list-tile-avatar>
                <v-list-tile-content class="permissionsItemName">
                  <v-list-tile-title v-sanitized-html="parent.genFilteredText(item.groupName)" />
                </v-list-tile-content>
              </template>
            </v-autocomplete>
          </v-container>
        </v-card-text>
        <v-card-text>
          <v-chip-group
            active-class="primary--text"
            column
          >
            <v-chip
              v-for="permission in selectedItems"
              :key="permission"
              close
              outlined
              class="my-1"
              @click:close="removeSelection(permission)"
            >
              {{ permission.groupName }}
            </v-chip>
          </v-chip-group>
        </v-card-text>
        <v-card-text v-if="error">
          <v-alert type="error">
            {{ error }}
          </v-alert>
        </v-card-text>
      </template>
      <template slot="footer">
        <div class="d-flex">
          <v-spacer />
          <v-btn
            class="btn mr-2"
            @click="cancel">
            {{ $t('items.dlp.button.cancel') }}
          </v-btn>
          <v-btn
            class="btn btn-primary"
            @click="saveDlpPermissions">
            {{ $t('items.dlp.button.save') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>
import {checkIsAdminMemberGroup, getPermissionsData, getDlpPermissions, saveDlpPermissions} from '../dlpAdministrationServices';

export default {
  data () {
    return {
      selectedItems: [],
      permissions: [],
      searchLoading: false,
      isAdmin: false,
      searchResults: [],
      savedPermissions: [],
      existingPermissions: [],
      searchFiled: null,
      error: null,
    };
  },
  watch: {
    searchFiled(value) {
      return value && value !== this.selectedItems && this.querySelections(value);
    },
  },
  created() {
    this.getDlpPermissions();
    checkIsAdminMemberGroup().then(data => {
      this.isAdmin = data.isAdmin && data.isAdmin === 'true';
    });
  },
  methods : {
    async querySelections(query) {
      this.searchLoading = true;
      try {
        const data = await getPermissionsData(`${query}`);
        this.searchResults = data.entities.filter(
          ({ groupName }) => (groupName || '').toLowerCase().indexOf((query || '').toLowerCase()) > -1
        ).filter(el =>  el.id !== '/spaces' && !(el.parentId !== null && el.parentId === '/spaces'))
          .filter(el => !this.existingPermissions.map(item => item.id).includes(el.id));
        this.searchLoading = false;
      } catch (err) {
        this.error = err.message;
      }
    },
    getDlpPermissions() {
      getDlpPermissions().then(data => {
        if (data) {
          this.permissions = data.entities;
          this.selectedItems = data.entities;
        }
      });
    },
    removeSelection(value) {
      this.selectedItems = this.selectedItems.filter(({ id }) => id !== value.id);
    },
    cancel() {
      this.$refs.EditPermissionsDrawer.close();
      this.searchResults = '';
      this.searchFiled = '';
      this.selectedItems = this.permissions;
      this.error = null;
    },
    selectionChange(selection) {
      this.searchFiled = '';
    },
    editPermissions() {
      this.$refs.EditPermissionsDrawer.open();
    },
    saveDlpPermissions() {
      saveDlpPermissions(this.selectedItems.map(item => item.id).join());
      this.$refs.EditPermissionsDrawer.close();
      this.permissions = this.selectedItems;
      this.searchResults = '';
    },
    cleanInput() {
      this.searchResults = '';
      this.searchFiled = '';
      this.error = null;
    },
  }
};
</script>

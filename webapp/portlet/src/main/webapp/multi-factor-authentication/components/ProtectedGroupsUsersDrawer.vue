<template>
  <exo-drawer
    id="protectedGroupsUsersDrawer"
    ref="protectedGroupsUsersDrawer"
    right
    @closed="drawer = false">
    <template slot="title">
      {{ $t('authentication.multifactor.protected.groups.users.title') }}
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
            :placeholder="$t('authentication.multifactor.protected.groups.users.placeholder')"
            menu-props="closeOnClick, maxHeight = 100"
            return-object
            class="inputGroups pt-0"
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
                column>
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
          column>
          <v-chip
            v-for="group in selectedItems"
            :key="group"
            close
            outlined
            class="my-1"
            @click:close="removeSelection(group)">
            {{ group.groupName }}
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
      <div class="d-flex ">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="cancel">
          {{ $t('authentication.multifactor.protected.resources.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="save">
          {{ $t('authentication.multifactor.protected.resources.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import {getGroups} from '../multiFactorServices';
export default {
  data: () => ({
    drawer: false,
    selectedItems: [],
    groups: [],
    searchLoading: false,
    searchResults: [],
    existingGroups: [],
    searchFiled: null,
    error: null,
  }),
  watch: {
    drawer() {
      if (this.drawer) {
        this.$refs.protectedGroupsUsersDrawer.open();
      } else {
        this.$refs.protectedGroupsUsersDrawer.close();
      }
    },
    searchFiled(value) {
      return value && value !== this.selectedItems && this.querySelections(value);
    },
  },
  created() {
    this.$root.$on('protectedGroupsUsers', this.protectedGroupsUsers);
  },
  methods: {
    protectedGroupsUsers() {
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
      this.searchResults = '';
      this.searchFiled = '';
      this.selectedItems = this.groups;
      this.error = null;
    },
    save() {
      this.$refs.protectedGroupsUsersDrawer.close();
      this.$root.$emit('protectedGroupsList', this.selectedItems);
    },
    async querySelections(query) {
      this.searchLoading = true;
      try {
        const data = await getGroups(`${query}`);
        this.searchResults = data.entities.filter(
          ({ groupName }) => (groupName || '').toLowerCase().indexOf((query || '').toLowerCase()) > -1
        ).filter(el =>  el.id !== '/spaces' && !(el.parentId !== null && el.parentId === '/spaces'))
          .filter(el => !this.existingGroups.map(item => item.id).includes(el.id));
        this.searchLoading = false;
      } catch (err) {
        this.error = err.message;
      }
    },
    removeSelection(value) {
      this.selectedItems = this.selectedItems.filter(({ id }) => id !== value.id);
    },
    cleanInput() {
      this.searchResults = '';
      this.searchFiled = '';
      this.error = null;
    },
  },
};
</script>

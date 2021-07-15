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
      <v-flex xs12 class="pa-3">
        <exo-group-suggester
          v-model="groups"
          :options="suggesterOptions"
          :bound-groups="groups"
          :source-providers="[findGroups]"
          :placeholder="$t('authentication.multifactor.protected.groups.users.placeholder')" />
      </v-flex>
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
import {getGroups, getProtectedGroups, saveProtectedGroups} from '../multiFactorServices';
export default {
  data () {
    const component = this;
    return {
      drawer: false,
      groups: [],
      selectedGroups: [],
      searchLoading: false,
      error: null,
      suggesterOptions: {
        type: 'tag',
        plugins: ['remove_button', 'restore_on_backspace'],
        create: false,
        createOnBlur: false,
        highlight: false,
        openOnFocus: false,
        valueField: 'value',
        labelField: 'text',
        searchField: ['text'],
        closeAfterSelect: false,
        dropdownParent: 'body',
        hideSelected: false,
        fillSelectize: true,
        renderMenuItem(item, escape) {
          return component.renderMenuItem(item, escape);
        },
        sortField: [{field: 'order'}, {field: '$score'}],
      }
    };
  },
  watch: {
    drawer() {
      if (this.drawer) {
        this.$refs.protectedGroupsUsersDrawer.open();
      } else {
        this.$refs.protectedGroupsUsersDrawer.close();
      }
    },
  },
  created() {
    this.$root.$on('protectedGroupsUsers', this.protectedGroupsUsers);
    this.getProtectedGroups();
  },
  methods: {
    protectedGroupsUsers() {
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
      this.error = null;
    },
    save() {
      saveProtectedGroups(this.groups.join(','));
      this.$root.$emit('protectedGroupsList', this.groups);
      this.$refs.protectedGroupsUsersDrawer.close();
    },
    getProtectedGroups() {
      getProtectedGroups().then(data => {
        for (const group of data.protectedGroups) {
          this.groups.push(group);
        }
      });
    },
    findGroups (query, callback) {
      if (!query.length) {
        return callback();
      }
      getGroups(query).then(data => {
        const groups = [];
        for (const group of data.entities) {
          groups.push({
            avatarUrl: null,
            text: group.label,
            value: group.id,
            type: 'group'
          });
        }
        callback(groups);
      });
    },
    renderMenuItem (item, escape) {
      return `
        <div class="item" title="${escape(item.value)}" rel="tooltip" data-placement="bottom">${escape(item.value)}</div>
      `;
    },
  },
};
</script>

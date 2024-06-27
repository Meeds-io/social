<template>
  <exo-drawer
    id="GroupBindingForm"
    v-model="drawer"
    ref="drawer"
    right
    @closed="$emit('close')">
    <template #title>
      {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.title') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        <div class="d-flex align-center mb-4">
          <img
            v-if="spaceToBind && spaceToBind.avatarUrl != null"
            :src="spaceToBind.avatarUrl"
            class="avatar">
          <img
            v-else
            :src="avatar"
            class="avatar">
          <span class="ps-2">{{ spaceToBind ? spaceToBind.displayName : '' }} </span>
        </div>
        <div class="d-flex mb-4">
          <exo-group-suggester
            v-model="groups"
            :options="suggesterOptions"
            :source-providers="[findGroups]"
            :bound-groups="groupSpaceBindings"
            :second-drawer-selected-groups="secondDrawerSelectedGroups"
            :placeholder="$t('social.spaces.administration.manageSpaces.spaceBindingForm.textField.placeHolder')" />
          <v-btn
            :title="$t('social.spaces.administration.manageSpaces.spaceBindingForm.selectList')"
            icon
            class="rightIcon"
            @click="goToSelectGroups">
            <i class="uiIconSpaceBinding uiIconGroup"></i>
          </v-btn>
        </div>
        <v-layout v-if="groupSpaceBindings.length > 0 && !boundGroupsLoading" column>
          <v-flex>
            <span>
              {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.boundGroups') }}
            </span>
          </v-flex>
          <v-flex class="boundGroups" mt-2>
            <v-list
              flat
              subheader
              dense>
              <v-list-item-group>
                <div v-for="(binding, index) in groupSpaceBindings" :key="index">
                  <v-list-item>
                    <v-list-item-content>
                      {{ renderGroupName(binding.group) }}
                    </v-list-item-content>
                    <v-list-item-action class="delete">
                      <v-btn
                        small
                        icon
                        class="rightIcon"
                        @click="$emit('openRemoveBindingModal', binding)">
                        <i class="uiIconDeleteUser uiIconLightGray"></i>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </div>              
              </v-list-item-group>
            </v-list>
          </v-flex>
        </v-layout>
      </div>
      <exo-group-binding-second-level-drawer
        ref="groupSelection"
        :already-selected="groups"
        :group-space-bindings="groupSpaceBindings"
        @selectionSaved="selectionSaved" />
    </template>
    <template #footer>
      <div class="d-flex justify-end">
        <v-btn
          class="btn me-2"
          @click="cancelBinding">
          {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!isAllowToSave"
          class="btn btn-primary"
          @click="$emit('openBindingModal', groups)">
          {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    spaceToBind: {
      type: String,
      default: null,
    },
    groupSpaceBindings: {
      type: Array,
      default: null,
    },
    boundGroupsLoading: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    const component = this;
    return {
      textAreaValue: '',
      groups: [],
      drawer: false,
      avatar: this.$spacesConstants && this.$spacesConstants.DEFAULT_SPACE_AVATAR,
      secondDrawerSelectedGroups: [],
      suggesterOptions: {
        type: 'tag',
        plugins: ['remove_button', 'restore_on_backspace'],
        create: false,
        createOnBlur: false,
        valueField: 'value',
        labelField: 'text',
        searchField: ['text'],
        closeAfterSelect: false,
        dropdownParent: 'body',
        hideSelected: true,
        renderMenuItem (item, escape) {
          return component.renderMenuItem(item, escape);
        },
        sortField: [{field: 'order'}, {field: '$score'}],
      }
    };
  },
  computed: {
    isAllowToSave() {
      return this.groups?.length;
    },
  },
  methods: {
    findGroups (query, callback) {
      if (!query.length) {
        return callback();
      }
      this.$spacesAdministrationServices.getGroups(query).then(data => {
        const groups = [];
        const boundGroups = this.groupSpaceBindings.map(binding => binding.group);
        for (const group of data.entities) {
          if (!group.id.startsWith('/spaces') && !boundGroups.includes(group.id)) {
            groups.push({
              avatarUrl: null,
              text: group.label,
              value: group.id,
              type: 'group'
            });
          }
        }
        callback(groups);
      });
    },
    renderMenuItem (item, escape) {
      return `
        <div class="item" title="${escape(item.value)}" rel="tooltip" data-placement="bottom">${escape(item.value)}</div>
      `;
    },
    goToSelectGroups() {
      this.$refs.groupSelection.open();
    },
    open() {
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    cancelBinding() {
      this.groups = [];
      this.close();
    },
    renderGroupName(groupName) {
      let groupPrettyName = groupName.slice(groupName.lastIndexOf('/') + 1, groupName.length);
      groupPrettyName = groupPrettyName.charAt(0).toUpperCase() + groupPrettyName.slice(1);
      return `${groupPrettyName} (${groupName})`;
    },
    selectionSaved(saved) {
      this.secondDrawerSelectedGroups = saved;
      this.groups = saved.map(group => group.id);
    },
  }
};
</script>

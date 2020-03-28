<template>
  <div class="secondDrawer">
    <v-card-title
      class="title">
      <v-layout
        pa-2
        justify-center
        align-baseline
        wrap
        row>
        <v-flex
          align-self-end
          xs1>
          <v-btn
            icon
            class="leftIcon"
            @click="$emit('back')">
            <v-icon small>arrow_back</v-icon>
          </v-btn>
        </v-flex>
        <v-flex>
          <span class="subtitle-1">
            {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.selectGroups') }}
          </span>
        </v-flex>
        <v-flex xs4>
          <v-text-field
            v-model="search"
            append-icon="search"
            class="treeGroupSearch"
            single-line
            solo
            flat
            hide-details>
          </v-text-field>
        </v-flex>
        <v-flex xs1 mr-2>
          <v-btn
            icon
            class="rightIcon"
            @click="closeDrawer">
            <v-icon
              large
              class="closeIcon">
              close
            </v-icon>
          </v-btn>
        </v-flex>
      </v-layout>
    </v-card-title>
    <v-layout pt-4 pl-2 class="content">
      <v-flex>
        <div v-show="loading">
          <v-flex pt-4 pl-4 pr-8>
            <v-skeleton-loader
              class="mx-auto"
              type="paragraph@3">
            </v-skeleton-loader>
          </v-flex>
        </div>
        <v-treeview
          v-show="!loading && !searching"
          v-model="selection"
          :items="items"
          :active="selection"
          selection-type="independent"
          expand-icon="mdi-chevron-down"
          dense
          shaped
          hoverable
          selectable
          open-on-click
          return-object>
        </v-treeview>
        <v-treeview
          v-show="searching"
          v-model="selection"
          :items="items"
          :search="search"
          :open="allItems"
          :active="active"
          selection-type="independent"
          dense
          shaped
          hoverable
          selectable
          activatable
          multiple-active
          open-on-click
          return-object>
        </v-treeview>
      </v-flex>
    </v-layout>
    <v-card-actions absolute class="drawerActions">
      <v-layout>
        <v-flex class="xs6"></v-flex>
        <button type="button" class="btn ml-2" @click="cancelSelection">{{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.cancel') }}</button>
        <button :disabled="!isAllowToSave" type="button" class="btn btn-primary ml-6" @click="saveSelection">
          {{ $t('social.spaces.administration.manageSpaces.spaceBindingForm.save') }}
        </button>
      </v-layout>
    </v-card-actions>      
  </div>
</template>

<script>
import * as spacesAdministrationServices from '../../spacesAdministrationServices';

export default {
  props: {
    alreadySelected: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      loading: true,
      selection: [],
      items: [],
      search: null,
      allItems: [],
      active: [],
      confirmedSelection: [],
    };
  },
  computed : {
    isAllowToSave() {
      // retrieve none confirmed selections
      const nonConfirmedSelection = [];
      const confirmed = this.confirmedSelection.map(group => group.id);
      const allSelection = this.selection.map(group => group.id);
      // check for non confirmed changes
      allSelection.forEach(groupId => {
        if (!confirmed.includes(groupId)) {
          nonConfirmedSelection.push(groupId);
        }
      });
      confirmed.forEach(groupId => {
        if (!allSelection.includes(groupId)) {
          nonConfirmedSelection.push(groupId);
        }
      });
      return this.selection && this.selection.length > 0 && nonConfirmedSelection.length > 0;
    },
    searching() {
      return this.search;
    },
  },
  watch: {
    alreadySelected() {
      const selected = this.alreadySelected.map(groupId => ({
        id: groupId,
        name: groupId,
      }));
      if (this.confirmedSelection.length === 0) {
        this.confirmedSelection.push(...selected);
        this.selection.push(...selected);
      } else {
        const removedGroupIds = [];
        const  addedGroupIds = [];
        
        // retrieve removed items
        const confirmed = this.confirmedSelection.map(item => item.id);
        confirmed.forEach(groupId => {
          if (!this.alreadySelected.includes(groupId)) {
            removedGroupIds.push(groupId);
          }
        });
        
        // retrieve added items
        this.alreadySelected.forEach(groupId => {
          if (!confirmed.includes(groupId)) {
            addedGroupIds.push(groupId);
          }
        });
        
        // synchronize confirmed selection
        removedGroupIds.forEach(groupId => {
          this.selection = this.selection.filter(group => group.id !== groupId);
        });
        addedGroupIds.forEach(groupId => {
          this.selection.push({
            id: groupId,
            name: groupId
          });
        });
        // update confirmed selection
        this.confirmedSelection = [];
        this.confirmedSelection.push(...selected);
      }
    },
    search() {
      if (this.search) {
        this.active = this.allItems.filter(item => item.name.toLowerCase().match(this.search.toLowerCase()));
      } else {
        // init activate.
        this.active = [];
      }
    },
  },
  created() {
    this.getRootChildGroups();
  },
  methods : {
    getRootChildGroups() {
      spacesAdministrationServices.getGroupsTree(null).then(data => {
        this.items = data.childGroups;
      }).finally(() => {
        this.loading = false;
        this.getAllGroups();
      });
    },
    getAllGroups() {
      //TODO optimize.
      // Get all groups by level, max set to 5.
      const firstChildren = [];
      const secondChildren = [];
      const thirdChildren = [];
      const fourthChildren = [];
      const fifthChildren = [];

      this.allItems.push(...this.items);

      // level 1:
      this.items.forEach(child => {
        const children = child.children;
        firstChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 2:
      firstChildren.forEach(child => {
        const children = child.children;
        secondChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 3:
      secondChildren.forEach(child => {
        const children = child.children;
        thirdChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 4:
      thirdChildren.forEach(child => {
        const children = child.children;
        fourthChildren.push(...children);
        this.allItems.push(...children);
      });

      // level 5:
      fourthChildren.forEach(child => {
        const children = child.children;
        fifthChildren.push(...children);
        this.allItems.push(...children);
      });
    },
    closeDrawer() {
      this.selection = [];
      this.$emit('close');
    },
    cancelSelection() {
      this.selection = [];
    },
    saveSelection() {
      this.$emit('selectionSaved', this.selection.map(group => group.id));
    },
  },
};
</script>

<style scoped>

</style>
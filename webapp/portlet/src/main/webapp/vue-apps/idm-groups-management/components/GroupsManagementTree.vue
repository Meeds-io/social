<template>
  <v-list-item-group v-model="selectedIndex" color="primary">
    <groups-management-tree-item
      v-for="child in groups"
      :key="child.id"
      :group="child"
      :page-size="pageSize"
      :loading="loading"
      :open-all="!!keyword"
      class="ms-3" />
  </v-list-item-group>
</template>

<script>
export default {
  data: () => ({
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    keyword: null,
    groups: [],
    searching: 0,
    loading: 0,
    pageSize: 50,
    selectedIndex: -1,
  }),
  watch: {
    selectedIndex() {
      if (this.selectedIndex !== 0 && !this.selectedIndex || this.selectedIndex < 0) {
        this.$root.$emit('selectGroup');
      }
    },
    searching() {
      this.$root.$emit('searchingGroup', this.searching);
    },
    keyword() {
      this.selectedIndex = -1;

      if (!this.keyword) {
        return this.retrieveGroupTree();
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loading) {
        this.searching++;
        this.waitForEndTyping();
      }
    },
  },
  created() {
    this.$root.$on('refreshGroup', this.updateGroupTree);
    this.$root.$on('removeGroup', this.removeGroup);
    this.$root.$on('retrieveGroupChildren', this.retrieveGroupTree);
    this.$root.$on('searchGroup', keyword => this.keyword = keyword);

    this.retrieveGroupTree()
      .finally(() => this.$root.$applicationLoaded());
  },
  methods: {
    updateGroupItem(group, groups) {
      this.loading = true;
      try {
        if (!groups) {
          groups = this.groups;
        }
        const groupIndex = groups.findIndex(displayedGroup => displayedGroup.id === group.id);
        if (groupIndex > -1) {
          groups.splice(groupIndex, 1, group);
          this.$forceUpdate();
          return;
        }
        for (const index in groups) {
          const displayedGroup = groups[index];
          if (group.parentId
              && displayedGroup.children
              && displayedGroup.children.length
              && group.parentId.indexOf(displayedGroup.id) === 0) {
            this.$forceUpdate();
            return this.updateGroupItem(group, displayedGroup.children);
          }
        }
      } finally {
        this.$nextTick().then(() => this.loading = false);
      }
    },
    updateGroupTree(group, parentGroup, groups) {
      this.loading = true;
      try {
        if (!groups) {
          groups = this.groups;
        }
        const parentId = parentGroup && parentGroup.id || group.parentId;
        if (!group.id) {
          group.id = group.parentId ? `${group.parentId}/${group.groupName}` : group.groupName;
        }
        for (const index in groups) {
          const displayedGroup = groups[index];
          if (group.id === displayedGroup.id) {
            Object.assign(displayedGroup, group);
            return true;
          } else if (parentId && parentId.indexOf(displayedGroup.id) === 0) {
            let updated = false;
            if (displayedGroup.children && displayedGroup.children.length) {
              updated = this.updateGroupTree(group, parentGroup, displayedGroup.children);
            }
            if (updated) {
              return;
            } else if (parentId === displayedGroup.id) {
              if (displayedGroup.childrenRetrieved) {
                if (!displayedGroup.children || !displayedGroup.children.length) {
                  displayedGroup.children = [group];
                } else {
                  displayedGroup.children.push(group);
                  displayedGroup.children = displayedGroup.children.slice();
                }
                this.updateGroupItem(displayedGroup);
              }
              return;
            }
          }
        }
        if (!parentId) {
          this.groups.push(group);
        }
      } finally {
        this.$nextTick().then(() => this.loading = false);
      }
    },
    removeGroup(group, parentGroup, groups) {
      this.loading = true;
      try {
        if (!groups) {
          groups = this.groups;
        }
        const groupIndex = groups.findIndex(displayedGroup => displayedGroup.id === group.id);
        if (groupIndex > -1) {
          groups.splice(groupIndex, 1);
          if (parentGroup) {
            if (!groups.length) {
              delete parentGroup.children;
            }
            this.updateGroupItem(parentGroup);
          }
          this.$forceUpdate();
          return;
        }
        for (const index in groups) {
          const displayedGroup = groups[index];
          if (group.parentId
              && displayedGroup.children
              && displayedGroup.children.length
              && group.parentId.indexOf(displayedGroup.id) === 0) {
            return this.removeGroup(group, displayedGroup, displayedGroup.children);
          }
        }
      } finally {
        this.$nextTick().then(() => this.loading = false);
      }
    },
    searchGroup(groupId, groups) {
      if (!groups) {
        groups = this.groups;
      }
      const group = groups.find(displayedGroup => displayedGroup.id === groupId);
      if (group) {
        return group;
      }
      for (const index in groups) {
        const displayedGroup = groups[index];
        if (displayedGroup.children
            && displayedGroup.children.length
            && groupId.indexOf(displayedGroup.id) === 0) {
          const foundGroup = this.searchGroup(groupId, displayedGroup.children);
          if (foundGroup) {
            return foundGroup;
          }
        }
      }
    },
    retrieveGroupTree(parentGroup, offset) {
      const parentId = parentGroup && parentGroup.id || '';
      if (parentGroup) {
        parentGroup.loading = true;
      }
      this.loading++;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/tree?parentId=${parentId}&q=${this.keyword || ''}&&offset=${offset || 0}&limit=${this.pageSize}&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(data => {
        const groups = data && data.entities || [];
        if (parentGroup) {
          parentGroup.childrenSize = data && data.size || [];
          if (groups.length) {
            parentGroup.children = groups;
          }
        } else {
          this.groups = groups;
        }
        return this.$nextTick();
      }).finally(() => {
        if (parentGroup) {
          parentGroup.childrenRetrieved = true;
          parentGroup.loading = false;
        }
        this.loading--;
      });
    },
    searchGroups() {
      this.loading++;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups?q=${this.keyword || ''}&offset=0&limit=${this.pageSize}&tree=true&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(data => {
        this.groups = data && data.entities || [];
      }).finally(() => {
        this.loading--;
        this.searching--;
      });
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchGroups();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  },
};
</script>
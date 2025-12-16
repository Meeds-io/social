<template>
  <v-menu
    v-if="displaySpaceCreationMenu"
    v-model="menu"
    ref="addNewSpaceButton"
    content-class="position-absolute application-menu z-index-modal"
    :left="left"
    offset-y>
    <template #activator="{attrs, on}">
      <v-btn
        id="addNewSpaceButton"
        :title="$t('menu.spaces.addNewSpaceTooltip')"
        :small="!icon && $root.isMobile"
        :color="color"
        :icon="icon"
        :elevation="elevation"
        v-bind="attrs"
        v-on="on">
        <v-icon :size="iconSize">fa-plus</v-icon>
        <span v-if="displayLabel" class="ms-2 hidden-xs-only">
          {{ $t('spacesList.button.add') }}
        </span>
      </v-btn>
    </template>
    <v-list dense class="layout-side-bar"
            max-width="auto"
            min-width="auto"
            width="auto">
      <v-list-item @click="addNewSpace">
        <v-list-item-content class="ms-0 text-body my-auto">
        <v-list-item-title>
          {{ $t('spacesList.createMainSpace') }}
        </v-list-item-title>
        </v-list-item-content>
      </v-list-item>

      <v-list-item @click="addNewSubSpace">
        <v-list-item-content class="ms-0 text-body my-auto">
        <v-list-item-title>
          {{ $t('spacesList.createSubSpace') }}
        </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-btn
    v-else
    id="addNewSpaceButton"
    :title="$t('menu.spaces.addNewSpaceTooltip')"
    :small="!icon && $root.isMobile"
    :color="color"
    :icon="icon"
    :elevation="elevation"
    v-bind="attrs"
    v-on="on"
    @click="addNewSpace">
    <v-icon :size="iconSize">fa-plus</v-icon>
    <span v-if="displayLabel" class="ms-2 hidden-xs-only">
      {{ $t('spacesList.button.add') }}
    </span>
  </v-btn>
</template>

<script>
export default {
  props: {
    color: {
      type: String,
      default: '',
    },
    icon: {
      type: Boolean,
      default: false,
    },
    displayLabel: {
      type: Boolean,
      default: false,
    },
    iconSize: {
      type: Number,
      default: 18,
    },
    elevation: {
      type: Number,
      default: 0,
    },
    requireFormDrawer: {
      type: Boolean,
      default: false
    },
    left: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    id: Math.random(),
    menu: false,
    hasParentSpace: false,
    spaceTemplates: [],
    subspaceTemplateIds: []
  }),
  computed: {
    filteredSpaceTemplates() {
      return this.spaceTemplates.filter(template => !this.subspaceTemplateIds.includes(template.id));
    },
    displaySpaceCreationMenu() {
      return !this.$root.openedSpaceTemplateId && this.hasParentSpace;
    }
  },
  watch: {
    menu() {
      // Workaround to fix closing menu when clicking outside
      if (this.menu) {
        document.addEventListener('mousedown', this.closeMenu);
      } else {
        document.removeEventListener('mousedown', this.closeMenu);
      }
    },
  },
  created() {
    this.init();
  },
  methods: {
    async init() {
      const result = await this.$spaceService.getSpacesByFilter({
        offset: 0,
        limit: 1,
        filter: 'accessible',
        onlyParentSpaces: true,
      });
      this.hasParentSpace = result?.size > 0;
      if (!this.$root.spaceTemplates) {
        this.$root.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
      }
      this.spaceTemplates = this.$root.spaceTemplates;
      if (!this.$root.subspaceTemplateIds) {
        this.$root.subspaceTemplateIds = await this.$spaceTemplateService.getSubspaceTemplateIds();
      }
      this.subspaceTemplateIds = this.$root.subspaceTemplateIds;
    },
    addNewSpace() {
      const spaceTemplate = this.$root.openedSpaceTemplateId;
      if (!this.requireFormDrawer) {
        this.$root.$emit('addNewSpace', spaceTemplate, !spaceTemplate && this.filteredSpaceTemplates);
      } else {
        window.require(['SHARED/spaceForm'], drawer => drawer.open(spaceTemplate, !spaceTemplate && this.filteredSpaceTemplates));
      }
    },
    addNewSubSpace() {
      if (!this.requireFormDrawer) {
        this.$root.$emit('addNewSpace', this.$root.openedSpaceTemplateId, null, null, true);
      } else {
        window.require(['SHARED/spaceForm'], drawer => drawer.open(this.$root.openedSpaceTemplateId, null, null, true));
      }
    },
    closeMenu(event) {
      if (event !== this.id) {
        if (event?.target) {
          window.setTimeout(() => {
            this.menu = false;
          }, 200);
        } else {
          this.menu = false;
        }
      }
    },
  }
};
</script>

<template>
  <component
    v-model="menu"
    :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    :content-class="menuId"
    offset-y>
    <template #activator="{ on, attrs }">
      <v-btn
        :aria-label="$t('spaceTemplates.menu.open')"
        icon
        small
        class="mx-auto"
        v-bind="attrs"
        v-on="on">
        <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-hover v-if="menu" @input="hoverMenu = $event">
      <v-list
        class="pa-0"
        dense
        @mouseout="menu = false"
        @focusout="menu = false">
        <v-subheader v-if="$root.isMobile">
          <div class="d-flex full-width">
            <div class="d-flex flex-grow-1 flex-shrink-1 align-center subtitle-1 text-truncate">
              {{ $t('spaceTemplate.label.templateMenu', {0: name}) }}
            </div>
            <div class="flex-shrink-0">
              <v-btn
                :aria-label="$t('spaceTemplate.label.closeMenu')"
                icon
                @click="menu = false">
                <v-icon>fa-times</v-icon>
              </v-btn>
            </div>
          </div>
        </v-subheader>
        <v-list-item-group v-model="listItem">
          <v-list-item
            dense
            @click="$root.$emit('space-template-drawer-open', spaceTemplate)">
            <v-icon size="13">
              fa-edit
            </v-icon>
            <v-list-item-title class="ps-2">
              {{ $t('spaceTemplate.label.editProperties') }}
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            dense
            @click="$root.$emit('space-template-drawer-open', spaceTemplate, true)">
            <v-icon size="13">
              fa-copy
            </v-icon>
            <v-list-item-title class="ps-2">
              {{ $t('spaceTemplate.label.duplicate') }}
            </v-list-item-title>
          </v-list-item>
          <v-tooltip :disabled="!spaceTemplate.system" bottom>
            <template #activator="{ on, attrs }">
              <div
                v-on="on"
                v-bind="attrs">
                <v-list-item
                  :disabled="spaceTemplate.system"
                  dense
                  @click="$root.$emit('space-templates-delete', spaceTemplate)">
                  <v-icon
                    :class="!spaceTemplate.system && 'error--text' || 'disabled--text'"
                    size="13">
                    fa-trash
                  </v-icon>
                  <v-list-item-title
                    :class="!spaceTemplate.system && 'error--text' || 'disabled--text'"
                    class="ps-2">
                    {{ $t('spaceTemplate.label.delete') }}
                  </v-list-item-title>
                </v-list-item>
              </div>
            </template>
            <span>{{ $t('spaceTemplate.label.system.noDelete') }}</span>
          </v-tooltip>
        </v-list-item-group>
      </v-list>
    </v-hover>
  </component>
</template>
<script>
export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    hoverMenu: false,
    listItem: null,
    menuId: `spaceTemplateMenu${parseInt(Math.random() * 10000)}`,
  }),
  computed: {
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
    name() {
      return this.$te(this.spaceTemplate?.name) ? this.$t(this.spaceTemplate?.name) : this.spaceTemplate?.name;
    },
  },
  watch: {
    listItem() {
      if (this.menu) {
        this.menu = false;
        this.listItem = null;
      }
    },
    menu() {
      if (this.menu) {
        this.$root.$emit('space-management-menu-opened', this.spaceTemplateId);
      } else {
        this.$root.$emit('space-management-menu-closed', this.spaceTemplateId);
      }
    },
    hoverMenu() {
      if (!this.hoverMenu) {
        window.setTimeout(() => {
          if (!this.hoverMenu) {
            this.menu = false;
          }
        }, 200);
      }
    },
  },
  created() {
    this.$root.$on('space-management-menu-opened', this.checkMenuStatus);
    document.addEventListener('click', this.closeMenuOnClick);
  },
  beforeDestroy() {
    this.$root.$off('space-management-menu-opened', this.checkMenuStatus);
    document.removeEventListener('click', this.closeMenuOnClick);
  },
  methods: {
    closeMenuOnClick(e) {
      if (e.target && !e.target.closest(`.${this.menuId}`)) {
        this.menu = false;
      }
    },
    checkMenuStatus(templateId) {
      if (this.menu && templateId !== this.spaceTemplate.id) {
        this.menu = false;
      }
    },
  },
};
</script>
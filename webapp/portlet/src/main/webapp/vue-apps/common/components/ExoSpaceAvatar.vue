<template>
  <div class="profile-popover space-avatar-wrapper">
    <a
      :id="id"
      :href="url"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar"
      @mouseover="showSpacePopover"
      @mouseleave="hideSpacePopover">
      <v-avatar
        :size="size"
        tile
        class="pull-left my-auto">
        <img
          :src="avatarUrl"
          :class="avatarClass"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
      <div
        v-if="displayName || $slots.subTitle"
        :class="!subtitleNewLine && 'd-flex'"
        class="pull-left text-truncate ms-2">
        <p
          v-if="displayName"
          :class="fullnameStyle"
          class="text-truncate subtitle-2 my-auto">
          {{ displayName }}
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title my-auto">
          <slot name="subTitle">
          </slot>
        </p>
      </div>
    </a>
    <v-menu
      v-if="popover && !isMobile"
      rounded="rounded"
      v-model="displaySpacePopover"
      :attach="`#${id}`"
      :close-on-content-click="false"
      :nudge-bottom="x"
      :nudge-right="y"
      :left="popoverLeftPosition"
      content-class="popover-menu white"
      max-width="270"
      min-width="270"
      offset-y
      offset-x>
      <v-card elevation="0" class="pa-2">
        <v-list-item class="px-2">
          <v-list-item-content class="py-0">
            <v-list-item-title>
              <exo-space-avatar
                :space="space"
                :size="40"
                avatar-class="border-color"
                class="activity-share-space d-inline-block my-auto"
                bold-title
                link-style
                subtitle-new-line>
                <template slot="subTitle">
                  <span v-if="spaceMembersCount" class="caption text-bold">
                    {{ spaceMembersCount }} {{ $t('UIActivity.label.Members') }}
                  </span>
                </template>
              </exo-space-avatar>
            </v-list-item-title>
            <p v-if="spaceDescription" class="text-truncate-3 text-caption text--primary font-weight-medium">
              {{ spaceDescription }}
            </p>
          </v-list-item-content>
        </v-list-item>
        <div class="d-flex justify-end">
          <extension-registry-components
            :params="params"
            class="d-flex"
            name="SpacePopover"
            type="space-popover-action"
            parent-element="div"
            element="div"
            element-class="mx-auto ma-lg-0" />
          <div
            v-for="extension in enabledWebConferencingComponents"
            :key="extension.key"
            :class="`${extension.appClass} ${extension.typeClass}`"
            :ref="extension.key">
          </div>
        </div>
      </v-card>
    </v-menu>
  </div>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    space: {
      type: Object,
      default: () => null,
    },
    boldTitle: {
      type: Boolean,
      default: () => false,
    },
    linkStyle: {
      type: Boolean,
      default: () => false,
    },
    subtitleNewLine: {
      type: Boolean,
      default: () => false,
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    popover: {
      type: Boolean,
      default: () => false,
    },
    popoverLeftPosition: {
      type: Boolean,
      default: () => false,
    },
  },
  data() {
    return {
      id: `spaceAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      displaySpacePopover: false,
      spacePopupExtensions: []
    };
  },
  computed: {
    spaceId() {
      return this.space && this.space.id;
    },
    displayName() {
      return this.space && this.space.displayName;
    },
    prettyName() {
      return this.space && this.space.prettyName;
    },
    groupId() {
      return this.space && this.space.groupId;
    },
    avatarUrl() {
      return this.space && this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.prettyName}/avatar`;
    },
    url() {
      if (!this.groupId) {
        return '#';
      }
      const uri = this.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
    spaceMembersCount() {
      return this.space && this.space.membersCount;
    },
    spaceDescription() {
      return this.space && this.space.description;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${!this.linkStyle && 'text-color' || ''}`;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    enabledWebConferencingComponents() {
      return this.spacePopupExtensions.filter(extension => extension.enabled);
    },
    params() {
      return {
        identity: this.space
      };
    },
  },
  created() {
    this.refreshExtensions();
  },
  methods: {
    showSpacePopover() {
      if ( this.popover && !this.isMobile) {
        const currentUser= $(`#${this.id}`);
        this.y = currentUser[0].offsetLeft;
        this.x = currentUser[0].offsetTop + currentUser[0].offsetHeight;
        this.spacePopupExtensions.map((extension) => {
          if ( !this.$refs[extension.key] ) {
            this.$nextTick().then(() => {
              this.initWebConferencingActionComponent(extension);
            });
          }
        });
        this.displaySpacePopover = true;
      }
    },
    hideSpacePopover() {
      if ( this.popover && !this.isMobile) {
        this.displaySpacePopover = false;
      }
    },
    refreshExtensions() {
      this.spacePopupExtensions = extensionRegistry.loadExtensions('space-popup', 'space-popup-action') || [];
    },
    initWebConferencingActionComponent(extension) {
      if (extension.enabled) {
        let container = this.$refs[extension.key];
        if (container && container.length > 0) {
          container = container[0];
          extension.init(container, this.space.prettyName);
        } else {
          // eslint-disable-next-line no-console
          console.error(
            `Error initialization of the ${extension.key} action component: empty container`
          );
        }
      }
    },
    extensionAction(event, extension) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (extension && extension.click) {
        extension.click(this.space.id);
      }
    }
  }
};
</script>

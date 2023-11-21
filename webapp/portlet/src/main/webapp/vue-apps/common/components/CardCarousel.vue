<template>
  <div class="carousel-top-parent overflow-hidden position-relative">
    <v-fab-transition>
      <v-btn
        v-show="displayLeftArrow"
        :aria-label="$t('cardCarousel.leftArrowButtonTitle')"
        color="while"
        width="23px"
        height="23px"
        class="absolute-vertical-center"
        fab
        dark
        absolute
        left
        x-small
        @click="moveLeft">
        <v-icon size="25">fa-arrow-circle-left</v-icon>
      </v-btn>
    </v-fab-transition>
    <v-card
      :class="!dense && 'px-0 pb-4 pt-2'"
      class="carousel-middle-parent scrollbar-width-none d-flex overflow-x-scroll"
      flat
      @scroll="computeProperties"
      @resize="computeProperties">
      <div :class="parentClass" class="carousel-last-parent d-flex ma-auto">
        <slot></slot>
      </div>
    </v-card>
    <v-fab-transition>
      <v-btn
        v-show="displayRightArrow"
        :aria-label="$t('cardCarousel.rightArrowButtonTitle')"
        color="while"
        width="23px"
        height="23px"
        class="absolute-vertical-center"
        fab
        dark
        absolute
        right
        x-small
        @click="moveRight">
        <v-icon size="25">fa-arrow-circle-right</v-icon>
      </v-btn>
    </v-fab-transition>
  </div>
</template>

<script>
export default {
  props: {
    parentClass: {
      type: Object,
      default: null,
    },
    dense: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    scrollElement: null,
    displayLeftArrow: false,
    displayRightArrow: false,
    childScrollIndex: 0,
    visibleChildrenPerPage: 1,
    computing: false,
  }),
  mounted() {
    this.scrollElement = this.$el && this.$el.children && this.$el.children.length > 1 && this.$el.children[1];

    window.setTimeout(() => {
      this.computeProperties();
    }, 500);
    window.onresize = this.computeProperties;
  },
  updated() {
    this.computeProperties();
  },
  methods: {
    stopPropagation(event) {
      if (event) {
        event.stopPropagation();
      }
    },
    moveRight() {
      const children = this.scrollElement.firstChild.children;
      const newIndex = this.childScrollIndex + this.visibleChildrenPerPage;
      this.childScrollIndex = newIndex >= children.length ? (children.length - 1) : newIndex;
      this.scrollElement.scrollTo({
        left: children[this.childScrollIndex].offsetLeft - 8,
        behavior: 'smooth'
      });
    },
    moveLeft() {
      const children = this.scrollElement.firstChild.children;
      const newIndex = this.childScrollIndex - this.visibleChildrenPerPage;
      this.childScrollIndex = newIndex < 0 ? 0 : newIndex;
      this.scrollElement.scrollTo({
        left: children[this.childScrollIndex].offsetLeft - 8,
        behavior: 'smooth'
      });
    },
    computeProperties() {
      if (!this.computing) {
        this.computing = true;
        window.setTimeout(() => {
          const parentWidth = this.scrollElement.offsetWidth;
          const contentWidth = this.scrollElement.firstChild.offsetWidth;
          const children = this.scrollElement.firstChild.children;
          const childrenCount = children.length;
          this.visibleChildrenPerPage = parseInt(parentWidth * childrenCount / contentWidth);
          this.displayLeftArrow = this.scrollElement && childrenCount && this.scrollElement.scrollLeft > children[0].offsetLeft;
          this.displayRightArrow = this.scrollElement && (this.scrollElement.scrollWidth - this.scrollElement.offsetWidth - this.scrollElement.scrollLeft) > 5;
          this.computing = false;
        }, 200);
      }
    },
  },
};
</script>
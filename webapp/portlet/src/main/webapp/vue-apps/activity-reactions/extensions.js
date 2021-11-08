export function registerActivityReactionTabs() {
  /*const likeExtension = {
    id: `like-${this.activityId}`,
    icon: 'uiIconThumbUp',
    reactionType: 'likes',
    order: 2,
    activityType: 'ACTIVITY',
    activityId: this.activityId,
    reactionNumber: this.activity.likes ? this.activity.likes.length : 0,
    reactionListItems: this.activity.likes || [],
    class: 'likers'
  };*/
  extensionRegistry.registerComponent('ActivityReactions', 'activity-reaction-action', {
    id: 'like',
    icon: 'uiIconThumbUp',
    reactionType: 'likes',
    order: 2,
    activityType: 'ACTIVITY',
    vueComponent: Vue.options.components['activity-likes-list'],
    rank: 10,
  });
}
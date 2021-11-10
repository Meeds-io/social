export function registerActivityReactionTabs() {
  extensionRegistry.registerComponent('ActivityReactions', 'activity-reaction-action', {
    id: 'like',
    icon: 'uiIconThumbUp',
    reactionType: 'likes',
    numberOfReactions: 0,
    order: 2,
    activityType: 'ACTIVITY',
    vueComponent: Vue.options.components['activity-likes-list'],
    rank: 10,
  });
}
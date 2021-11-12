export function registerActivityReactionTabs() {
  extensionRegistry.registerComponent('ActivityReactions', 'activity-reaction-action', {
    id: 'like',
    reactionLabel: 'likesLabel',
    numberOfReactions: 0,
    order: 2,
    vueComponent: Vue.options.components['activity-likes-list'],
    rank: 10,
  });
}
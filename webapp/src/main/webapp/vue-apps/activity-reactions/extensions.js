export function registerActivityReactionTabs () {
  extensionRegistry.registerComponent('ActivityReactions', 'activity-reaction-action', {
    id: 'like',
    reactionLabel: 'UIActivity.label.likesLabel',
    numberOfReactions: 0,
    vueComponent: Vue.component['activity-likes-list'],
    rank: 1,
  });
}
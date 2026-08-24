export function registerActivityReactionTabs() {
  // the tab id stays 'like' to keep the drawer tab contract (openers and
  // third-party tabs such as Kudos are untouched); the tab now lists every
  // reaction with per-option filter chips
  extensionRegistry.registerComponent('ActivityReactions', 'activity-reaction-action', {
    id: 'like',
    reactionLabel: 'UIActivity.label.reactions',
    numberOfReactions: 0,
    vueComponent: Vue.options.components['activity-reactions-list'],
    rank: 1,
  });
}
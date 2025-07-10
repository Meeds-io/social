extensionRegistry.registerExtension('favorite', 'favorite-type', {
  rank: 20,
  id: 'activity',
  icon: 'fa-stream',
});
 
extensionRegistry.registerComponent('favorite-activity', 'favorite-drawer-item', {
  id: 'activity',
  vueComponent: Vue.options.components['activity-favorite-item'],
}); 

extensionRegistry.registerExtension('favorite', 'favorite-type', {
  rank: 10,
  id: 'space',
  icon: 'fa-people-arrows',
});

extensionRegistry.registerComponent('favorite-space', 'favorite-drawer-item', {
  id: 'space',
  vueComponent: Vue.options.components['space-favorite-item'],
}); 


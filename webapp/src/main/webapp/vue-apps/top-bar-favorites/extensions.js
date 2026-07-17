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

extensionRegistry.registerExtension('favorite', 'favorite-type', {
  rank: 30,
  id: 'page',
  icon: 'fa-file-alt',
});

extensionRegistry.registerComponent('favorite-page', 'favorite-drawer-item', {
  id: 'page',
  vueComponent: Vue.options.components['page-favorite-item'],
});


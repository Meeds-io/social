export function registerExtension(title) {
  const chartNavigation = {
    id: 'user-chart',
    title: title,
    icon: 'fas fa-sitemap',
    class: 'fas fa-sitemap',
    order: 10,
    enabled: (user) => eXo.env.portal.isExternal === false && user.enabled && user?.external !==  'true',
    click: (profile) => {
      const url = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/organizationalchart?centerUserId=${profile.id}`;
      window.open(url, '_self');
    },
  };

  if (extensionRegistry) {
    extensionRegistry.registerExtension('user-extension', 'navigation', chartNavigation);
  }
  document.dispatchEvent(new CustomEvent('user-extension-updated', { detail: chartNavigation}));
}

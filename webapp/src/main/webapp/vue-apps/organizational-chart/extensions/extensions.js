export function registerExtension(title) {
  const chartNavigation = {
    id: 'user-chart',
    title: title,
    icon: 'fas fa-sitemap',
    class: 'fas fa-sitemap',
    order: 10,
    enabled: (user) => eXo.env.portal.isExternal === false && user.enabled && user?.external !==  'true',
    click: (profile) => {
      const isCurrentUser = profile.id === eXo.env.portal.userIdentityId;
      const chartPage = isCurrentUser && 'dashboard/myteam' || 'organizationalchart';
      const siteName = isCurrentUser && eXo.env.portal.myCraftSiteName || eXo.env.portal.metaPortalName;
      const url = `${eXo.env.portal.context}/${siteName}/${chartPage}?centerUserId=${profile.id}`;
      window.open(url, '_self');
    },
  };

  if (extensionRegistry) {
    extensionRegistry.registerExtension('user-extension', 'navigation', chartNavigation);
  }
  document.dispatchEvent(new CustomEvent('user-extension-updated', { detail: chartNavigation}));
}

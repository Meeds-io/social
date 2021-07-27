// Here we can add additional rules using workbox for service worker
workbox.routing.registerRoute(
  new RegExp('.*/rest/.*/social/.*/avatar.*'),
  new workbox.strategies.CacheFirst({
    cacheName: imageCacheName,
  }),
);

workbox.routing.registerRoute(
  new RegExp('.*/rest/.*/social/.*/banner.*'),
  new workbox.strategies.CacheFirst({
    cacheName: imageCacheName,
  }),
);

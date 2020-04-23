/*global self*/
const CACHE_NAME = 'ausk-v1';
// let assetsToCache = [
//   '/',
// ];

// Install a service worker
// self.addEventListener('install', event => {
//   // Perform install steps
//   event.waitUntil(
//     window.caches.open(CACHE_NAME)
//       .then(cache => {
//         console.log('Opened cache');
//         cache.addAll(assetsToCache);
//       })
//       .then(() => self.skipWaiting())
//   );
// });

// Cache and return requests
self.addEventListener('fetch', event => {
  event.respondWith(
    // caching urls while fetching
    fetch(event.request)
      .then(res => {
        const resClone = res.clone();
        window.caches.open(CACHE_NAME).then(cache => {
          cache.put(event.request, resClone);
        });
        return res;
      })
      .catch(() => window.caches.match(event.request).then(res => res))
  );
});

// Update a service worker
self.addEventListener('activate', event => {
  let cacheWhitelist = [CACHE_NAME];
  event.waitUntil(
    window.caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheWhitelist.indexOf(cacheName) === -1) {
            return window.caches.delete(cacheName);
          }
        })
      );
    })
  );
});

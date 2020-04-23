/*global navigator*/
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('basicServiceWorker.js')
      .then(
        registration => {
          console.log('Service Worker registration successful', registration.scope);
        },
        err => {
          console.log('Service Worker registration failed', err);
        }
      )
      .catch(err => {
        console.log(`Service Worker error: ${err}`);
      });
  });
} else {
  console.log('Service Worker is not supported by browser.');
}

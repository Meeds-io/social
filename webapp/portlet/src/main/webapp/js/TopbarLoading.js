(function($) {
  let operationsInProgress = 0;
  return {
    init: () => {
      document.addEventListener('displayTopBarLoading', () => {
        operationsInProgress++;
        $('.TopbarLoadingContainer').removeClass('hidden');
      });

      document.addEventListener('hideTopBarLoading', () => {
        if (operationsInProgress > 0) {
          operationsInProgress--;
        }
        if (operationsInProgress === 0) {
          $('.TopbarLoadingContainer').addClass('hidden');
        }
      });
    },
  };
})($);
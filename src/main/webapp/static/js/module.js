angular.module('app', ['ngAnimate', 'ngRoute']).config(function ($httpProvider, userSession) {
    $httpProvider.defaults.headers.common['X-CSRF-Token'] =
        userSession.csrfToken;
});
angular.module('app', ['ngAnimate', 'ngRoute', 'mdl']).config(function ($httpProvider, userSession) {
    $httpProvider.defaults.headers.common['X-CSRF-Token'] =
        userSession.csrfToken;
});
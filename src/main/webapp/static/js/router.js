angular.module('app').config(function($locationProvider, $routeProvider) {
    $locationProvider.html5Mode(true);
    $locationProvider.hashPrefix('!');

    $routeProvider.when('/contacts', {
        templateUrl: '/static/templates/contacts.html',
        controller: 'ContactsController'
    }).when('/sync', {
        templateUrl: '/static/templates/sync.html',
        controller: 'SyncController'
    }).when('/settings', {
        templateUrl: '/static/templates/settings.html',
        controller: 'SettingsController'
    }).otherwise({
        redirectTo: '/contacts'
    })
});
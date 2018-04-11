angular.module('app').config(function($locationProvider, $routeProvider) {
    $locationProvider.html5Mode(true);
    $locationProvider.hashPrefix('!');

    $routeProvider.when('/app/contacts', {
        templateUrl: '/contacts.html',
        controller: 'DrawerController'
    }).when('/app/sync', {
        templateUrl: '/sync.html',
        controller: 'SyncController'
    }).when('/app/settings', {
        templateUrl: '/settings.html',
        controller: 'SettingsController'
    }).otherwise({
        redirectTo: '/app/contacts'
    })
});
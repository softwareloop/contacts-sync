angular.module("app").controller('ContactsController', function($scope, contactsService) {
    $scope.contacts = contactsService.getContacts();
});
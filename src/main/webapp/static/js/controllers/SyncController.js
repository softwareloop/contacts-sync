angular.module("app").controller('SyncController', function($scope, $http, contactsService) {
    $scope.syncNow = function() {
        $http.post('/api/contacts/sync').
        then(function(response) {
            var data = response.data;
            contactsService.refresh();
            alert(data.message);
        });
    };

});
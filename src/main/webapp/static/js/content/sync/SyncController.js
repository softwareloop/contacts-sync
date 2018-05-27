angular.module("app").controller('SyncController', function($scope, $http) {
    $scope.syncNow = function() {
        $http.post('/api/contacts/sync').
        then(function(response) {
            var data = response.data;
            alert(data.message);
        });
    }
});
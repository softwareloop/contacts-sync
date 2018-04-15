angular.module("app").controller('SyncController', function($scope, $http) {
    $scope.syncNow = function() {
        $http.post('/api/sync/sync').
        then(function(response) {
            var data = response.data;
            alert(data.message);
        });
    }
});
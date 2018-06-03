angular.module("app").controller('SettingsController', function($scope, $http, $window, userSession) {
    $scope.userSession = userSession;

    $scope.disconnectAndLogout = function() {
        $http.post('/api/settings/disconnect-and-logout')
            .then(function(response) {
                $window.location.href = '/';
            });
    }
});
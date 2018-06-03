angular.module("app").controller('DrawerController', function($scope, $location, userSession) {
    this.userSession = userSession;

    this.navigateToContacts = function() {
        closeDrawer();
        $location.path('/contacts');
    };

    this.navigateToSync = function() {
        closeDrawer();
        $location.path('/sync');
    };

    this.navigateToSettings = function() {
        closeDrawer();
        $location.path('/settings');
    };

    function closeDrawer() {
        console.log("Close drawer");
        var layout = document.querySelector('.mdl-layout');
        layout.MaterialLayout.toggleDrawer();
    }

});
angular.module('app').service('contactsService', function($http) {
    var contacts = [];

    this.getContacts = function() {
        return contacts;
    };

    this.refresh = function() {
        $http.get('/api/contacts')
            .then(function(response) {
                contacts.length = 0;
                var data = response.data;
                for (var i = 0; i < data.length; i++) {
                    contacts.push(data[i]);
                }
            });
    };

    this.refresh();
});
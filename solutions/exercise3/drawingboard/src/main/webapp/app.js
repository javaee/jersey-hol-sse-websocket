'use strict';

angular.module('myApp', ['myApp.services']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/main', {templateUrl: 'main.html', controller: MainController});
    $routeProvider.when('/drawings/:drawingId', {templateUrl: 'drawing.html', controller: DrawingController});
    $routeProvider.otherwise({redirectTo: '/main'});
  }]);

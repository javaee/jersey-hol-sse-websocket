'use strict';

/* Controllers */

function MainController($scope, DrawingService, $http) {
    $scope.drawings = DrawingService.query();
    $scope.delete = function ($drawingId) {
        DrawingService.delete({drawingId:$drawingId});
    };
    
    $scope.addDrawing = function () {
        var newNote = new DrawingService({name: $scope.drawingName});
        $scope.drawingName = '';
        newDrawing.$save();
    };
    
    var source=new EventSource("/drawingboard-api/drawings/events");
    
    var eventHandler = function (event) {
        $scope.notes = DrawingService.query();
    };
    
    source.addEventListener("create", eventHandler, false);
    source.addEventListener("update", eventHandler, false);
    source.addEventListener("delete", eventHandler, false);
}

function DrawingController($scope, $routeParams, DrawingService) {
    $scope.drawing = DrawingService.get({drawingId:$routeParams.drawingId});
}

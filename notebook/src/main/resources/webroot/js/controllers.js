'use strict';

/* Controllers */

function MainController($scope, NoteService, $http) {
    $scope.notes = NoteService.query();
    $scope.delete = function ($noteId) {
        NoteService.delete({noteId:$noteId});
    };
    
    $scope.addNote = function () {
        var newNote = new NoteService({name: $scope.noteName});
        $scope.noteName = '';
        newNote.$save();
    };
    
    var source=new EventSource("/notebook-api/notes/events");
    
    var eventHandler = function (event) {
        $scope.notes = NoteService.query();
    };
    
    source.addEventListener("create", eventHandler, false);
    source.addEventListener("update", eventHandler, false);
    source.addEventListener("delete", eventHandler, false);
}

function NoteController($scope, $routeParams, NoteService) {
    $scope.note = NoteService.get({noteId:$routeParams.noteId});
}

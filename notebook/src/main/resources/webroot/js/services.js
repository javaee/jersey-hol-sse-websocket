'use strict';

/* Services */

var myModule = angular.module('myApp.services', ['ngResource']);

myModule.factory('NoteService', function ($resource) {
    return $resource('/notebook-api/notes/:noteId', {noteId:'@noteId'}, {});
});

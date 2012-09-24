'use strict';

/* Controllers */

// Controller for the main page (list of drawings)
function MainController($scope, DrawingService, $http) {
    // obtain drawings from the RESTful service
    $scope.drawings = DrawingService.query();
    
    // deletes a single drawing by ID
    $scope.remove = function ($drawingId) {
        DrawingService.remove({drawingId:$drawingId});
    };
    
    // adds a new drawing
    $scope.addDrawing = function () {
        var newDrawing = new DrawingService({name: $scope.drawingName});
        $scope.drawingName = '';
        newDrawing.$save();
    };
    
    // listens to server-sent events for the list of drawings
    $scope.eventSource = new EventSource("/drawingboard-api/drawings/events");
    
    var eventHandler = function (event) {
        $scope.drawings = DrawingService.query();
    };
    
    $scope.eventSource.addEventListener("create", eventHandler, false);
    $scope.eventSource.addEventListener("update", eventHandler, false);
    $scope.eventSource.addEventListener("delete", eventHandler, false);
    
    // clean up
    $scope.$on("$destroy", function (event) {
        $scope.eventSource.close();
    });
}

// Controller for the drawing editor page
function DrawingController($scope, $routeParams, DrawingService) {
    $scope.drawing = DrawingService.get({drawingId:$routeParams.drawingId});
    $scope.drawingCanvas = document.getElementById('drawing');
    $scope.shapeType = "BIG_CIRCLE";
    $scope.shapeColor = "RED";

    // open a web socket connection for a given drawing
    $scope.websocket = new WebSocket("ws://" + document.location.host
        + "/drawingboard-api/drawings/websockets/" + $routeParams.drawingId);
    $scope.websocket.onmessage = function (evt) {
        if (evt.data == "clear") {
            $scope.drawing.shapes = [];
            var context = $scope.drawingCanvas.getContext('2d');
            context.fillStyle = "white";
            context.fillRect(0,0,500,500);
            context.fill();
        } else {
            $scope.drawShape(eval("(" + evt.data + ")"));
        }
    };
    
    // clean up
    $scope.$on("$destroy", function (event) {
        $scope.websocket.close();
    });

    // draws a given shape
    $scope.drawShape = function (shape) {
        var context = $scope.drawingCanvas.getContext('2d');
        var radius = 8;
        //Canvas commands go here
        //context.strokeStyle = "#000000";
        context.fillStyle = shape.color;
        if (shape.type == 'SMALL_CIRCLE') {
            context.beginPath();
            context.arc(shape.x,shape.y,radius,0,Math.PI*2,true);
            context.closePath();
            context.fill();
        } else if (shape.type == 'BIG_CIRCLE') {
            context.beginPath();
            context.arc(shape.x,shape.y,2*radius,0,Math.PI*2,true);
            context.closePath();
            context.fill();
        } else if (shape.type == 'BIG_SQUARE') {
            //context.fillRect(0,1,10,10);
            context.fillRect( (shape.x-(2*radius)), (shape.y-(2*radius)), (4*radius), (4*radius));
            //context.fill();
        } else if (shape.type == 'SMALL_SQUARE') {
            context.fillRect( (shape.x-(radius)), (shape.y-(radius)), (2*radius), (2*radius));
        }
    }

    // mouseMove event handler
    $scope.mouseMove = function (event) {
        if (event.shiftKey) {
            $scope.mouseDown(event);
        }
    }           

    // mouseDown event handler
    $scope.mouseDown = function (event) {
        var rect = $scope.drawingCanvas.getBoundingClientRect();
        var root = document.documentElement;

        var mouseX = event.clientX - rect.left - root.scrollTop;
        var mouseY = event.clientY - rect.top - root.scrollLeft;

        $scope.websocket.send(
            '{"x" : ' + mouseX +
            ', "y" : ' + mouseY +
            ', "color" : "' + $scope.shapeColor + 
            '", "type" : "' + $scope.shapeType + '"}');
    }
    
    // clears the canvas (deletes all shapes)
    $scope.clearCanvas = function () {
        $scope.websocket.send("clear");
    }
}

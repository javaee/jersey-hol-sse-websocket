'use strict';

/* Controllers */

function MainController($scope, DrawingService, $http) {
    $scope.drawings = DrawingService.query();
    $scope.delete = function ($drawingId) {
        DrawingService.delete({drawingId:$drawingId});
    };
    
    $scope.addDrawing = function () {
        var newDrawing = new DrawingService({name: $scope.drawingName});
        $scope.drawingName = '';
        newDrawing.$save();
    };
    
    var source=new EventSource("/drawingboard-api/drawings/events");
    
    var eventHandler = function (event) {
        $scope.drawings = DrawingService.query();
    };
    
    source.addEventListener("create", eventHandler, false);
    source.addEventListener("update", eventHandler, false);
    source.addEventListener("delete", eventHandler, false);
}

function DrawingController($scope, $routeParams, DrawingService) {
    $scope.drawing = DrawingService.get({drawingId:$routeParams.drawingId});
    $scope.drawingCanvas = document.getElementById('drawing');
    $scope.shapeType = "BIG_CIRCLE";
    $scope.shapeColor = "RED";

    $scope.websocket = new WebSocket("ws://localhost:8080/drawingboard-api/drawings/websockets/" + $routeParams.drawingId);
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

    $scope.mouseMove = function (event) {
        if (event.shiftKey) {
            $scope.mouseDown(event);
        }
    }           

    $scope.mouseDown = function (event) {
        $scope.websocket.send(
            '{"x" : ' + event.layerX +
            ', "y" : ' + event.layerY +
            ', "color" : "' + $scope.shapeColor + 
            '", "type" : "' + $scope.shapeType + '"}');
    }
    
    $scope.clearCanvas = function () {
        $scope.websocket.send("clear");
    }
}

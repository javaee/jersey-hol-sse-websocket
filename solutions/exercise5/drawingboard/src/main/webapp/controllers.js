'use strict';

/* Controllers */

// Controller for the main page (list of drawings)
function MainController($scope, DrawingService, $http) {
    // a var to distinguish a JavaFX/WebView based client 
    var javaFXClient = navigator.appVersion.indexOf("JavaFX") > 0;
    // obtain drawings from the RESTful service
    $scope.drawings = DrawingService.query();

    // deletes a single drawing by ID
    $scope.remove = function($drawingId) {
        DrawingService.remove({drawingId: $drawingId});
    };

    // adds a new drawing
    $scope.addDrawing = function() {
        var newDrawing = new DrawingService({name: $scope.drawingName});
        $scope.drawingName = '';
        newDrawing.$save();
    };

    // listens to server-sent events for the list of drawings
    if (!javaFXClient)
        $scope.eventSource = new EventSource("/drawingboard/api/drawings/events");

    var eventHandler = function(event) {
        $scope.drawings = DrawingService.query();
    };

    if (!javaFXClient) {
        $scope.eventSource.addEventListener("create", eventHandler, false);
        $scope.eventSource.addEventListener("update", eventHandler, false);
        $scope.eventSource.addEventListener("delete", eventHandler, false);
    }

    // clean up
    $scope.$on("$destroy", function(event) {
        if (!javaFXClient)
            $scope.eventSource.close();
    });
}

// Controller for the drawing editor page
function DrawingController($scope, $routeParams, DrawingService) {
    // a var to distinguish a JavaFX/WebView based client 
    var javaFXClient = navigator.appVersion.indexOf("JavaFX") > 0;
    $scope.drawing = DrawingService.get({drawingId: $routeParams.drawingId});
    $scope.drawingCanvas = document.getElementById('drawing');
    $scope.shapeType = "BIG_CIRCLE";
    if (javaFXClient) $scope.shapeColor = "RED";
        else if (navigator.appVersion.indexOf("Chrome") > 0) $scope.shapeColor = "BLUE";
            else $scope.shapeColor = "GREEN";
    // open a web socket connection for a given drawing
    if (!javaFXClient) {
        $scope.websocket = new WebSocket("ws://" + document.location.host + "/drawingboard/websockets/" + $routeParams.drawingId);
        $scope.websocket.onmessage = function(evt) {
            console.log(evt.data);
            $scope.drawShape(eval("(" + evt.data + ")"));
        };
    }
    else
        window.webSocketOpen.open("ws://" + document.location.host
                + "/drawingboard/websockets/", $routeParams.drawingId);

    // clean up
    $scope.$on("$destroy", function(event) {
        if (!javaFXClient){
            // sometimes when this function is called, the websocket is already closed
            if ($scope.websocket.readyState > 0)
                $scope.websocket.close();
        }
        else {
            // sometimes when this function is called, $scope.drawing.id is undefinded
            if ($scope.drawing.id > 0)
                window.webSocketClose.close($scope.drawing.id);
        }
    });

    // draws a given shape
    $scope.drawShape = function(shape) {
        var context = $scope.drawingCanvas.getContext('2d');
        var radius = 8;
        //Canvas commands go here
        //context.strokeStyle = "#000000";
        context.fillStyle = shape.color;
        if (shape.type == 'SMALL_CIRCLE') {
            context.beginPath();
            context.arc(shape.x, shape.y, radius, 0, Math.PI * 2, true);
            context.closePath();
            context.fill();
        } else if (shape.type == 'BIG_CIRCLE') {
            context.beginPath();
            context.arc(shape.x, shape.y, 2 * radius, 0, Math.PI * 2, true);
            context.closePath();
            context.fill();
        } else if (shape.type == 'BIG_SQUARE') {
            //context.fillRect(0,1,10,10);
            context.fillRect((shape.x - (2 * radius)), (shape.y - (2 * radius)), (4 * radius), (4 * radius));
            //context.fill();
        } else if (shape.type == 'SMALL_SQUARE') {
            context.fillRect((shape.x - (radius)), (shape.y - (radius)), (2 * radius), (2 * radius));
        }
    }

    // mouseMove event handler
    $scope.mouseMove = function(event) {
        if (event.shiftKey) {
            $scope.mouseDown(event);
        }
    }

    // mouseDown event handler
    $scope.mouseDown = function(e) {
        var totalOffsetX = 0;
        var totalOffsetY = 0;
        var currentElement = $scope.drawingCanvas;

        do {
            totalOffsetX += currentElement.offsetLeft;
            totalOffsetY += currentElement.offsetTop;
        } while (currentElement = currentElement.offsetParent);


        var posx = e.pageX - totalOffsetX;
        var posy = e.pageY - totalOffsetY;

        var msg = '{"x" : ' + posx +
                ', "y" : ' + posy +
                ', "color" : "' + $scope.shapeColor +
                '", "type" : "' + $scope.shapeType + '"}';

        if (!javaFXClient)
            $scope.websocket.send(msg);
        else
            window.webSocketSend.send(msg);
    }
}

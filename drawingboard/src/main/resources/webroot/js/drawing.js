'use strict';

function drawShape(shape) {
    var drawingCanvas = document.getElementById('drawing');
    var context = drawingCanvas.getContext('2d');
    var radius = 8;
    //Canvas commands go here
    //context.strokeStyle = "#000000";
    context.fillStyle = shape.color;
    if (shape.type == 'SMALL_CIRCLE') {
        context.beginPath();
        context.arc(x,y,radius,0,Math.PI*2,true);
        context.closePath();
        context.fill();
    } else if (shape.type == 'BIG_CIRCLE') {
        context.beginPath();
        context.arc(x,y,2*radius,0,Math.PI*2,true);
        context.closePath();
        context.fill();
    } else if (shape.type == 'BIG_SQUARE') {
        //context.fillRect(0,1,10,10);
        context.fillRect( (x-(2*radius)), (y-(2*radius)), (4*radius), (4*radius));
        //context.fill();
    } else if (shape.type == 'SMALL_SQUARE') {
        context.fillRect( (x-(radius)), (y-(radius)), (2*radius), (2*radius));
    }
}

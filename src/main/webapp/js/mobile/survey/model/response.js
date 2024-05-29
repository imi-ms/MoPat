function Response() {
	
    this.customtext;
    this.value;
    this.date;
    this.answerId;
    this.enabled = true;
    this.pointsOnImage;
    
    this.currentPointsPosition = -1;
    
    this.init = function () {
        // Create a array for the points on image
        var tempPointsOnImage = new Array();

        // If there are given points on image from an incomplete encounter: loop through
        for (var i = 0; i < this.pointsOnImage.length; i++) {
            // Create a PointOnImage-Object from the plain js object
            var pointOnImage = $.extend(new PointOnImage(), this.pointsOnImage[i]);
            // Add the pointOnImage to the array
            tempPointsOnImage.push(pointOnImage);
        }
        // Overwrite the old array within the plain object with the new PointOnImage-Object array
        this.pointsOnImage = tempPointsOnImage;
        currentPointsPosition = this.pointsOnImage.length - 1;
    };
}
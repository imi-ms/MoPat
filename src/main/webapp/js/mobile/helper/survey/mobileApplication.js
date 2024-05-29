function MobileApplication() {
}

// Variable for checking if the internet is available
var internetAvailable = true;

// Variable for image retrieval
var images = {};

/**
 * 
 * @returns {Boolean} true if internet connection is available, false otherwise.
 */
MobileApplication.isInternetAvailable = function () {
    jQuery.ajaxSetup({async: false});
    random = Math.round(Math.random() * 10000);
    var testUrl = "/images/logo.svg";
    if (contextPath !== "/") {
        testUrl = contextPath + "/images/logo.svg";
    }           
    // Check for internet connection
    $.get(testUrl, {subins: random}, function (data) {
        // If inernet connection is available open close dialog            
        internetAvailable = true;
    }).fail(function () {
        // Open no internet connection alert otherwise
        internetAvailable = false;
    });
    return internetAvailable;
};

MobileApplication.getBase64Image = function(name, url) {
    var img = new Image();
    img.src = url;

    // Wait until the image is fully loaded and create the base64 representation
    img.onload = function () {
        // Create an empty canvas element
        var canvas = document.createElement("canvas");
        canvas.width = this.width;
        canvas.height = this.height;

        // Copy the image contents to the canvas
        var ctx = canvas.getContext("2d");
        ctx.drawImage(this, 0, 0);

        // Get the data-URL formatted image
        // Firefox supports PNG and JPEG. You could check img.src to guess the
        // original format, but be aware the using "image/jpg" will re-encode the image.
        var dataURL = canvas.toDataURL("image/png");
        // Save Base64 reprensentation in images[]
        images[name] = dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
    };
};

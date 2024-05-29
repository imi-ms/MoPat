function Configuration () {
    this.logo;
    
    this.init = function() {
        if (this.logo != null) {
            var logo = $('.footer .ui-block-a img');
            logo.attr("src", this.logo);
            logo.attr("alt", "");
            
        }
    }
}
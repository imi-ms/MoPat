function DragNDropList() {
}
/*
 * Shows or hides the tables which the plus/minus symbol belongs to
 */
DragNDropList.toggleCollapseTable = function (element) {
    if ($(element).hasClass("bi-plus-lg")) {
        $(element).closest('.toggleCollapseTable').find('.collapsable').slideDown();
        $(element).closest('.toggleCollapseTable').find('.bi-plus-lg').hide();
        $(element).closest('.toggleCollapseTable').find('.bi-dash-lg').show();
    } else if ($(element).hasClass("bi-dash-lg")) {
        $(element).closest('.toggleCollapseTable').find('.collapsable').slideUp();
        $(element).closest('.toggleCollapseTable').find('.bi-dash-lg').hide();
        $(element).closest('.toggleCollapseTable').find('.bi-plus-lg').show();
    }
}

/*
 * Prepends the selected element to the target list
 */
DragNDropList.moveElementToTargetList = function (element, target) {
    var selectedElement = element.parent().parent();
    selectedElement.remove();
    target.append(selectedElement);
    this.checkIfTablesAreEmptyAndShowPlaceholder(); 
    this.switchPosition(selectedElement);
    this.sortEntries(); 
    this.handleElementChanges(selectedElement);
}

/*
 * Handles the action that occurs when the table of a row has changed
 */
DragNDropList.handleElementChanges = function (selectedElement) {
    //hide placeholder row if element is dropped
    $('.placeholderRow').hide();

    $(".assignedTable").find(":input").removeAttr("disabled");
    $(".assignedTable").find(".scores").css('display', 'block');
    $(".assignedTable").find(".templateTypes").css('display', 'block');
    $(".assignedTable").find(".enable").css('display', 'block');
    $(".availableTable").find(":input").attr("disabled", 'true');
    $(".availableTable").find(".scores").css('display', 'none');
    $(".availableTable").find(".templateTypes").css('display', 'none');
    $(".availableTable").find(".enable").css('display', 'none');

    if (startTable.attr('id') !== targetTable.attr('id')) {
        //choose the filter that has to be reset
        targetTable.parent().find('#' + targetTable.attr('id').replace('Table', 'Filter')).val('');

        //reset filter and hide empty row
        targetTable.children('.draggable').each(function () {
            $(this).css("display", "");
        });
        targetTable.find('#filterEmptyRow').hide();

        //reset filter of startTable
        var hiddenCounter = 0;
        startTable.children('.draggable').each(function () {
            if ($(this).css('display') === "none") {
                hiddenCounter++;
            }
        });

        if (hiddenCounter !== 0 && hiddenCounter === startTable.children('.draggable').length) {
            startTable.find('#filterEmptyRow').css('display', '');
        }
    }
}

/**
 * Function checks all sortable elements in the DOM and checks if the empty row 
 * with the id "#emptyRow" should be visible 
 */
DragNDropList.checkIfTablesAreEmptyAndShowPlaceholder = function() {
    $(".sortable").each(function(i) {
        if ($(this).children(".draggable").length === 0) {
            $(this).children('#emptyRow')[0].setAttribute('style', "display: block");
        } else {
            $(this).children('#emptyRow')[0].setAttribute('style', "display: none");
        }
    });
}

/**
 * Function to handle position changes in a draggable list.
 * Changes the indices for all entries for every position change and
 * checks if the list was changed and triggers onStop function to allow 
 * live updates
 * @param {*} e element that is dragged
 */
DragNDropList.onChange = function(e) {
    // Perform check with dom elements as jquery objects differ even if they point to the same element
    if ($(e.item).parent()[0] != $(targetTable)[0]) {
        targetTable = $(e.item).parent(); 
        this.checkIfTablesAreEmptyAndShowPlaceholder();
        this.switchPosition($(e.item));
        this.handleElementChanges($(e.item)); 
    }
    this.sortEntries(); 
}

/**
 * Processes all position elements of a sortable 
 * and adjusts them depending on their order. 
 * Should be called after changing the order. 
 */
DragNDropList.sortEntries = function() {
    //renumbering the position
    $(".sortable").each(function(i) {
        $(this).children().find("div.position").each(function(i) {
            $(this).children('span').html((i + 1) + ".");
            $(this).children('input').val((i + 1));
        });
    });
}

/**
 * Handles the filter function for the DragNDropList to search for elements 
 * containing the search string and hiding the elements that do not.
 * 
 * @param {String} input The search string, which should be searched for in the list.
 * @param {integer} id Id of the table, which shoukd be filtered.
 * 
 */
DragNDropList.filter = function (input, id) {
    var filter = input.val().toUpperCase();
    var elements = $(id).find('.filterName');

    var hiddenCounter = 0;
    for (var i = 0; i < elements.length; i++) {
        if ($(elements[i]).val().toUpperCase().indexOf(filter) > -1) {
            $(elements[i]).parents('.draggable').first().css("display", "");
        } else {
            $(elements[i]).parents('.draggable').first().css("display", "none");
            hiddenCounter++;
        }
    }

    if (hiddenCounter === elements.length && elements.length > 0) {
        //Show text for search without matches
        $(id).find('#filterEmptyRow').css("display", "");
    } else {
        //Hide text if there are matches
        $(id).find('#filterEmptyRow').css("display", "none");
    }
}

/*
 * Changes the position and the direction of the clickable arrow-element
 */
DragNDropList.switchPosition = function(element) {
    var glyphElement = element.children().find('.bi');
    if (glyphElement.hasClass("bi-chevron-left")) {
        element.append(glyphElement.parent().parent());
        $(glyphElement).attr("class", "bi bi-chevron-right"); 
    } else {
        element.prepend(glyphElement.parent().parent());
        $(glyphElement).attr("class", "bi bi-chevron-left"); 
    }
}



 
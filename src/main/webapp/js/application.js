function Application() {
}

// Updates 'data-toggle' to 'data-bs-toggle' for Bootstrap 5 compatibility
// Summernote adds 'button[data-toggle="dropdown"]' to dynamic class elements,
// while Bootstrap 5 requires 'data-bs-toggle="dropdown"' for dropdown functionality
$(document).ready(function() {
    $("button[data-toggle='dropdown']").each(function (index) {
       $(this).removeAttr("data-toggle").attr("data-bs-toggle", "dropdown");
    })
});

// Set options for WYSIWYG-Editor
var wysiwygOptions = {
    onblur: function (e) {
        // Clean content after change
        if ($(e.target).code() === "<br>") {
            $(e.target).html("");
        }
    },
    height: 200,
    toolbar: [
        ['style', ['bold', 'italic', 'underline', 'clear']],
        ['para', ['ul', 'ol', 'paragraph']],
        ['misc', ['undo', 'redo']]
    ],
};

Application.init = function (lang) {
    var language = 'en-US';
    switch (lang) {
        case "de_DE":
            language = 'de-DE';
            break;
        case "es_CL":
        case "es_ES":
            language = 'es-ES';
            break;
        default:
    }

    if (!Modernizr.inputtypes.date) {
        var localized = language.substring(0, language.indexOf('-'));
        
        $.datepicker.setDefaults( $.datepicker.regional[ localized ] );        
        
        $('input[type=date]').datepicker({ 
            dateFormat: 'yy-mm-dd'
        });
    }

    Application.initializeDataTables('.pagedTable', true, lang);
    Application.initializeDataTables('.unpagedTable', false, lang);
    wysiwygOptions.lang = language; // set your language

    // Create WYSIWYG-Editor
    $('.wysiwyg').summernote(wysiwygOptions);
    // Correct the toolbar's left margin 
    $('.note-toolbar.btn-toolbar').css("margin-left", "0px");
    // Add a submit action handler to the form, to delete auto generated
    // summernote content before submitting the form
    $('form').submit(function (e) {
        Application.clearEmptyWysiwygEditor();
    });

    // Remove .note-dropzone-Elements
    $('.note-dropzone').remove();

    $('.note-editing-area').on('dragover drop', function(event) {
        event.preventDefault();
        event.stopPropagation();
        return false;
    });

};

Application.createNewWysiwygEditor = function (elem, language) {
    wysiwygOptions.lang = language;
    // Create WYSIWYG-Editor
    elem.summernote(wysiwygOptions);
    // Correct the toolbar's left margin 
    $('.note-toolbar.btn-toolbar').css("margin-left", "0px");
};

Application.clearEmptyWysiwygEditor = function () {
    // Delete the automatically generated content of the WYSIWYG-Editor
    $('.wysiwyg').each(function(index, elem) {
        if ($(elem).summernote("code") === "<br>") {
            $(elem).destroy();
            $(elem).html("");
        }
    });
};

Application.initializeDataTables = function (selector, paging, language) {
    switch (language) {
        case "de_DE":
            $(selector).dataTable({
                "paging": paging,
                "bSort": false,
                "stateSave": true,
                "language": {
                    "sEmptyTable": "Keine Daten in der Tabelle vorhanden",
                    "sInfo": "_START_ bis _END_ von _TOTAL_ Einträgen",
                    "sInfoEmpty": "0 bis 0 von 0 Einträgen",
                    "sInfoFiltered": "(gefiltert von _MAX_ Einträgen)",
                    "sInfoPostFix": "",
                    "sInfoThousands": ".",
                    "sLengthMenu": "_MENU_ Einträge anzeigen",
                    "sLoadingRecords": "Wird geladen...",
                    "sProcessing": "Bitte warten...",
                    "sSearch": "Suchen",
                    "sZeroRecords": "Keine Einträge vorhanden.",
                    "oPaginate": {
                        "sFirst": "Erste",
                        "sPrevious": "Zurück",
                        "sNext": "Nächste",
                        "sLast": "Letzte"
                    },
                    "oAria": {
                        "sSortAscending": ": aktivieren, um Spalte aufsteigend zu sortieren",
                        "sSortDescending": ": aktivieren, um Spalte absteigend zu sortieren"
                    }
                }
            });
            break;
        case "es_CL":
        case "es_ES":
            $(selector).dataTable({
                "paging": paging,
                "bSort": false,
                "stateSave": true,
                "language": {
                    "sProcessing": "Procesando...",
                    "sLengthMenu": "Mostrar _MENU_ registros",
                    "sZeroRecords": "No se encontraron resultados",
                    "sEmptyTable": "Ningún dato disponible en esta tabla",
                    "sInfo": "Mostrando registros del _START_ al _END_ de un total de _TOTAL_ registros",
                    "sInfoEmpty": "Mostrando registros del 0 al 0 de un total de 0 registros",
                    "sInfoFiltered": "(filtrado de un total de _MAX_ registros)",
                    "sInfoPostFix": "",
                    "sSearch": "Buscar:",
                    "sUrl": "",
                    "sInfoThousands": ",",
                    "sLoadingRecords": "Cargando...",
                    "oPaginate": {
                        "sFirst": "Primero",
                        "sLast": "Último",
                        "sNext": "Siguiente",
                        "sPrevious": "Anterior"
                    },
                    "oAria": {
                        "sSortAscending": ": Activar para ordenar la columna de manera ascendente",
                        "sSortDescending": ": Activar para ordenar la columna de manera descendente"
                    }
                }
            });
            break;
        default:
            $(selector).dataTable({"paging": paging, "stateSave": true, "bSort": false});
    }
};
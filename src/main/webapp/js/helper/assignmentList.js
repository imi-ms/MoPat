(function ($) {
    'use strict';

    const selectors = {
        root: '#questionnaireAssignment',
        assignedList: '#assignedQuestionnaireList',
        assignedItem: '.questionnaire-assignment-item',
        assignedEmptyState: '#assignedQuestionnaireEmptyState',
        selectionModal: '#questionnaireSelectionModal',
        selectionList: '#questionnaireSelectionList',
        selectionRow: '.questionnaire-selection-row',
        selectionCheckbox: '.questionnaire-selection-checkbox',
        selectionFilter: '#questionnaireSelectionFilter',
        selectionEmptyState: '#questionnaireSelectionEmptyState',
        addButton: '#addSelectedQuestionnairesButton',
        selectedCount: '#selectedQuestionnaireCount'
    };

    let sortable;

    function init() {
        if (!$(selectors.root).length) {
            return;
        }

        initializeSorting();
        bindEvents();
        refreshState();
    }

    function initializeSorting() {
        const list = document.querySelector(selectors.assignedList);
        if (!list || typeof Sortable === 'undefined') {
            return;
        }

        sortable = Sortable.create(list, {
            animation: 150,
            handle: '.questionnaire-drag-handle',
            draggable: selectors.assignedItem,
            onEnd: function () {
                reindexAssignedQuestionnaires();
            }
        });
    }

    function bindEvents() {
        $(document)
            .on('click', '.remove-questionnaire-button', removeQuestionnaire)
            .on('change', selectors.selectionCheckbox, updateSelectedCount)
            .on('input', selectors.selectionFilter, filterQuestionnaires)
            .on('click', selectors.addButton, addSelectedQuestionnaires);

        $(selectors.selectionModal).on('show.bs.modal', function () {
            synchronizeSelectionRows();
            $(selectors.selectionFilter).val('');
            filterQuestionnaires();
        });
    }

    function removeQuestionnaire(event) {
        const item = $(event.currentTarget).closest(selectors.assignedItem);
        const questionnaireId = String(item.data('questionnaire-id'));

        item.remove();
        setSelectionRowAssigned(questionnaireId, false);
        refreshState();
    }

    function addSelectedQuestionnaires() {
        const checkedRows = $(`${selectors.selectionRow} ${selectors.selectionCheckbox}:checked:not(:disabled)`)
            .closest(selectors.selectionRow);

        checkedRows.each(function () {
            const row = $(this);
            const questionnaireId = String(row.data('questionnaire-id'));

            if (isAssigned(questionnaireId)) {
                return;
            }

            const item = row.find('.questionnaire-assignment-template').find(selectors.assignedItem).first().clone(false, false);
            item.find(':input').prop('disabled', false);
            $(selectors.assignedList).append(item);
            setSelectionRowAssigned(questionnaireId, true);
        });

        refreshState();

        const modalElement = document.querySelector(selectors.selectionModal);
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) {
            modal.hide();
        }
    }

    function refreshState() {
        reindexAssignedQuestionnaires();
        synchronizeSelectionRows();
        updateAssignedEmptyState();
        updatePublishingState();
        updateSelectedCount();
    }

    function reindexAssignedQuestionnaires() {
        const fieldPrefix = $(selectors.root).data('field-prefix') || 'bundleQuestionnaireDTOs';

        $(`${selectors.assignedList} > ${selectors.assignedItem}`).each(function (index) {
            const item = $(this);
            const position = index + 1;

            item.find('.questionnaire-position span').text(`${position}.`);
            item.find('[data-field="position"]').val(position);

            item.find('[data-field]').each(function () {
                const input = $(this);
                input.attr('name', `${fieldPrefix}[${index}].${input.data('field')}`);
                input.prop('disabled', false);
            });
        });
    }

    function synchronizeSelectionRows() {
        $(selectors.selectionRow).each(function () {
            const row = $(this);
            const questionnaireId = String(row.data('questionnaire-id'));
            setSelectionRowAssigned(questionnaireId, isAssigned(questionnaireId));
        });
    }

    function setSelectionRowAssigned(questionnaireId, assigned) {
        const row = $(`${selectors.selectionRow}[data-questionnaire-id="${escapeSelectorValue(questionnaireId)}"]`);
        const checkbox = row.find(selectors.selectionCheckbox);
        const badge = row.find('.badge');

        checkbox.prop('checked', false).prop('disabled', assigned);
        row.toggleClass('text-muted', assigned);

        if (assigned) {
            if (!badge.length) {
                row.find('.form-check-label').append('<span class="badge bg-secondary float-end questionnaire-assigned-badge">Already added</span>');
            }
        } else {
            row.find('.questionnaire-assigned-badge, .badge').remove();
        }
    }

    function isAssigned(questionnaireId) {
        return $(`${selectors.assignedList} > ${selectors.assignedItem}[data-questionnaire-id="${escapeSelectorValue(questionnaireId)}"]`).length > 0;
    }

    function updateAssignedEmptyState() {
        const isEmpty = $(`${selectors.assignedList} > ${selectors.assignedItem}`).length === 0;
        $(selectors.assignedEmptyState).toggle(isEmpty);
    }

    function updatePublishingState() {
        const isEmpty = $(`${selectors.assignedList} > ${selectors.assignedItem}`).length === 0;
        const publishCheckbox = $('#isPublished1');

        publishCheckbox.prop('disabled', isEmpty);
        if (isEmpty) {
            publishCheckbox.prop('checked', false);
        }
    }

    function updateSelectedCount() {
        const count = $(`${selectors.selectionCheckbox}:checked:not(:disabled)`).length;
        $(selectors.selectedCount).text(`(${count})`);
        $(selectors.addButton).prop('disabled', count === 0);
    }

    function filterQuestionnaires() {
        const term = String($(selectors.selectionFilter).val() || '').trim().toLowerCase();
        let visibleCount = 0;

        $(selectors.selectionRow).each(function () {
            const row = $(this);
            const searchName = String(row.data('search-name') || '');
            const visible = term === '' || searchName.includes(term);
            row.toggle(visible);
            if (visible) {
                visibleCount++;
            }
        });

        $(selectors.selectionEmptyState).toggle(visibleCount === 0);
    }

    function escapeSelectorValue(value) {
        if (window.CSS && typeof window.CSS.escape === 'function') {
            return window.CSS.escape(value);
        }
        return value.replace(/([ #;?%&,.+*~\\':"!^$[\]()=>|/@])/g, '\\$1');
    }

    $(init);
})(jQuery);

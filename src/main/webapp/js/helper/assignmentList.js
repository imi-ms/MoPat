(function ($) {
    'use strict';

    const selectors = {
        root: '#assignmentList',
        assignedList: '#assignedItemList',
        assignedItem: '.assignment-list-item',
        assignedEmptyState: '#assignedItemEmptyState',
        selectionModal: '#itemSelectionModal',
        selectionList: '#itemSelectionList',
        selectionRow: '.assignment-selection-row',
        selectionCheckbox: '.assignment-selection-checkbox',
        selectionFilter: '#itemSelectionFilter',
        selectionEmptyState: '#itemSelectionEmptyState',
        addButton: '#addSelectedItemsButton',
        selectedCount: '#selectedItemCount'
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
        const assignedListElement =
          document.querySelector(selectors.assignedList);

        if (!assignedListElement || typeof Sortable === 'undefined') {
            return;
        }

        sortable = Sortable.create(assignedListElement, {
            animation: 150,
            handle: '.assignment-drag-handle',
            draggable: selectors.assignedItem,
            onEnd: function () {
                reindexAssignedItems();
            }
        });
    }

    function bindEvents() {
        $(document)
          .on(
            'click',
            '.remove-assignment-item-button',
            removeItem
          )
          .on(
            'change',
            selectors.selectionCheckbox,
            updateSelectedCount
          )
          .on(
            'input',
            selectors.selectionFilter,
            filterItems
          )
          .on(
            'click',
            selectors.addButton,
            addSelectedItems
          );

        $(selectors.selectionModal).on(
          'show.bs.modal',
          function () {
              synchronizeSelectionRows();
              $(selectors.selectionFilter).val('');
              filterItems();
          }
        );
    }

    function removeItem(event) {
        const assignedItem = $(event.currentTarget)
          .closest(selectors.assignedItem);

        const itemId = String(
          assignedItem.data('item-id')
        );

        assignedItem.remove();
        setSelectionRowAssigned(itemId, false);
        refreshState();
    }

    function addSelectedItems() {
        const selectedRows = $(
          `${selectors.selectionRow} ` +
          `${selectors.selectionCheckbox}:checked:not(:disabled)`
        ).closest(selectors.selectionRow);

        selectedRows.each(function () {
            const selectionRow = $(this);
            const itemId = String(
              selectionRow.data('item-id')
            );

            if (isAssigned(itemId)) {
                return;
            }

            const assignedItem = selectionRow
              .find('.assignment-item-template')
              .find(selectors.assignedItem)
              .first()
              .clone(false, false);

            assignedItem
              .find(':input')
              .prop('disabled', false);

            $(selectors.assignedList).append(assignedItem);
            setSelectionRowAssigned(itemId, true);
        });

        refreshState();

        const modalElement = document.querySelector(
          selectors.selectionModal
        );

        const modalInstance =
          bootstrap.Modal.getInstance(modalElement);

        if (modalInstance) {
            modalInstance.hide();
        }
    }

    function refreshState() {
        reindexAssignedItems();
        synchronizeSelectionRows();
        updateAssignedEmptyState();
        updatePublishingState();
        updateSelectedCount();
    }

    function reindexAssignedItems() {
        const fieldPrefix =
          $(selectors.root).data('field-prefix') ||
          'assignedItems';

        $(
          `${selectors.assignedList} > ` +
          selectors.assignedItem
        ).each(function (index) {
            const assignedItem = $(this);
            const position = index + 1;

            assignedItem
              .find('.assignment-item-position span')
              .text(`${position}.`);

            assignedItem
              .find('[data-field="position"]')
              .val(position);

            assignedItem
              .find('[data-field]')
              .each(function () {
                  const input = $(this);

                  input.attr(
                    'name',
                    `${fieldPrefix}[${index}].` +
                    input.data('field')
                  );

                  input.prop('disabled', false);
              });
        });
    }

    function synchronizeSelectionRows() {
        $(selectors.selectionRow).each(function () {
            const selectionRow = $(this);
            const itemId = String(
              selectionRow.data('item-id')
            );

            setSelectionRowAssigned(
              itemId,
              isAssigned(itemId)
            );
        });
    }

    function setSelectionRowAssigned(itemId, assigned) {
        const escapedItemId =
          escapeSelectorValue(itemId);

        const selectionRow = $(
          `${selectors.selectionRow}` +
          `[data-item-id="${escapedItemId}"]`
        );

        const checkbox = selectionRow.find(
          selectors.selectionCheckbox
        );

        const assignedBadge = selectionRow.find(
          '.assignment-item-assigned-badge'
        );

        checkbox
          .prop('checked', false)
          .prop('disabled', assigned);

        selectionRow.toggleClass(
          'text-muted',
          assigned
        );

        if (assigned) {
            if (!assignedBadge.length) {
                selectionRow
                  .find('.form-check-label')
                  .append(
                    '<span class="' +
                    'badge bg-secondary float-end ' +
                    'assignment-item-assigned-badge">' +
                    'Already added' +
                    '</span>'
                  );
            }
        } else {
            selectionRow
              .find('.assignment-item-assigned-badge')
              .remove();
        }
    }

    function isAssigned(itemId) {
        const escapedItemId =
          escapeSelectorValue(itemId);

        return $(
          `${selectors.assignedList} > ` +
          `${selectors.assignedItem}` +
          `[data-item-id="${escapedItemId}"]`
        ).length > 0;
    }

    function updateAssignedEmptyState() {
        const hasNoAssignedItems = $(
          `${selectors.assignedList} > ` +
          selectors.assignedItem
        ).length === 0;

        $(selectors.assignedEmptyState)
          .toggle(hasNoAssignedItems);
    }

    function updatePublishingState() {
        const hasNoAssignedItems = $(
          `${selectors.assignedList} > ` +
          selectors.assignedItem
        ).length === 0;

        const publishCheckbox = $('#isPublished1');

        publishCheckbox.prop(
          'disabled',
          hasNoAssignedItems
        );

        if (hasNoAssignedItems) {
            publishCheckbox.prop('checked', false);
        }
    }

    function updateSelectedCount() {
        const selectedItemCount = $(
          `${selectors.selectionCheckbox}` +
          ':checked:not(:disabled)'
        ).length;

        $(selectors.selectedCount)
          .text(`(${selectedItemCount})`);

        $(selectors.addButton)
          .prop('disabled', selectedItemCount === 0);
    }

    function filterItems() {
        const searchTerm = String(
          $(selectors.selectionFilter).val() || ''
        ).trim().toLowerCase();

        let visibleItemCount = 0;

        $(selectors.selectionRow).each(function () {
            const selectionRow = $(this);

            const searchableName = String(
              selectionRow.data('search-name') || ''
            );

            const isVisible =
              searchTerm === '' ||
              searchableName.includes(searchTerm);

            selectionRow.toggle(isVisible);

            if (isVisible) {
                visibleItemCount++;
            }
        });

        $(selectors.selectionEmptyState)
          .toggle(visibleItemCount === 0);
    }

    function escapeSelectorValue(value) {
        if (
          window.CSS &&
          typeof window.CSS.escape === 'function'
        ) {
            return window.CSS.escape(value);
        }

        return value.replace(
          /([ #;?%&,.+*~\\':"!^$[\]()=>|/@])/g,
          '\\$1'
        );
    }

    $(init);
})(jQuery);

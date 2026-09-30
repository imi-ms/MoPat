(function ($) {
    'use strict';

    const SELECTORS = {
        root:                '.assignment-list',
        assignedList:        '.assignment-list__assigned',
        assignedItem:        '.assignment-list-item',
        assignedEmptyState:  '.assignment-list__assigned-empty',
        modal:               '.assignment-list__modal',
        selectionRow:        '.assignment-selection-row',
        selectionCheckbox:   '.assignment-selection-checkbox',
        selectionFilter:     '.assignment-list__filter',
        selectionEmptyState: '.assignment-list__selection-empty',
        addButton:           '.assignment-list__add-selected',
        selectedCount:       '.assignment-list__selected-count',
        removeButton:        '.remove-assignment-item-button',
        dragHandle:          '.assignment-drag-handle',
        itemTemplate:        '.assignment-item-template',
        itemPosition:        '.assignment-item-position',
        assignedBadge:       '.assignment-item-assigned-badge'
    };

    const INSTANCE_KEY = 'assignmentListInstance';


    function escapeAttributeValue(value) {
        return String(value).replace(/(["\\])/g, '\\$1');
    }

    function AssignmentList(rootElement) {
        this.$root = $(rootElement);
        this.sortable = null;
    }

    /* ---------- Scoping ---------------------------------------------- */

    /**
     * Searches within THIS instance only.
     * Elements belonging to a nested assignment list are filtered out.
     */
    AssignmentList.prototype.$in = function (selector, $context) {
        const rootElement = this.$root[0];

        return ($context || this.$root).find(selector).filter(function () {
            return $(this).closest(SELECTORS.root)[0] === rootElement;
        });
    };

    AssignmentList.prototype.owns = function (element) {
        return $(element).closest(SELECTORS.root)[0] === this.$root[0];
    };

    AssignmentList.prototype.$assignedItems = function () {
        return this.$assignedList.children(SELECTORS.assignedItem);
    };

    /* ---------- Lifecycle -------------------------------------------- */

    AssignmentList.prototype.init = function () {
        this.$assignedList        = this.$in(SELECTORS.assignedList).first();
        this.$assignedEmptyState  = this.$in(SELECTORS.assignedEmptyState).first();
        this.$modal               = this.$in(SELECTORS.modal).first();
        this.$filter              = this.$in(SELECTORS.selectionFilter).first();
        this.$selectionEmptyState = this.$in(SELECTORS.selectionEmptyState).first();
        this.$addButton           = this.$in(SELECTORS.addButton).first();
        this.$selectedCount       = this.$in(SELECTORS.selectedCount).first();

        this.fieldPrefix              = this.$root.data('field-prefix') || 'assignedItems';
        this.alreadyAddedText         = this.$root.data('already-added-text') || 'Already added';
        this.disableWhenEmptySelector = this.$root.data('disable-when-empty');

        this.initializeSorting();
        this.bindEvents();
        this.refreshState();
    };

    AssignmentList.prototype.initializeSorting = function () {
        const assignedListElement = this.$assignedList[0];

        if (!assignedListElement || typeof Sortable === 'undefined') {
            return;
        }

        const self = this;

        this.sortable = Sortable.create(assignedListElement, {
            animation: 150,
            handle: SELECTORS.dragHandle,
            draggable: SELECTORS.assignedItem,
            onEnd: function () {
                self.reindexAssignedItems();
            }
        });
    };

    AssignmentList.prototype.bindEvents = function () {
        const self = this;

        // Delegate on the root element, not on document.
        this.$root
          .on('click', SELECTORS.removeButton, function (event) {
              if (!self.owns(event.currentTarget)) { return; }
              self.removeItem(event);
          })
          .on('change', SELECTORS.selectionCheckbox, function (event) {
              if (!self.owns(event.currentTarget)) { return; }
              self.updateSelectedCount();
          })
          .on('input', SELECTORS.selectionFilter, function (event) {
              if (!self.owns(event.currentTarget)) { return; }
              self.filterItems();
          })
          .on('click', SELECTORS.addButton, function (event) {
              if (!self.owns(event.currentTarget)) { return; }
              self.addSelectedItems();
          });

        this.$modal.on('show.bs.modal', function () {
            self.synchronizeSelectionRows();
            self.$filter.val('');
            self.filterItems();
        });
    };

    /* ---------- Actions ----------------------------------------------- */

    AssignmentList.prototype.removeItem = function (event) {
        const $assignedItem = $(event.currentTarget).closest(SELECTORS.assignedItem);
        const itemId = String($assignedItem.data('item-id'));

        $assignedItem.remove();
        this.setSelectionRowAssigned(itemId, false);
        this.refreshState();
    };

    AssignmentList.prototype.addSelectedItems = function () {
        const self = this;

        const $selectedRows = this.$in(SELECTORS.selectionCheckbox)
          .filter(':checked:not(:disabled)')
          .closest(SELECTORS.selectionRow);

        $selectedRows.each(function () {
            const $selectionRow = $(this);
            const itemId = String($selectionRow.data('item-id'));

            if (self.isAssigned(itemId)) {
                return;
            }

            const $assignedItem = $selectionRow
              .find(SELECTORS.itemTemplate)
              .find(SELECTORS.assignedItem)
              .first()
              .clone(false, false);

            $assignedItem.find(':input').prop('disabled', false);

            self.$assignedList.append($assignedItem);
            self.setSelectionRowAssigned(itemId, true);
        });

        this.refreshState();

        const modalInstance = bootstrap.Modal.getInstance(this.$modal[0]);

        if (modalInstance) {
            modalInstance.hide();
        }
    };

    /* ---------- State -------------------------------------------------- */

    AssignmentList.prototype.refreshState = function () {
        this.reindexAssignedItems();
        this.synchronizeSelectionRows();
        this.updateAssignedEmptyState();
        this.updateDisableWhenEmptyTarget();
        this.updateSelectedCount();
    };

    AssignmentList.prototype.reindexAssignedItems = function () {
        const fieldPrefix = this.fieldPrefix;

        this.$assignedItems().each(function (index) {
            const $assignedItem = $(this);
            const position = index + 1;

            $assignedItem
              .find(SELECTORS.itemPosition)
              .first()
              .children('span')
              .first()
              .text(`${position}.`);

            $assignedItem.find('[data-field="position"]').val(position);

            $assignedItem.find('[data-field]').each(function () {
                const $input = $(this);
                $input.attr('name', `${fieldPrefix}[${index}].${$input.data('field')}`);
            });
        });
    };

    AssignmentList.prototype.synchronizeSelectionRows = function () {
        const self = this;

        this.$in(SELECTORS.selectionRow).each(function () {
            const itemId = String($(this).data('item-id'));
            self.setSelectionRowAssigned(itemId, self.isAssigned(itemId));
        });
    };

    AssignmentList.prototype.setSelectionRowAssigned = function (itemId, assigned) {
        const attributeFilter = `[data-item-id="${escapeAttributeValue(itemId)}"]`;

        const $selectionRow  = this.$in(SELECTORS.selectionRow + attributeFilter);
        const $checkbox      = $selectionRow.find(SELECTORS.selectionCheckbox);
        const $assignedBadge = $selectionRow.find(SELECTORS.assignedBadge);

        $checkbox
          .prop('checked', false)
          .prop('disabled', assigned);

        $selectionRow.toggleClass('text-muted', assigned);

        if (assigned) {
            if (!$assignedBadge.length) {
                const $badge = $(
                  '<span class="badge bg-secondary float-end assignment-item-assigned-badge"></span>'
                ).text(this.alreadyAddedText);

                $selectionRow.find('.form-check-label').first().append($badge);
            }
        } else {
            $assignedBadge.remove();
        }
    };

    AssignmentList.prototype.isAssigned = function (itemId) {
        return this.$assignedItems()
          .filter(`[data-item-id="${escapeAttributeValue(itemId)}"]`)
          .length > 0;
    };

    AssignmentList.prototype.updateAssignedEmptyState = function () {
        this.$assignedEmptyState.toggle(this.$assignedItems().length === 0);
    };

    /**
     * Intentionally targets an element OUTSIDE the component
     * (e.g. '#isPublished1') and is therefore resolved document-wide.
     */
    AssignmentList.prototype.updateDisableWhenEmptyTarget = function () {
        if (!this.disableWhenEmptySelector) {
            return;
        }

        const $target = $(this.disableWhenEmptySelector);

        if (!$target.length) {
            return;
        }

        const hasNoAssignedItems = this.$assignedItems().length === 0;

        $target.prop('disabled', hasNoAssignedItems);

        if (hasNoAssignedItems) {
            $target.prop('checked', false);
        }
    };

    AssignmentList.prototype.updateSelectedCount = function () {
        const selectedItemCount = this.$in(SELECTORS.selectionCheckbox)
          .filter(':checked:not(:disabled)')
          .length;

        this.$selectedCount.text(`(${selectedItemCount})`);
        this.$addButton.prop('disabled', selectedItemCount === 0);
    };

    AssignmentList.prototype.filterItems = function () {
        const searchTerm = String(this.$filter.val() || '').trim().toLowerCase();

        let visibleItemCount = 0;

        this.$in(SELECTORS.selectionRow).each(function () {
            const $selectionRow = $(this);
            const searchableName = String($selectionRow.data('search-name') || '');
            const isVisible = searchTerm === '' || searchableName.includes(searchTerm);

            $selectionRow.toggle(isVisible);

            if (isVisible) {
                visibleItemCount++;
            }
        });

        this.$selectionEmptyState.toggle(visibleItemCount === 0);
    };

    /* ---------- Bootstrapping ------------------------------------------ */

    function initAll(context) {
        $(context || document).find(SELECTORS.root).addBack(SELECTORS.root).each(function () {
            const $root = $(this);

            if ($root.data(INSTANCE_KEY)) {
                return;
            }

            const instance = new AssignmentList(this);
            $root.data(INSTANCE_KEY, instance);
            instance.init();
        });
    }

    window.AssignmentList = {
        initAll: initAll,
        get: function (element) {
            return $(element).closest(SELECTORS.root).data(INSTANCE_KEY) || null;
        }
    };

    $(function () {
        initAll(document);
    });
})(jQuery);
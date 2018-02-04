# TODO
    - Menu:
        - Log File
            - Open
            - Save
        - View
            - Monthly
            - Timeline
        = Log Entries
            - New Log Entry
            - Delete Log Entry
            - List of open log entry views
        - Settings
            - Author
            - Auto Save?

    - Update LogBookEditor/LogBookFile to support creating a backup at the start of the session
        and then not create a backup after that.

    - Add ability to select individual entries in ItemListCell.
        - If more items than will fit, pop up list view, or maybe larger ItemListCell.
        - Also need the +more entry at the bottom.

        - Somehow support an item that spans multiple DayCells.
        - Maybe rethink the DayCell based approach?

    - Linear view:
        - Just a ListView with a custom ListCell that encapsulates DayCell?

    - Add means of adding tags already in use to the log entry view's tag editor.
        - Could just be a choice or combo box control with a list of all the tags currently in use plus
        an extra entry that's just to indicate the use, maybe down arrow.
        When the value of the control changes, insert the tag and revert the value back
        to the placeholder value.

# Bugs


# Design Thoughts

QuickListView:
    - Limited size list option.
    - Displays wrappable items.
    - If more items than fit in the height, display extra row with '+ n more'.
    - Click on an item, open editor, but editor is separate.



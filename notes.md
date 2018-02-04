# TODO
    - Menu:
        - Log File: OK
        - View
            - Monthly
            - Timeline
        - Log Entries: OK
        - Settings
            - Author

    - Add undo to LogBookEditor.

    - Add ability to select individual entries in ItemListCell.
        - If more items than will fit, pop up list view, or maybe larger ItemListCell.
        - Also need the +more entry at the bottom.

        - Somehow support an item that spans multiple DayCells.
        - Maybe rethink the DayCell based approach?

    - Linear view:
        - Just a ListView with a custom ListCell that encapsulates DayCell?


# Bugs


# Design Thoughts

QuickListView:
    - Limited size list option.
    - Displays wrappable items.
    - If more items than fit in the height, display extra row with '+ n more'.
    - Click on an item, open editor, but editor is separate.



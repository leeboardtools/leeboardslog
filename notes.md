# TODO
    - Update LogBookEditor/LogBookFile to support creating a backup at the start of the session
        and then not create a backup after that.

    - Add ability to select individual entries in ItemListCell.
        - If more items than will fit, pop up list view, or maybe larger ItemListCell.
        - Also need the +more entry at the bottom.

        - Somehow support an item that spans multiple DayCells.
        - Maybe rethink the DayCell based approach?

    - Linear view:
        - Just a ListView with a custom ListCell that encapsulates DayCell?

    - Add a priority to LogEntry, a double value. What's the idea behind the priority?
        to be able to filter out different levels of detail. Maybe call it detail level,
        and not a double:
        - Big picture
        - Highlight
        - Detail

# Bugs


# Design Thoughts

QuickListView:
    - Limited size list option.
    - Displays wrappable items.
    - If more items than fit in the height, display extra row with '+ n more'.
    - Click on an item, open editor, but editor is separate.



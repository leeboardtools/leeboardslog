# TODO
    - Continue with LogBookViewController.onEditStart(), onEditCommit(), onEditCancel() (can
        probably ignore onEditCommit())
      Add primary LogEntryView to LogBookEditor.

    - Update LogBookEditor/LogBookFile to support creating a backup at the start of the session
        and then not create a backup after that.

    - Add ability to select individual entries in ItemListCell.
        - If more items than will fit, pop up list view, or maybe larger ItemListCell.
        - Also need the +more entry at the bottom.

        - Somehow support an item that spans multiple DayCells.
        - Maybe rethink the DayCell based approach?

    - Restructure main controller.

    - Linear view:
        - Just a ListView with a custom ListCell that encapsulates DayCell?

    - Add a priority to LogEntry, a double value.

# Bugs


# Design Thoughts

Main Concepts:
    - Have a main view, LogBookView, which reflects an active date.
        The main view can display different views of the log file contents:
        - Calendar view (monthly)
        - Linear view (vertical list by date)
        - Timeline view (horizontal or vertical timeline)

    - Editors edit a specific entry. Each entry has:
        - Title
        - Start Time, End Time (Date, Time Pickers)
        - Tags (popup list with checkboxes) Tags editor
        - Markup selector (could just be a context thing)
        - Text entry.



- LogBookEditor - The central controller for editing a LogBook/LogBookFile. Will need to track
    LogBookEditors and LogBookViews.

- LogBookView - The main view of a LogBook. Used to view and select LogEntrys.
    Several types within the main view:
    - CalendarView - Monthly calendar. 
        - Month, Year selectors at top.

    - LinearView - ListView style. 
        - DatePicker for picking the active date.
        - Buttons for adding, deleting LogEntrys.

    - TimeLineView - TBD.
        - Month, Year selectors at top.

    Other options:
    - Menu
        - Author
        - Current Time Zone
        - View Style
        - Open Log Entry editors
        - Preferences?


- MonthlyView:
    - Click on day:
        - If day is active, then start editing.
        - Otherwise make day active.
    - Enter key: Start editing.
    - Esc key: Cancel editing?
    - Editing performed by the DayCell? Or just fire an ActionEvent?

    - If DayCell does editing:
        - startEdit() will fire a start edit event.
        - commitEdit() will fire a commit edit event.
        - cancelEdit() will fire a cancel edit event.

        How will these be handled by the LogBook?
            LogBookEditor will keep track of a primary LogEntryView.
            The primary LogEntryView can be pinned as a separate view.
            The active LogEntryView will be associated with the active cell.
            If the active date is changed after the LogEntryView's contents have
            been modified, will need to prompt to save/discard/cancel before activating
            the new active date.
            
            LogBookEditor will therefore process any startEdit() events IF the active
            LogEntryView is not open.


QuickListView:
    - Limited size list option.
    - Displays wrappable items.
    - If more items than fit in the height, display extra row with '+ n more'.
    - Click on an item, open editor, but editor is separate.
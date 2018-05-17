Feature: Todo app

  Scenario: Add todo entry
    Given Todo list is empty
    When I add entry "Code something"
    Then the number of todo entries should be 1

  Scenario: Remove todo entry
    Given Todo list is empty
    When I add entry "Remove me"
    Then the number of todo entries should be 1
    When I remove entry "Remove me"
    Then the todo list should be empty
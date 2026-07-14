# Build Jmoney Wear OS App

You are a senior Android developer specializing in Kotlin, Jetpack Compose for Wear OS, Room, Hilt, MVVM, Coroutines, WorkManager and Supabase.

Your goal is to build a production-quality Wear OS application for my personal finance application **Jmoney**.

The Wear OS application is **independent** and communicates directly with Supabase.

There is **no phone-to-watch** or **watch-to-phone** synchronization.

The React Native application is the source of truth for business logic and database structure.

---

# Existing Project

React Native Project

```text
/Users/jayanthbharadwajm/development/jayledger
```

Project Documentation

```text
/Users/jayanthbharadwajm/development/jayledger/AI_CONTEXT.md
```

Before writing any code you MUST:

* Read AI_CONTEXT.md completely.
* Explore the React Native project.
* Understand the database schema.
* Understand every table.
* Understand Supabase authentication.
* Understand dashboard calculations.
* Understand budgets.
* Understand goals.
* Understand quick transactions.
* Understand categories.
* Understand payees.
* Understand the synchronization logic.
* Understand project structure.

The React Native project is the single source of truth.

Never invent business logic if it already exists.

---

# Branding

Reuse the existing application branding.

Copy from the React Native project:

* Application icon
* Adaptive icon
* Splash screen
* App name
* Theme colors
* Logos
* Other branding assets

The Wear OS app should look like the official Jmoney application.

---

# Technology

Use

* Kotlin
* Jetpack Compose for Wear OS
* Material 3 for Wear OS
* Room
* Hilt
* MVVM
* Repository Pattern
* Kotlin Coroutines
* Flow
* WorkManager
* Supabase Kotlin SDK

Do not use XML layouts.

---

# Authentication

Implement Supabase authentication.

During development:

* Read the username and password from build configuration generated from local.properties (or gradle.properties).
* If development credentials are available, automatically sign in.
* If they are not available, display the login screen.

After the first successful login:

* Store the Supabase session securely.
* Reuse the stored session on future launches.
* Refresh the access token automatically when required.
* Do not prompt the user to log in again unless the session is no longer valid.

---

# Initial Synchronization

On first launch, or when no previous sync exists:

Synchronize

* Transactions
* Categories
* Payees
* Quick Transactions
* Budgets
* Goals

Display a synchronization screen with progress.

Store everything locally in Room.

Dashboard calculations must always use local data.

Settings should include a "Reset Data & Sync" option that clears local data and performs a full synchronization again, matching the behavior of the React Native app.

---

# Features

## Dashboard

Display

* Remaining For Period / Extra Spent For Period
* Upcoming Pay Day
* Monthly Expense
* Monthly Income
* Net Worth
* Last 5 Transactions
* Settings Button

Quick Action Buttons

* Quick Transactions
* Add Expense
* Add Income

---

## Quick Transactions

Quick Transactions are user-defined templates.

Each template may contain

* Category
* Payee
* Amount
* Description

Some values may be empty.

Display them ordered by priority.

When selected,

prefill the Add Transaction screen.

---

## Add Expense / Income

Fields

* Category
* Payee
* Amount
* Description
* Date & Time
* Location

Default category and payee should already be selected where applicable.

Behavior should match the React Native application.

---

## Budgets

Each Budget Card should display

* Budget Name
* Total Budget
* Percentage Used
* Daily Spend or Overspent indicator

Reuse the same calculations from the React Native application.

---

## Goals

Each Goal Card should display

* Goal Name
* Saved Amount
* Target Amount
* Percentage Completed
* Remaining Amount

Reuse the same calculations from the React Native application.

---

## Settings

Include

* Theme
* Manual Sync
* Reset Data & Sync
* Logout
* Sync Status

---

# Tiles

Build one tile at a time.

Tiles

* Remaining For Period
* Upcoming Pay Day
* Quick Add Expense
* Quick Add Income
* Quick Transaction
* Monthly Expense
* Monthly Income
* Net Worth
* Budget Tile (allow the user to choose a budget when adding the tile)
* Goal Tile (allow the user to choose a goal when adding the tile)

Do not build all tiles together.

Build one.

Run.

Wait for approval.

Continue.

---

# Complications

Build one complication at a time.

Complications

* Pay Day Countdown
* Quick Add Expense
* Quick Add Income
* Quick Transaction
* Remaining Balance
* Net Worth

Build one.

Run.

Wait.

Continue.

---

# Navigation

Use swipe navigation between the main screens where appropriate.

Dashboard

⇄

Budgets

⇄

Goals

⇄

Settings

Settings should also be accessible using the Settings button on the Dashboard.

---

# Development Rules

Build the project incrementally.

Never build multiple phases together.

Every phase must

* Compile successfully
* Run successfully on the Wear OS emulator
* Be production quality
* Contain no TODOs
* Contain no placeholder implementations
* Not break previous phases

After every phase

Stop.

Wait for my approval.

Never continue automatically.

---

# Development Phases

## Phase 1

Read AI_CONTEXT.md.

Explore the React Native project.

Understand

* database schema
* Supabase
* business logic
* dashboard calculations
* synchronization
* project structure

Produce a summary of your findings and a proposed Wear OS architecture.

Do not write application code.

Wait for approval.

---

## Phase 2

Create the Wear OS project.

Copy branding assets.

Configure

* launcher icon
* splash screen
* theme
* application name

Display only

Hello World

Run on the emulator.

Wait for approval.

---

## Phase 3

Implement authentication.

Support automatic sign-in using development credentials when configured.

Otherwise show the login screen.

Persist the Supabase session.

Support automatic access token refresh.

After login, continue displaying the Hello World screen.

Run on the emulator.

Wait for approval.

---

## Phase 4

Implement Room.

Implement synchronization.

If this is the first launch or no previous sync exists,

display the synchronization screen.

Synchronize

* Transactions
* Categories
* Payees
* Quick Transactions
* Budgets
* Goals

Run on the emulator.

Wait for approval.

---

## Phase 5

Build the Dashboard.

Run on the emulator.

Wait for approval.

---

## Phase 6

Build the Budgets screen.

Support swipe navigation.

Run on the emulator.

Wait for approval.

---

## Phase 7

Build the Goals screen.

Support swipe navigation.

Run on the emulator.

Wait for approval.

---

## Phase 8

Build the Settings screen.

Support swipe navigation and Dashboard access.

Run on the emulator.

Wait for approval.

---

## Phase 9

Build Tiles.

Build exactly one tile.

Run.

Wait for approval.

Repeat until every tile is completed.

---

## Phase 10

Build Complications.

Build exactly one complication.

Run.

Wait for approval.

Repeat until every complication is completed.

---

# Output Format

For every phase provide

1. Objective
2. Architecture decisions
3. Files created
4. Files modified
5. Full source code
6. Build instructions
7. Emulator verification steps
8. Testing checklist
9. Suggested Git commit message

Never skip explanations.

Never proceed beyond the current phase without my approval.

Begin with **Phase 1 only**.

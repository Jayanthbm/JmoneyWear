# MATERIAL 3 EXPRESSIVE MIGRATION PLAN

This document outlines the visual and interaction design audit, design token mappings, component architecture, and implementation strategy to migrate **Jmoney** (Wear OS application) to Google's latest **Material 3 Expressive** design system.

---

## 1. Current Design Analysis

### Existing Design Language
Jmoney currently uses a standard Material 3 style tailored for Wear OS, but it leans heavily on legacy, rigid layouts:
*   **Colors**: Custom, high-contrast palette defined in `Color.kt` (e.g., solid blue, dark gray/slate background, high-contrast greens/reds).
*   **Typography**: Default `MaterialTheme.typography` styles without expressive scale variations.
*   **Spacing**: Mostly generic `dp` padding values with tight layouts on small round displays.
*   **Component Shapes**: Medium-rounded card shapes (approx. `12.dp` to `16.dp` corner radius) and traditional round buttons.

### Inconsistencies & Areas for Redesign
1.  **Metric Cards**: Plain container shapes on the Dashboard with basic vertical padding and text layouts. They lack expressive typography hierarchy and comfortable touch targets.
2.  **Buttons**: Standard `ActionButton` composables that do not utilize expressive asymmetry or fluid hover/press states.
3.  **Progress Indicators**: Custom horizontal progress bars in Budgets and Goals with rigid corner clipping and low-contrast today/progress indicators.
4.  **Transitions**: Immediate screen replacement without spatial navigation cues or fluid gestures.
5.  **Form Elements**: Traditional text fields and pickers that need larger touch targets and clearer visual hierarchy on round screens.

---

## 2. Material 3 Expressive Mapping

The migration will map every screen from the current layout to a modern Material 3 Expressive layout:

### A. Theme and Colors
*   **Background**: Deep, responsive organic dark tones (e.g., HSL-tailored soft dark grays rather than pitch black or high-contrast dark blue).
*   **Shapes**: Large rounded corners (`24.dp` to `28.dp` or fully pill-shaped) to match the Wear OS circular screen contour.
*   **Typography**: Use Google Fonts or modern system type scales with expressive weight contrasts (e.g., Bold, wide numerals for balances and compact, legible labels for secondary text).

### B. Screen Mappings

```
Current Design ──> Recommended Material 3 Expressive Design
```

#### Dashboard Screen
*   **Current**: Vertical list of standard metric cards, small action buttons (`Expense`, `Income`, `Quick Transaction`), and list of recent transactions.
*   **Expressive Redesign**:
    *   *Typography*: Extra large, expressive numeric display for "Remaining for Period" with lighter weight secondary currency labels.
    *   *Quick Actions*: Shift to modern Wear OS pill-shaped buttons with clear icon-to-text hierarchy, increased touch targets (min. `48.dp`), and subtle ripple animations.
    *   *Metric Cards*: Use expressive backgrounds, high color-contrast borders, and rounded shapes (`24.dp` radius) that follow the circular viewport.
    *   *Recent Transactions*: List items with larger rounded cards, clean icon templates, and better vertical spacing.

#### Budgets Screen
*   **Current**: Traditional card rows showing budget name, spent amount, and custom flat progress bar.
*   **Expressive Redesign**:
    *   *Card Shape*: High corner-radius container cards (`24.dp` to fully pill-shaped).
    *   *Progress Bar*: Thick, highly visible tracking bars with rounded endpoints, subtle glow for overspent budgets, and clean vertical dotted line for "Today's progress".

#### Goals Screen
*   **Current**: Basic cards showing target vs. current amount and standard progress bar.
*   **Expressive Redesign**:
    *   *Visuals*: Expressive category iconography (large emoji or modern vector graphics).
    *   *Progress*: Circular progress indicators or thick expressive progress bars, integrating dynamic colors based on target completion.

#### Settings Screen
*   **Current**: Flat Info Cards for Account & Last Sync, standard flat buttons for Sync and Sign Out.
*   **Expressive Redesign**:
    *   *Lists*: Standardized Wear OS settings items with left-aligned icons, title + subtitle hierarchy, and full-width touch targets.
    *   *Buttons*: Filled tonal buttons with clean rounded profiles.

#### Login Screen
*   **Current**: Basic email/password input fields and standard login button.
*   **Expressive Redesign**:
    *   *Forms*: High-visibility input fields with large corner radiuses, explicit focus indicators, and custom Material 3 animations for error states.

---

## 3. Animation Plan

All animations follow Material Motion guidelines to feel fluid, light, and natural:

*   **Screen transitions**: Shared horizontal/vertical slide and fade transitions using the Compose Pager.
*   **Button and Card Press**: Scale down slightly (e.g., `0.95f`) on touch-down and bounce back with a spring spec.
*   **Ripple Effects**: Custom organic bounding ripples mapped to the shape of the component.
*   **Loading State**: Circular orbit/spin animations with easing curves.
*   **Scroll Behaviors**: Transforming lazy columns with scale/alpha adjustments for items entering/leaving the screen edges.

---

## 4. Component Library

We will create/refactor reusable components in the `presentation.common` package:

1.  **ExpressiveButton**: Customized action buttons with pill shapes, modern icons, and scale-press behaviors.
2.  **ExpressiveCard**: Standard container card with rounded corners (`24.dp`), subtle borders, and optional click scaling.
3.  **ExpressiveProgressBar**: Clean, thick tracking bar with status colors (Success, Warning, Error) and progress indicators.
4.  **SettingsRow**: List item template with icon, title, and detail slot.

---

## 5. Migration Phases

 we will execute the migration incrementally:
*   **Phase 1**: Theme & Component Design system setup
*   **Phase 2**: Dashboard Screen
*   **Phase 3**: Budgets Screen
*   **Phase 4**: Goals Screen
*   **Phase 5**: Settings Screen
*   **Phase 6**: Login & Sync Screens
*   **Phase 7**: Wear OS Tiles

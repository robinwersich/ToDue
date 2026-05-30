# Organizer Architecture

The organizer is the main screen of ToDue. It presents a 2D infinite-scroll interface where users navigate temporal granularity (horizontal) and time (vertical).

## Domain Model

### Date & Time Types

- **`DateRange`** (`ClosedRange<LocalDate>`) — inclusive date range with `size`, `center`, and `toDoubleRange()` extensions.
- **`DateSequence`** — iterable `DateRange` created via `LocalDate.rangeTo()`.
- **`TimeUnit`** — enum: `DAY(1f)`, `WEEK(7f)`, `MONTH(30.5f)`. Each has a `referenceSize` and factory `instanceFrom(date)`.
- **`TimeBlock`** — interface extending `DateRange`. A date range with semantic meaning (a specific day, week, or month).
- **`TimeUnitInstance`** — sealed interface extending `TimeBlock`. Supports `+`/`-` arithmetic and `..` range creation.
  - **`Day(date: LocalDate)`** — single day.
  - **`Week(yearWeek: YearWeek)`** — Monday through Sunday.
  - **`Month(yearMonth: YearMonth)`** — calendar month.
- **`TimeUnitInstanceSequence`** — a range of same-unit instances. Both a `Sequence<TimeUnitInstance>` and a `TimeBlock` itself.

### Timeline & Sections

- **`Timeline(id: Long, timeUnit: TimeUnit)`** — an infinite series of `TimeBlock`s at a specific granularity. Ordered by `id` (smaller id = finer granularity). `timeBlockFrom(date)` creates the block containing that date.
- **`TimelineSection<TSection : DateRange>(timelineId: Long, section: TSection)`** — anchors a date range to a specific timeline.
  - **`TimelineRange`** = `TimelineSection<DateRange>` — arbitrary date range in a timeline.
  - **`TimelineBlock`** = `TimelineSection<TimeBlock>` — semantic block in a timeline (the common usage).

### Tasks

- **`Task(id, text, scheduledBlock: TimelineBlock, dueDate: LocalDate, doneDate: LocalDate?)`** — a todo item scheduled in a specific timeline block. Due date is independent from the scheduled block.
- **`TaskBlock(timelineBlock: TimelineBlock, tasks: List<Task>)`** — a timeline block with its associated tasks. Marked `@Immutable` for Compose stability.

## Navigation State

The organizer navigation is controlled by two independent draggable axes combined into a single state.

### Position Types

- **`TimelineNavPosition(timeline: Timeline, child: Timeline?)`** — which timeline(s) are visible. `showChild` is true when `child != null` (split-view mode). `visibleTimelines` yields child first, then parent.
- **`NavigationPosition(timelineNavPos, date, dateRange, timeBlock)`** — complete snapshot of what the user sees. `timelineBlock` is derived from the nav position.

### NavigationState

`@Stable` class managing two `AnchoredDraggableState` instances:

**Date axis (vertical):** `AnchoredDraggableState<LocalDate>` with three anchors — previous, current, and next time block center dates. Anchor spacing accounts for block size and split-view date range.

**Timeline axis (horizontal):** `AnchoredDraggableState<TimelineNavPosition>` with up to three anchors:
- From fullscreen: `[show-child (split), current (full), show-parent (split)]`
- From split view: `[child-only (full), current (split), parent (full)]`

**Key properties:**
- `currentNavPos`, `currentDate`, `currentTimeline`, `currentTimeBlock`, `currentTimelineBlock` — derived from both draggable states.
- `isSplitView` — whether a child timeline is visible.
- `navPosTransition: SwipeableTransition<NavigationPosition>` — interpolates between two adjacent positions during a swipe, combining both axes.
- `visibleTimelineBlocks` — all blocks currently rendered (includes transition targets + margin buffer).
- `focussedTimelineBlocksFlow` — blocks that are currently expanded.
- `activeTimelineBlocksFlow` — blocks that currently show data.

**Split-view mechanics:**
- `childTimelineSizeRatio = 0.3f` — child takes 30% width (left), parent 70% (right).
- `getChild()`/`getParent()` — navigate the timeline hierarchy by sorted index.
- `tryAnimateToChild(childBlock)` — sets child focus date and animates into split view.
- `animateToParent()` — collapses split view back to fullscreen.
- Anchor positions shift by `childTimelineSizeRatio * viewportWidth` when entering/leaving split view.

## Display & Rendering

### Style Enums

- **`TimelineStyle`** — `HIDDEN_PARENT`, `PARENT`, `FULLSCREEN`, `CHILD`, `HIDDEN_CHILD`. Determined by `timelineStyle(timelineId, navPos)` based on position relative to the current nav position.
- **`TaskBlockContentMode`** — `FULLSCREEN` or `PARENT`. Controls task rendering density.
- **`TaskBlockClickTarget`** — `CHILD`, `PARENT`, `NONE`. Controls click navigation behavior.

### OrganizerNavigation

The core rendering composable. For each `visibleTimelineBlock`:
- Computes `relativeOffset` (x/y position) and `relativeSize` (width/height fraction) from `TimelineStyle` and focused-block status.
- Interpolates content alpha (1.0 focused fullscreen, 0.8 split, 0.0 preview) and label alpha.
- Applies `anchoredDraggable` (horizontal) and `anchoredDraggableWithNestedScroll` (vertical) for gesture handling.
- Click on child block: `tryAnimateToChild()`. Click on parent: `animateToParent()`.

## Data Flow

```
TimeBlockRepository.getTimelines()
  -> NavigationState.setTimelines()

NavigationState.activeTimelineBlocksFlow
  -> TaskRepository.getTaskBlocksMap()
    -> activeTaskBlocksFlow

NavigationState.currentTimelineBlockFlow
  -> TaskRepository.getTaskBlockFlow()
    -> currentTaskBlockFlow

combine(activeTaskBlocks, currentTaskBlock, focussedTimelineBlocks)
  -> focussedTaskBlockViewStatesFlow: Map<TimelineBlock, TaskBlock>
```

The ViewModel handles events (`AddTask`, `UpdateTask`, `DeleteTask`, `SetTaskDone`) via repository calls.

## Data Layer

- **Entities:** `TimelineEntity(id, timeUnit)`, `TaskEntity(id, text, scheduledTimelineSection, dueDate, estimatedDuration, doneDate)`, `TimelineSectionColumns(timelineId, start, endInclusive)`.
- **DAOs:** `TimelineDao.getTimelines()` returns `Flow<List<TimelineEntity>>`. `TaskDao` provides CRUD + date-range queries.
- **Repositories:** `DatabaseTimeBlockRepository` and `DatabaseTaskRepository` map between entities and domain models.

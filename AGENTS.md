# IMPORTANT IMPLEMENTATION RULES — READ BEFORE MAKING CHANGES

### 1. Verify Before Creating Anything

Before implementing any Settings feature:

* Inspect the existing codebase.
* Check whether the functionality already exists.
* Reuse existing ViewModels, repositories, DAOs, models, utilities, and screens whenever possible.
* **Do not create duplicate systems.**
* Do not replace working functionality unnecessarily.
* Do not assume something is missing just because it is not visible from the Settings screen.

If an existing feature is already implemented elsewhere, integrate the Settings option with that existing functionality.

---

### 2. Every Sidebar Item Must Be Independent

Every Settings item must open its **own dedicated screen**.

Do not combine:

* Appearance + Language
* Currency + Calculations
* Security + Privacy
* Backup + Data Management
* Bills + Budgets

Each must have its own screen and navigation destination.

The existing sidebar design should remain unchanged unless a small change is technically necessary.

---

### 3. No Fake Settings

Every setting must actually work.

Do not create switches, dropdowns, buttons, or options that only change visually but have no effect.

For every setting:

**UI → State → Storage → Application behavior**

must be properly connected.

If a setting cannot be implemented correctly, **do not pretend it works**. Mark it as requiring further implementation.

---

# 4. Appearance

Create a dedicated Appearance screen.

Keep the existing Appearance functionality and expand it only with useful options.

Possible options:

* System / Light / Dark
* Existing app theme colors
* Accent color
* App color palette
* Text size
* UI scaling where appropriate
* Compact / Comfortable layout where useful
* Animation preferences where appropriate

All changes must immediately affect the application where applicable.

Do not introduce unnecessary visual settings.

---

# 5. Language

Create a completely separate Language screen.

The language system must be **fully functional throughout the entire application**, not just the Settings screen.

Required languages:

* English
* Hindi
* Bengali
* Marathi
* Punjabi
* French
* Chinese
* Urdu
* Japanese

Add other languages only if there is a genuine reason.

### Critical requirement:

Changing the language must update **all user-visible text**, including:

* Settings
* Dashboard
* Transactions
* Categories
* Budgets
* Savings Goals
* Bills
* Analytics
* Reports
* Export screens
* Dialogs
* Buttons
* Error messages
* Empty states
* Validation messages
* Help/FAQ
* About/What's New

Search the entire codebase for hardcoded user-visible strings.

Do not leave English text hidden inside Kotlin/Compose code.

Also correctly handle:

* Plurals
* Date formatting
* Number formatting
* Currency formatting
* RTL languages such as Urdu
* Text overflow
* Longer translated strings

Do not simply translate the Settings page.

---

# 6. Currency

Create a dedicated Currency screen.

Provide a properly maintained list covering **100+ countries/currencies**.

The user must be able to search by:

* Country
* Currency name
* Currency code
* Currency symbol

Example:

`India`
`Indian Rupee`
`INR`
`₹`

### VERY IMPORTANT — Currency Change Behavior

The user must be able to choose what happens to existing transactions when changing the default currency.

Provide clearly separated options such as:

**Option A — Keep Existing Transactions**

* Existing transactions retain their stored currency/value.
* New transactions use the newly selected default currency.

**Option B — Convert Existing Transactions**

* Existing transactions are converted to the selected currency.
* Conversion must use a real exchange rate.
* The conversion must be performed safely.
* Do not overwrite the original value without a clear user confirmation.

### Statistics Currency

Provide a separate:

**Default Currency for Statistics**

This determines which currency is used when displaying aggregated statistics.

Do not incorrectly add values from different currencies together.

For example:

₹10,000 + $100 must **not** simply become ₹10,100.

Currency conversion must happen before cross-currency aggregation.

---

# 7. Exchange Rate API

For real-time currency conversion:

* Use a legitimate exchange-rate API.
* **Do not invent an API.**
* Verify the API and its terms/documentation before implementation.
* Do not hardcode fake exchange rates.
* Cache the most recent successful exchange rates locally.
* Refresh rates approximately every **10 days**, as requested.
* Do not make an API request every time the user opens a screen.
* Show the last successful rate update date.
* Handle API failure gracefully.

### Important Privacy/Offline Rule

The core finance application must remain functional offline.

If the exchange-rate API is unavailable:

* Existing locally cached rates may be used.
* The user should be informed that the rate is not current.
* Core transaction functionality must continue working.
* Do not make cloud connectivity mandatory for normal finance management.

If an API requires:

* API key
* paid subscription
* cloud account
* network permission
* terms that conflict with the application's privacy/offline requirements

**ASK BEFORE implementing it.**

Do not silently introduce a dependency on a paid or external service.

---

# 8. Bills & Reminders

Rename the Settings item to:

**Bills & Reminders**

The application already has a Bills/Bill Reminder system.

**Do NOT create another Bills system.**

The Settings screen must connect to the existing Bills functionality and configure its behavior.

Possible useful settings:

* Recurring bills
* Default reminder timing
* Due-date behavior
* Recurrence frequency
* Reminder preferences
* Paid/unpaid behavior
* Overdue behavior
* Upcoming bill display
* Default bill category
* Auto-marking behavior where supported

All settings must use the existing Bills data and architecture.

Do not duplicate the database, repository, or Bill ViewModel.

---

# 9. Categories & Tags

Create a dedicated screen.

Support the existing category structure.

Provide management for appropriate sections such as:

* Income categories
* Expense categories
* Other supported transaction categories
* Tags

Allow where supported:

* Create
* Edit
* Delete
* Rename
* Reorder
* Choose icon
* Choose color

Do not allow deletion to silently break existing transactions.

If a category is already used by transactions, handle deletion safely.

---

# 10. Budgets

Create a dedicated Budget Settings screen.

Use the existing budget engine.

Possible settings:

* Default monthly budget
* Budget warning threshold
* 80% indicator
* 90% indicator
* 100% exceeded indicator
* Indicator colors
* Budget display preferences
* Reset behavior where appropriate

The colors must integrate with the application's existing theme.

Do not create another budget calculation system.

---

# 11. Savings Goals

Create a dedicated Savings Goals Settings screen.

Use the existing Savings Goal system.

Useful settings may include:

* Goal display preferences
* Progress visualization
* Completion behavior
* Contribution behavior
* Withdrawal behavior
* Goal sorting
* Goal colors
* Percentage display
* Default preferences

Only implement settings that have a real effect.

---

# 12. Calculations

Create a dedicated Calculations screen.

Include useful mathematical/financial tools where appropriate, such as:

* Average calculator
* Percentage calculator
* Amount difference
* Percentage change
* Split calculation
* Currency conversion
* Money-to-money conversion

Currency conversion must use the same verified exchange-rate system described above.

Do not duplicate currency APIs.

---

# 13. Transactions

Create a dedicated Transaction Settings screen.

Useful settings may include:

* Default transaction type
* Default category
* Default date
* Default behavior after saving
* Remember last category
* Remember last tags
* Receipt behavior
* Notes behavior
* Tag behavior
* Delete confirmation
* Undo behavior
* Transaction sorting
* Group transactions by date

Only include settings that actually affect the existing transaction system.

---

# 14. Backup & Restore

Create a dedicated Backup & Restore screen.

Keep the existing backup functionality.

Do not remove current features.

Possible additions:

* Manual backup
* Restore
* Backup history
* Last backup date
* Backup file location
* Backup validation
* Backup version
* Automatic backup

### Daily Sync / Automatic Backup

Do **not** automatically introduce cloud synchronization.

The application is designed around local/offline data.

If "daily sync" requires:

* Google Drive
* OneDrive
* Firebase
* another cloud provider
* external server
* account login

then **ASK BEFORE IMPLEMENTING IT**.

A local daily automatic backup may be implemented if appropriate without violating the offline architecture.

---

# 15. Data Management

Create a dedicated Data Management screen.

**Do not remove the existing Data Management functionality.**

Keep all currently working options.

Additional useful tools may include:

* Storage usage
* Clear cache
* Export data
* Import data
* Reset data
* Database information
* Exported file management
* Temporary file cleanup

Destructive actions must require confirmation.

---

# 16. Security

Create a dedicated Security screen.

Only implement security features that can actually work.

Potential useful features:

* App lock
* PIN/passcode
* Biometric unlock
* Lock on app restart
* Lock after inactivity
* Hide sensitive information
* Screenshot protection where appropriate
* Secure sensitive screens
* Auto-lock duration

Do not claim biometric or encryption functionality unless it is actually implemented correctly.

---

# 17. Privacy

Create a dedicated Privacy screen.

Include useful privacy controls/information such as:

* App privacy information
* Permission management
* Data storage information
* Local/offline data information
* External network usage
* Sensitive information visibility
* Blur sensitive amounts
* Blur dashboard statistics
* Hide balances
* Hide transaction amounts
* Privacy mode

Any privacy statement shown to the user must accurately describe what the application actually does.

---

# 18. About App

Create a dedicated About App screen.

Include:

* App name
* Version
* Build information where appropriate
* Current application information
* What's New
* Recently added features
* Important changes
* Credits where applicable

**What's New must reflect actual changes.**

Do not invent release notes.

---

# 19. Help & Support

Create a dedicated Help & Support screen.

Provide useful categorized FAQs.

Categories can include:

* Transactions
* Income
* Expenses
* Budgets
* Savings Goals
* Bills & Reminders
* Analytics
* Currency
* Reports
* Backup & Restore
* Privacy
* Security
* Localization
* Troubleshooting

FAQ answers should explain how the **actual application works**.

Do not create generic answers for features that don't exist.

---

# 20. Theme Consistency

Every new screen must automatically use the existing:

* Theme
* Color palette
* Typography
* Icons
* Spacing
* Cards
* Shapes
* Dark mode
* Light mode
* Accent colors

Do not introduce an unrelated design system.

---

# 21. Search/Research Requirement

For features involving external standards, APIs, exchange rates, Android security, localization, currency data, or other current technical information:

**Research/verify the current implementation requirements before implementing them.**

Do not rely on assumptions.

However, research does **not** mean automatically adding an external dependency.

If an external service/API is required, explain:

* Why it is needed
* What service will be used
* Whether it requires an API key
* Whether it is free/paid
* What data leaves the device
* How often it is contacted
* Whether it conflicts with the offline/privacy architecture

**Ask for approval before introducing a new external service when it is not clearly necessary.**

---

# 22. Required vs Recommended vs Optional

Do not treat every improvement as mandatory.

Use:

🔴 **Required** — broken functionality, incorrect calculations, data loss, crashes, security problems.

🟡 **Recommended** — useful improvement but the app works without it.

🟢 **Optional** — nice-to-have enhancement.

If the current implementation is already good:

> **No change recommended.**

Do not add unnecessary complexity simply because a feature could theoretically exist.

---

# 23. Final Verification

After implementation, test every Settings entry individually.

Verify:

* Navigation works.
* Back navigation works.
* Settings persist after restarting the app.
* Changes actually affect the application.
* Dark mode works.
* Light mode works.
* Theme colors work.
* All supported languages work.
* RTL works where applicable.
* Currency formatting works.
* Existing data remains intact.
* Existing functionality has not been broken.
* No duplicate systems were created.
* No dead buttons remain.

---

### Currency Model Guidance
When handling transactions across currencies, preserve each transaction's original stored currency value while using the default currency for new entries and conversions.

# EduExam Pro — Offline Desktop Examination System

EduExam Pro is a Java Swing desktop examination app. It has no website interface, web server, cloud dependency, or internet requirement. It stores accounts, examinations, questions, and results locally in the `data/` folder next to the app.

## Open the app

Install Java 17 or newer. Double-click `Launch_EduExam_Desktop.bat`, or open PowerShell in this folder and run:

```powershell
java -jar online-exam-system-8.0.jar
```

When moving an existing installation, copy the `data/` folder too to keep its local accounts and exam history. A clean installation creates demo accounts and a sample exam.

## Features

### Student
- Register and sign in with a local account; edit profile and change password.
- Browse exam definitions, read instructions, duration, subject, and schedule before starting.
- Attempt timed scheduled exams or use untimed practice mode.
- Navigate questions, jump with the palette, clear answers, and save responses as they are entered.
- Answer MCQ, true/false, and short-answer questions; timed exams submit automatically at expiry.
- See instant scores, pass/fail grade, item breakdown, analytics, leaderboard, and previous results.
- Export exam history to CSV and print scorecards.

### Teacher
- Sign in as a separate teacher role and edit teacher profile details.
- Create, edit, activate, schedule, and remove exams; set subject, instructions, duration, question selection, randomization, and one-attempt policy.
- Search and maintain MCQ, true/false, and short-answer question banks with configurable marks.
- Review student result records, filter submissions, inspect performance summaries, and export reports.

### Administrator
- Manage student and teacher accounts, including add, search, edit, and delete.
- Manage questions, examinations, and the institute results ledger.
- View dashboard statistics, student attempts and averages, filter reports, and export results as CSV.

### Local security and privacy
- Role-specific login and access to account administration.
- Passwords are stored as salted PBKDF2 hashes. Existing local accounts are upgraded automatically after a successful login.
- All records remain on the local computer; no network calls are required.

## Fresh-install demo accounts

- Student: `student1` / `pass123`
- Administrator: `admin1` / `admin123`
- Teacher: `teacher1` / `teach123`

The demo credentials are intended for trying a fresh local installation; change passwords from the profile screen for personal use.

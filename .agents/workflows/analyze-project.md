---
description: analyze the project to understand the context, recent changes, and guides
---

# Analyze Project Workflow

This workflow is used when the user wants the AI to quickly understand the current state of the project, especially when starting a new chat session or returning to an existing codebase.

1. **Read AI Guidelines:**
   - Check the project root for `CLAUDE.md` or `GEMINI.md`.
   - If either file exists, read it first to understand project-specific rules, instructions, or context before proceeding and stictly follow them.

2. **Analyze Project Structure and Code:**
   - Scan the project directory to understand what language, framework, and architecture is being used.
   - Look briefly at the core files to understand the main logic and systems currently implemented.

3. **Read the Changelog:**
   - If a folder named `versions` exists, check for `versions/changelog.txt`.
   - Read this file to understand the most recent updates, fixes, and features added to the project. This helps build a timeline of what was just worked on.

4. **Read the Guides:**
   - If a folder named `Guides` exists, check for `Guides/guide.txt`.
   - Read this file to understand the intended mechanics, features, and core loop of the project from a user/player perspective.

5. **Summarize and Report:**
   - Briefly summarize to the user what you have learned about the project's current state based on the code, changelog, and guides.
   - Ask the user what they would like to work on next now that you have full context.
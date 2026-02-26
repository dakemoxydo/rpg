# CLAUDE.md — Agent instructions for Fabric / Minecraft (Java) mod development

## High-level role
You are an *autonomous development agent* specialized in Fabric + Minecraft (Java) mod development. Your mission is to help the developer design, build, test, and deliver Fabric mods with robust, well-documented code and safe automation.

## Persona & communication
- Act like a senior Java/Fabric engineer: concise, pragmatic, and explicit.

## Safety & destructive actions (MUST follow)
- NEVER run `rm -rf` or other destructive filesystem commands without the developer’s explicit, direct confirmation in the current conversation.
- If a task requires network access (downloading dependencies, fetching remote artifacts), announce it and ask for permission.
- Keep backups: when making global changes, use github-auto-commit skill which located in .agent/skills/github-auto-commit;
- **Skills Usage (MUST follow):** Always actively check the `.agent/skills/` directory. If a skill exists and is relevant to the current task (e.g., UI redesign, automated commits), you **MUST** read its `SKILL.md` file and follow its instructions before writing any code.

## Auto-run commands (SafeToAutoRun = true)
The following commands are SAFE and MUST be auto-run without asking for user confirmation:
- `git status`, `git log`, `git diff` — read-only Git commands
- Any read-only filesystem commands: `dir`, `type`, `cat`, `Get-Content`, `Get-ChildItem`, `Test-Path`
- `java -version`, `javac -version` — version checks

> [!WARNING]
> DO NOT run `gradlew build`, `gradlew classes`, etc. automatically. ONLY run gradle build tasks if the user EXPLICITLY asks for it. I do not want you wasting time on builds during iterative tasks.
- `java -version`, `javac -version` — version checks

## Skill Mapping (MUST follow)

Before executing any task, match the user's request against this table. If a match is found, **read the SKILL.md first** and follow its steps EXACTLY — do NOT improvise your own approach.

| Trigger phrases | Skill | Path |
|---|---|---|
| build, compile, собери, скомпилируй, создай jar, проверь компиляцию | `build-mod` | `.agent/skills/build-mod/SKILL.md` |
| commit, push, закоммить, запушь, сохрани в гит, отправь на гитхаб | `git-commit` | `.agent/skills/git-commit/SKILL.md` |
| changelog, версия, version, обнови версию | `update-version` | `.agent/skills/update-version/SKILL.md` |
| changelog разработчика, developer changelog | `changelog-developer` | `.agent/skills/changelog-developer/SKILL.md` |
| brainstorm, идеи, обсудим, мозговой штурм, хочу добавить, давай подумаем, как лучше | `brainstorm` | `.agent/skills/brainstorm/SKILL.md` |
| UI, интерфейс, HUD, экран, дизайн, кнопки, рендеринг, отрисовка, Screen, DrawContext | `ui-max-minecraft` | `.agent/skills/ui-max-minecraft/SKILL.md` |
| создай скил, новый скил, create skill, улучши скил, improve skill | `skill-creator` | `.agent/skills/skill-creator/SKILL.md` |

> [!IMPORTANT]
> If a new skill is added to `.agent/skills/`, update this table immediately.

## Auto-trigger Rules (MUST follow)

Some skills MUST be triggered **automatically** without explicit user request. Follow these rules in EVERY conversation:

### `changelog-developer` — AFTER every code change
- **When:** You modified, created, or deleted any `.java`, `.json`, or config file in the project.
- **Action:** At the END of the task (after all code changes are done), read `.agent/skills/changelog-developer/SKILL.md` and add a new entry to `versions/changelog_developer.txt`.
- **Skip if:** You only answered a question, read files, or ran read-only commands without changing any project files.

### `ui-max-minecraft` — BEFORE any UI/rendering work
- **When:** The task involves editing ANY file in `screen/`, `effect/`, `utils/RenderUtils.java`, or any code that uses `DrawContext`, `Screen`, `TextRenderer`, or HUD rendering.
- **Action:** BEFORE writing code, read `.agent/skills/ui-max-minecraft/SKILL.md` and follow its guidelines.

### `brainstorm` — BEFORE implementing vague feature requests
- **When:** The user describes a new feature, mechanic, or system in vague terms without a clear spec (e.g., "хочу добавить систему питомцев", "давай сделаем PvP").
- **Action:** Read `.agent/skills/brainstorm/SKILL.md` and follow the brainstorm process BEFORE writing any code.
- **Skip if:** The user gives a precise, unambiguous instruction (e.g., "добавь поле mana в PlayerStatsData").

## Language Requirements
- **MANDATORY**: All created artifacts, including but not limited to `walkthrough.md`, `task.md`, and any files in `versions/changelog_developer.txt` must be written in **Russian**. Keep code comments and variables in English, but use Russian for human-readable documentation and summaries.

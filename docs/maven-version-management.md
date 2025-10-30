# Maven Version Management - Quick Reference Guide

This guide covers version management for the Smart-TV Maven multi-module project using a develop → main workflow with release tags.

---

## 📋 Table of Contents

1. [Checking Current Version](#checking-current-version)
2. [Creating a New Release](#creating-a-new-release)
3. [Emergency: Undo Version Change](#emergency-undo-version-change)

---

## Checking Current Version

### Check parent project version

```bash
mvn -q --non-recursive help:evaluate "-Dexpression=project.version" -DforceStdout
```

**Example output:** `0.8.0-SNAPSHOT` or `1.0.0`

### Check specific module version

```bash
cd tv-server
mvn -q --non-recursive help:evaluate "-Dexpression=project.version" -DforceStdout
cd ..
```

> **Note:** All modules should have the same version as the parent POM.

---

## Creating a New Release

Follow these steps when you're ready to create a new release version (e.g., `v0.8.0`, `v1.0.0`).

### Prerequisites

- [ ] All code changes committed and pushed to `develop` branch
- [ ] Tests passing (`mvn test`)
- [ ] Build successful (`mvn clean install`)
- [ ] Ready to create a stable release

---

### Step 1: Set Release Version on `develop`

**Purpose:** Remove `-SNAPSHOT` suffix to mark this as a release version.

```bash
# Make sure you're on develop branch
git checkout develop
git pull

# Set release version (example: 1.0.0)
mvn -q versions:set "-DnewVersion=1.0.0" -DprocessAllModules -DgenerateBackupPoms=false

# Commit the version change
git add .
git commit -m "Release 1.0.0"
git push
```

> ⚠️ **Important:** Do this on `develop` branch BEFORE merging to `main`!

**What this does:**
- Updates version in all `pom.xml` files (parent + all modules)
- Changes from `1.0.0-SNAPSHOT` → `1.0.0`
- `-DprocessAllModules` ensures all child modules are updated
- `-DgenerateBackupPoms=false` skips creating `.pom` backup files

---

### Step 2: Merge `develop` → `main`

**Purpose:** Bring the release version into the stable `main` branch.

```bash
# Switch to main and update
git checkout main
git pull

# Merge develop into main
git merge develop

# Push to remote
git push
```

---

### Step 3: Create Git Tag on `main`

**Purpose:** Mark this specific commit as a release with a version tag.

```bash
# Make sure you're on main
git checkout main
git pull

# Create annotated tag (example: v1.0.0)
git tag v1.0.0

# Push tag to remote
git push --tags
```

> **Tag naming convention:** Use `v` prefix followed by version number (e.g., `v1.0.0`, `v0.8.0`)

---

### Step 4: Bump `develop` to Next SNAPSHOT

**Purpose:** Prepare `develop` branch for future development work.

```bash
# Switch back to develop
git checkout develop

# Fast-forward merge from main (to include the release commit)
git merge --ff-only main

# Bump to next SNAPSHOT version (example: 1.1.0-SNAPSHOT or 2.0.0-SNAPSHOT)
mvn -q versions:set "-DnewVersion=1.1.0-SNAPSHOT" -DprocessAllModules -DgenerateBackupPoms=false

# Commit the bump
git add .
git commit -m "Bump to 1.1.0-SNAPSHOT"

# Push to remote
git push
```

**What this does:**
- Increments version and adds `-SNAPSHOT` suffix
- `-SNAPSHOT` indicates this is a development version, not a release
- Future commits on `develop` will be part of version `1.1.0-SNAPSHOT`

---

### Complete Release Workflow Summary

```
┌─────────────────────────────────────────────────────────┐
│ Step 1: develop branch                                   │
│   mvn versions:set -DnewVersion=1.0.0                   │
│   git commit -m "Release 1.0.0"                         │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ Step 2: Merge to main                                    │
│   git checkout main                                      │
│   git merge develop                                      │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ Step 3: Tag on main                                      │
│   git tag v1.0.0                                        │
│   git push --tags                                        │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ Step 4: Bump develop to next SNAPSHOT                    │
│   git checkout develop                                   │
│   git merge --ff-only main                              │
│   mvn versions:set -DnewVersion=1.1.0-SNAPSHOT          │
│   git commit -m "Bump to 1.1.0-SNAPSHOT"                │
└─────────────────────────────────────────────────────────┘
```

---

## Emergency: Undo Version Change

If you made a mistake with `mvn versions:set` and haven't committed yet:

```bash
git restore .
```

**Or** if backup POMs were generated (when `-DgenerateBackupPoms=false` was NOT used):

```bash
mvn versions:revert
```

> ⚠️ **Warning:** This only works if you haven't committed the changes yet!

---

## Common Version Patterns

| Version | Meaning |
|---------|---------|
| `0.1.0-SNAPSHOT` | Development version, pre-release |
| `0.1.0` | Stable release version |
| `1.0.0-SNAPSHOT` | Major version development |
| `1.0.0` | Major stable release |
| `1.0.1` | Patch release (bug fixes) |
| `1.1.0` | Minor release (new features) |
| `2.0.0` | Major release (breaking changes) |

**Semantic Versioning (SemVer):** `MAJOR.MINOR.PATCH`
- **MAJOR:** Incompatible API changes
- **MINOR:** Backwards-compatible functionality
- **PATCH:** Backwards-compatible bug fixes

---

## Tips

✅ **Always check current version before making changes:**
```bash
mvn -q --non-recursive help:evaluate "-Dexpression=project.version" -DforceStdout
```

✅ **Always test before releasing:**
```bash
mvn clean test
mvn clean install
```

✅ **Use descriptive commit messages:**
- Good: `"Release 1.0.0"`, `"Bump to 1.1.0-SNAPSHOT"`
- Bad: `"version"`, `"update"`

✅ **Follow the workflow order:** Don't skip steps or change the order!

❌ **Don't tag directly on `develop`:** Always tag on `main` branch

❌ **Don't forget the SNAPSHOT bump:** Without it, develop will have a release version

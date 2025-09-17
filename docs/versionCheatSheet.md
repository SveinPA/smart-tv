# Check current version (Parent)
```text
mvn -q --non-recursive help:evaluate "-Dexpression=project.version" -DforceStdout
```

# Specific module
```text
cd tv-server
mvn -q --non-recursive help:evaluate "-Dexpression=project.version" -DforceStdout
cd ..
```
# New "Release"/Milestone version steps
1: Set milestone version on **develop branch** (without -SNAPSHOT) **BEFORE MERGE TO MAIN!**
```text
mvn -q versions:set "-DnewVersion=0.1.0-init" -DprocessAllModules -DgenerateBackupPoms=false
git commit -am "Release 0.2.0"
```
2: merge **develop** to **main**  

3: Create Tag on main and push
```text
git checkout main
git pull
git tag v0.2.0
git push --tags
```
4: Return to **develop**, Bump to next SNAPSHOT
```text
git checkout develop
git merge --ff-only main
mvn -q versions:set -DnewVersion=0.3.0-SNAPSHOT -DprocessAllModules -DgenerateBackupPoms=false
git commit -am "Bump to 0.3.0-SNAPSHOT"
git push
```
# Regret version change
```text
git restore .
```


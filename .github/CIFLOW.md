# Development Version and Branch Handling

## Branches

The versioning policy follows [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

| Branch Pattern | Purpose |
|---------------|---------|
| `develop` | Main development branch — contains latest development sources of the active version |
| `feature/JNG-NUMBER_short_summary` | Feature branches based on `develop` for new functionality |
| `(release/)X.Y.Z` | Release branches (the `release/` prefix is reserved for CI) |
| `bugfix/JNG-NUMBER_short_summary` | Bug fixes based on release branches — must be applied to newer release and develop branches too |
| `support/JNG-NUMBER_short_summary` | Support branches based on release branches — same merge-forward rules as bugfix |
| `master` | Contains the latest released sources of the active version |

```mermaid
gitGraph
    commit id: "init"
    branch develop
    checkout develop
    commit id: "dev-1"
    branch feature/JNG-1
    commit id: "feat-1"
    commit id: "feat-2"
    checkout develop
    merge feature/JNG-1
    commit id: "dev-2"
    branch release/1.0-beta1
    commit id: "rc-1"
    branch bugfix/JNG-4
    commit id: "fix-1"
    checkout release/1.0-beta1
    merge bugfix/JNG-4
    checkout develop
    merge release/1.0-beta1
    checkout master
    merge release/1.0-beta1 id: "v1.0"
```

## Version Numbers

Version numbers follow semantic versioning with these rules:

| Event | Version Change |
|-------|---------------|
| Start a `feature/` branch | No change — inherits from `develop` |
| Start a `release/` branch | 2nd number on `develop` is incremented |
| Start a `bugfix/` branch | No change — applied on release branches during pre-release testing |
| Start a `support/` branch | 3rd number is incremented — used for minor updates to previous releases |
| Start a `hotfix/` branch | 4th number is incremented — applied to both release and master branches |

## GitHub Action Flows

The CI/CD pipeline consists of four interconnected GitHub Actions workflows.

### build.yml — Main Build

Triggered on pushes to `develop` or pull requests targeting `develop`, `master`, `increment/*`, or `release/*`.

```mermaid
flowchart TD
    A[Push/PR trigger] --> B{Base branch?}
    B -->|master, release/*| C[Version from pom.xml\nwithout -SNAPSHOT]
    B -->|develop, increment/*| D[Version: major.minor.qualifier.date_commitId_branchName]
    C --> E[Build and deploy to Nexus]
    D --> E
    E --> F[Create git tag v_version_]
    F --> G{Branch type?}
    G -->|increment/*, release/*| H[Create merge-pr/_version_ tag]
    H --> I[Trigger merge-pr-tagged.yml]
    G -->|develop| J[Build changelog]
    J --> K[Create GitHub prerelease]
```

### merge-pr-tagged.yml — PR Merge Handler

Triggered when a `merge-pr/*` tag is pushed.

```mermaid
flowchart TD
    A[merge-pr/* tag pushed] --> B[Extract version from tag]
    B --> C{Version format?}
    C -->|major.minor.qualifier| D[Merge PR to master]
    D --> E[Trigger create-release-on-master.yml]
    C -->|other format| F[Squash PR to develop]
    F --> G[Trigger build.yml]
    D --> H[Delete merge-pr tag]
    F --> H
```

### create-release-on-master.yml — Release Publisher

Triggered on pushes to `master`.

```mermaid
flowchart TD
    A[Push to master] --> B[Get version from tag]
    B --> C[Build changelog]
    C --> D[Create GitHub release with changelog]
```

### release.yml — Manual Release

Triggered manually with a version parameter (`auto` or explicit `major.minor.qualifier`).

```mermaid
flowchart TD
    A[Manual trigger with version] --> B{Version = 'auto'?}
    B -->|Yes| C[Read version from pom.xml\nwithout -SNAPSHOT]
    B -->|No| D[Use given version]
    C --> E[Set next version = qualifier + 1]
    D --> E
    E --> F[Create PR on master with release version]
    E --> G[Create PR on develop with next version]
    F --> H[Trigger build.yml]
    G --> I[Trigger build.yml]
```

## How to Develop

Issue tracking uses [JIRA](https://blackbelt.atlassian.net/jira/dashboards).

> **Important:** There is no commit without a ticket number. Every pull request and commit must reference a `JNG-xxx` ticket.

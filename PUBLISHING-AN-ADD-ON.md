# Publishing an Add-On

The master list of add-ons that we index is in this code repository, in the
[add-ons-to-index.json](src/main/resources/add-ons-to-index.json) file.

To add your OMOD or OWA to the index, submit a Pull Request that changes only that file, adding your add-on to the
`toIndex` list. (You do not need to create an OpenMRS JIRA issue for this.)

Before you add an entry, your add-on's released versions must already be published to a Maven repository that we can
index. For most OpenMRS modules this is the [OpenMRS Artifactory](https://openmrs.jfrog.io/), which OpenMRS CI
publishes to when your module is released.

Once your Pull Request is merged, the live index picks up the change automatically, usually within two hours.

## Fields to specify

Every entry must have:

* `uid`: a unique identifier for your add-on. It becomes part of the add-on's URL, so use a fully-qualified,
  URL-friendly id like `org.openmrs.module.reporting` or `org.openmrs.owa.conceptdictionary`.
* `type`: `OMOD` or `OWA`
* `name`: the display name of your add-on, e.g. "Reporting Module"
* `maintainers`: one or more people or organizations, each with a `name` and an optional `url`
* `backend`: which handler we use to find your add-on's versions (see below)

New entries should also include:

* `description`: a few more words about your add-on: what it does and why someone should choose it

You may optionally include:

* `links`: external links, e.g. to your add-on's documentation and source code. Each link has a `rel`
  (e.g. `documentation` or `source`), an `href`, and an optional `title`.
* `tags`: keywords to help people find your add-on
* `status`: e.g. `DEPRECATED` for an add-on that should no longer be used

You will see examples of the format in the file itself.

## Backends

### OpenMRS Artifactory

This is the standard backend, used by almost every add-on in the index. Only released versions are indexed;
SNAPSHOTs are skipped. If your add-on is published to the OpenMRS Artifactory, specify:

```json
"backend": "org.openmrs.addonindex.backend.Artifactory",
"mavenRepoDetails": {
  "groupId": "org.openmrs.module",
  "artifactId": "reporting"
}
```

### Your own Nexus 3 repository

If you host your add-on in your own Nexus 3 repository, also give us its base URL:

```json
"backend": "org.openmrs.addonindex.backend.Nexus3Repo",
"mavenRepoDetails": {
  "repoUrl": "https://nexus.example.org",
  "groupId": "org.openmrs.module",
  "artifactId": "mymodule"
}
```

### Other repositories

We welcome pull requests that add support for indexing other kinds of repositories.

## Testing your entry locally

By default the application fetches the add-on list from GitHub master and ignores your local file. To see your new
entry in a local run, set `add_on_list.strategy: LOCAL` in your custom config, then build and run the application.
See the [README](README.md) for details.

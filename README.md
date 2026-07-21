[![Build with Maven](https://github.com/openmrs/openmrs-contrib-addonindex/actions/workflows/build.yml/badge.svg)](https://github.com/openmrs/openmrs-contrib-addonindex/actions/workflows/build.yml)

# OpenMRS Add-On Index

This application is the _index_ of add-ons for the OpenMRS Platform. It runs at
[addons.openmrs.org](https://addons.openmrs.org).

We support:

* OpenMRS Modules (OMOD files)
* Open Web Apps (OWA files)

This replaces [Modulus](https://github.com/openmrs/openmrs-contrib-modulus), where we used to host and publish modules.
This project has a different aim: add-ons are hosted elsewhere, such as the
[OpenMRS Artifactory](https://openmrs.jfrog.io/), and we index them so they can be searched in one place.

## Publishing your Module or OWA

See [PUBLISHING-AN-ADD-ON](PUBLISHING-AN-ADD-ON.md).

## Testing / Development

The server is a Spring Boot application, written with Java 21 and built with Maven. The web UI is a React SPA, built
with pnpm and Parcel.

The Maven build installs the required Node and pnpm versions automatically, then builds the UI into the Spring Boot
static resources directory. You only need a local Node and pnpm installation if you are working directly in
`src/main/ui`.

It uses Elasticsearch to store its index.

We use [GitHub Actions](https://github.com/openmrs/openmrs-contrib-addonindex/actions/workflows/build.yml) to
continuously build this application.

### Prerequisites

- Java 21
- Maven 3.6.3 or newer, or the included Maven wrapper (`./mvnw`)
- Docker, if you want to run Elasticsearch locally with the command below

### Elasticsearch

You need a running Elasticsearch to run this application. To run one with Docker:

```bash
mkdir esdata
docker run --name es-addons -p 9200:9200 -p 9300:9300 -v "${PWD}/esdata:/usr/share/elasticsearch/data" -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.13.4
```

If your Elasticsearch is not on `http://localhost:9200`, set `elasticsearch.url` in your custom application config.
(See below for how to set this configuration.)

### Building and running the server

From the root of this project:

```bash
./mvnw clean package
java -jar target/addonindex-*.jar
```

Then navigate to http://localhost:8080/.

You can also check that the backend is running with:

```bash
curl http://localhost:8080/api/v1/indexingstatus
```

To run the full test suite, including integration tests:

```bash
./mvnw verify
```

IntelliJ IDEA has nice support for Spring Boot: you can create a Run Configuration for the Application class.

#### Automatic restart/reload

We include `spring-boot-devtools` in this project, so any changes on the classpath are automatically deployed to the
running application, and it is restarted if necessary. (In IntelliJ you would trigger this with the Build Project
command, which is Command-F9 on macOS.)

The workflow for front-end development is:

1. Run the application (e.g. in IntelliJ)
1. Make changes to the UI code
1. (in `src/main/ui`) `pnpm run build:dev`
1. In IntelliJ, `Build Project`
1. Refresh your browser window

#### UI-only development

If you are working directly in `src/main/ui`, install dependencies and build the UI with:

```bash
cd src/main/ui
pnpm install --frozen-lockfile
pnpm run build:dev
```

For a rebuild-on-change workflow, use:

```bash
pnpm run build:watch
```

#### Custom settings

To override the default settings, create a file `config/application.yml` whose contents should be like our
[application.yml](src/main/resources/application.yml) in this source code. For development you might want to create
this file with contents:

```yaml
logging.level:
  org.openmrs.addonindex: DEBUG
```

#### Testing changes to the add-on list

By default the application does **not** read your local copy of `add-ons-to-index.json`. It fetches the list from
GitHub master every two hours, so local edits to that file are ignored. To test changes to the add-on list, set this
in your custom config:

```yaml
add_on_list:
  strategy: LOCAL
```

`LOCAL` reads the file from the classpath, so you need to rebuild (or trigger a devtools restart) after editing it.

You can do the same for the list of OpenMRS core versions:

```yaml
core_version_list:
  strategy: LOCAL
```

#### Reducing bandwidth use

This application is bandwidth-heavy on its first run (for example, it downloads every OMOD version to inspect its
`config.xml`). To save bandwidth, set this in your custom config (though this loses some functionality, like knowing
each version's platform and module requirements):

```yaml
scheduler:
  fetch_details_to_index:
    fetch_extra_details: false
```

## Docker Packaging

This application can be packaged as a Docker image with Spring Boot:

```bash
./mvnw package spring-boot:build-image
```

The image name is configured in `pom.xml` as `openmrs/addonindex`. Note that the default build uses the `development`
profile. For a production-equivalent image, build with:

```bash
./mvnw package spring-boot:build-image -Dprod
```

(Use `-Dprod`, not `-Pproduction`: the `development` profile activates whenever the `prod` property is absent, so
`-Pproduction` would activate both profiles.)

## Deployment

The application is published to Docker Hub as
[`openmrs/addonindex`](https://hub.docker.com/r/openmrs/addonindex). Deployment to OpenMRS infrastructure is managed
outside this repository. If you need a deploy, ask in the
[Add-On Index category on OpenMRS Talk](https://talk.openmrs.org/c/projects/add-on-index).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Questions and discussion are welcome in the
[Add-On Index category on OpenMRS Talk](https://talk.openmrs.org/c/projects/add-on-index).

## License

Licensed under the [Mozilla Public License 2.0](LICENSE) with the OpenMRS Healthcare Disclaimer. © [OpenMRS Inc.](https://openmrs.org)

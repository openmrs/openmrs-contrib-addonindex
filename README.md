[![Build status](https://omrs-shields.psbrandt.io/plan/AD/BAOIS)](https://ci.openmrs.org/browse/AD-BAOIS)

# OpenMRS Add-On Index

This application serves as an _index_ of add-ons for the OpenMRS Platform.

We support:

* OpenMRS Modules (OMOD files)
* Open Web Apps (OWA files).
 
This replaces [Modulus](http://github.com/openmrs/openmrs-contrib-modulus) where we hosted and published modules. The 
aim of this project is different -- we want people to host their OpenMRS add-ons elsewhere (bintray, github releases, etc)
and we will merely index them for easy searching, in one unified index.
    
For convenience we'll also support indexing modules that have already been published to [OpenMRS's maven 
repository](http://mavenrepo.openmrs.org/).

## Publishing your Module or OWA

See [PUBLISHING-AN-ADD-ON](PUBLISHING-AN-ADD-ON.md).

## Testing / Development

The server is a Spring Boot application, written with Java 8, built using maven. The web UI is a ReactJS SPA, built 
with npm (using webpack).

It uses ElasticSearch to store its index.

We use [OpenMRS's Bamboo CI server](https://ci.openmrs.org/browse/AD-BAOIS) to continuously build this application.

To build locally, you need to build the web UI first, and then run the server. (Though if you skip building the web UI 
you'll still be able to see the server's REST API)

### Building the Web UI

You must have NodeJS and NPM installed already.

From `src/main/ui` do

    > npm install
    > npm run build:dev
   
CI does `npm run build:prod` which minimizes js/css.

### ElasticSearch

You need to be running ElasticSearch to run this application. To run this using Docker, do:

	> mkdir esdata
    > docker run --name es-addons -v "$PWD/esdata":/usr/share/elasticsearch/data -d -p 9200:9200 -p 9300:9300 -e ES_JAVA_OPTS="-Xms1g -Xmx1g" elasticsearch:5.1

If you aren't running ElasticSearch on `http://localhost:9200` then you'll need to set `spring.elasticsearch.jest.uris` in
 your custom application config. (See below for how to set this configuration.) 

### Building and running the server

You must have Java 8.

IntelliJ IDEA has nice support for Spring Boot: you can create a Run Configuration for the Application class. 

To run at the command line (from the root of this project, where pom.xml is):

    > mvn clean package
    > // to run integration tests: mvn clean verify
    > java -jar target/addonindex-*.jar
    
Then navigate to http://localhost:8080/

#### Automatic restart/reload

We include `spring-boot-devtools` in this project, so any changes on the classpath are automatically deployed to the 
running application, and it is restarted if necessary. (In IntelliJ you would trigger this with the Build Project 
command, which is Command-F9 on OSX.)

Thus the workflow of doing front-end development is:
  
1. (First, run the application in IntelliJ)
1. Make changes to the javascript code
1. (in `src/main/ui`) `npm run build:dev` (set up an IDE Run Configuration for this)
1. In IntelliJ, `Build Project`
1. Refresh your browser window

#### Custom settings

To override the default settings, create a file `config/application.yml` whose contents should be like our
[application.yml](src/main/resources/application.yml) in this source code. For development you might want to create 
this file with contents:

    logging.level:
      org.openmrs.addonindex: DEBUG
      

This application is bandwidth-heavy on its first run (e.g. it downloads all OMOD versions to inspect their configuration).
 If you want to save bandwidth, set this in your custom config (though this will lose some functionality, like being able 
 to fetch modules by their package):
 
    scheduler:
      fetch_details_to_index:
        fetch_extra_details: false
 
 ### Dev Notes
 
 If you add any new routes to the JS application you must ensure that they are listed in the `addViewControllers` method in 
 [WebMvcConfiguration.java](src/main/java/configuration/WebMvcConfiguration.java). (See the comment in that file for why.)
 
## Docker Packaging

In order to deploy this to OpenMRS staging infrastructure, we package this application as a docker container and publish it 
to [dockerhub](https://hub.docker.com/) as `openmrs/addonindex`. You would do this manually as

    mvn package docker:build
    
but CI builds it automatically, so you don't need to do this.

## Deploying to production

The production server is updated via a webhook whenever you push to the `production` tag on dockerhub.

This is automated in CI as a manual stage in the build plan. In case you need to do this manually, first you need to have 
a docker image to tag, which you do either by:

* (Option A) build everything locally (first the UI then the jar + docker build, and looking for output like `Successfully 
built 
089a664e4a61`)
* (Option B) do `docker pull <id>`
    
Then you need to tag this and push to dockerhub, like: 
 
    docker tag <id> openmrs/addonindex:production
    docker push openmrs/addonindex:production


## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

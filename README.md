## OpenMRS Add-On Index

This application serves as an _index_ of add-ons for the OpenMRS Platform.

Add-ons include both OpenMRS Modules (OMOD files) and Open Web Apps (OWAs).
 
This replaces [Modulus](http://github.com/openmrs/openmrs-contrib-modulus) which we used to host and publish modules. The 
aim of this project is different -- we want people to host their OpenMRS add-ons elsewhere (bintray, github releases, etc)
 and we will merely index them for easy searching.
    
For convenience we'll also support indexing modules that have already been published to [OpenMRS's maven 
repository](http://mavenrepo.openmrs.org/).

## Publishing your Module or OWA

Currently this application contains a hardcoded list of modules. This will soon be replaced, and we'll load these from a 
git repository. Once that happens we'll update this documentation with how you should request that we index your OMOD or 
OWA.

## Testing / Development

The server is a Spring Boot application, written with Java 8, built using maven. The web UI is a ReactJS SPA, built 
with npm (using webpack).

We use [Snap CI](https://app.snap-ci.com/djazayeri/openmrs-contrib-addonindex) to continuously build this application.  

To build locally, you need to build the web UI first, and then run the server. (Though if you skip building the web UI 
you'll still be able to see the server's REST API)

### Building the Web UI

You must have NodeJS and NPM installed already.

From `src/main/ui` do

    > npm install
    > npm run build

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
1. `(in src/main/ui) npm run build` (set up an IDE Run Configuration for this)
1. In IntelliJ, `Build Project`
1. Refresh your browser window

#### Server settings

To override the default settings, create a file `config/application.yml` whose contents should be like our
[application.yml](src/main/resources/application.yml) in this source code. For development you might want to create 
this file with contents:

    logging.level:
      org.openmrs.addonindex: DEBUG


## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
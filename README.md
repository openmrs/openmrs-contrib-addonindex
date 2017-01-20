## OpenMRS Add-On Index

This application serves as an _index_ of add-ons for the OpenMRS Platform.

Add-ons include both OpenMRS Modules (OMOD files) and Open Web Apps (OWAs).
 
This replaces [Modulus](http://github.com/openmrs/openmrs-contrib-modulus) which we used to host and publish modules. The 
aim of this project is different -- we want people to host their OpenMRS add-ons elsewhere (bintray, github releases, etc)
 and we will merely index them for easy searching.
    
For convenience we'll also support indexing modules that have already been published to [OpenMRS's maven 
repository](http://mavenrepo.openmrs.org/).

## Publishing your Module or OWA

TODO

## Testing / Development

This is a Spring Boot application, written with Java 8, built using maven.

IntelliJ IDEA has nice support for Spring Boot: you can create a Run Configuration for the Application class.

To override default settings, create a file `config/application.yml` whose contents should be like our
[application.yml](src/main/resources/application.yml) in this source code. For development you might set it to:

    logging.level:
      org.openmrs.addonindex: DEBUG

To run at the command line:

    > mvn clean verify
    > java -jar target/addonindex-1.0-SNAPSHOT.jar
    
The navigate to http://localhost:8080/

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
# Publishing an Add-On

The master list of add-ons that we index the information and versions of in inside this code repository in the
[add-ons-to-index.json](src/main/resources/add-ons-to-index.json) file.

If you want to add your OMOD or OWA to the list, then you should submit a Pull Request, changing only that file, adding 
the details of your add-on to the `toIndex` list.

## Fields to specify

Regardless of where your module is published, you must provide the following fields:
  
* `uid`: only letters and spaces; must be unique within this file
* `type`: OMOD or OWA
* `name`: The name of your add-on (e.g. "Reporting Module")
* `description`: More words about your module, e.g. what it does and why someone should choose it

### OpenMRS Maven Repository

If your module is published to the OpenMRS Maven Repository, then you also need specify details like the following:
    
    "backend": "org.openmrs.addonindex.backend.OpenmrsMavenRepo",
    "mavenRepoDetails": {
	    "groupId": "org.openmrs.module",
	    "artifactId": "reporting-omod"
	}
 
### Bintray, Github Releases, etc

In the future we intend to support indexing add-ons that you have published via Bintray or Github Releases.
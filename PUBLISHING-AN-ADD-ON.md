# Publishing an Add-On

The master list of add-ons that we index the information and versions of in inside this code repository in the
[add-ons-to-index.json](src/main/resources/add-ons-to-index.json) file.

If you want to add your OMOD or OWA to the list, then you should submit a Pull Request, changing only that file, adding 
the details of your add-on to the `toIndex` list.

(It is _not_ necessary to create an OpenMRS JIRA issue to go with this Pull Request.)

## Fields to specify

Regardless of where your add-on is published, you must provide the following fields:
  
* `uid`: only letters and spaces; must be unique within this file, and should be a fully-qualified name
* `type`: OMOD or OWA
* `name`: The name of your add-on (e.g. "Reporting Module")
* `description`: More words about your module, e.g. what it does and why someone should choose it
  * you don't need to specify this if you're using Bintray, because we will read it from there
* `maintainers`: One or more people or entities who maintain this add-on, with optional links

You may optionally include:
* `links`: external links, e.g. to your module's documentation and source code

You will see examples of the format in the file itself.

### Bintray
 
If your add-on is published to Bintray, you need to specify:

    "backend": "org.openmrs.addonindex.backend.Bintray",
    "bintrayPackageDetails": {
    	"owner": "openmrs",
    	"repo": "owa",
    	"package": "openmrs-owa-conceptdictionary"
    }
    
If your add-on is published to your own Bintray account and also linked to the OpenMRS account, please use the URL that's 
in the OpenMRS account as part of your submission.
 
### OpenMRS Maven Repository

If your module is published to the OpenMRS Maven Repository, then you also need specify details like the following:
    
    "backend": "org.openmrs.addonindex.backend.OpenmrsMavenRepo",
    "mavenRepoDetails": {
	    "groupId": "org.openmrs.module",
	    "artifactId": "reporting-omod"
	}
	
### Github Releases, etc

In the future we intend to support indexing add-ons that you have published via Github Releases. We welcome other 
suggestions. (Or better, pull requests adding support for other repositories!)

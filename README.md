# ABB Knowledge Manager (KM) Seeq Connector
**Owner:**  [jtillett@endeavoreng.com](mailto:jtillett@endeavoreng.com)

**GitHub Organization:**  [endeavoreng](https://github.com/endeavoreng)

**Release:** 100.1.0

# Release Notes
* The primary objective is to transition from pre-CD with the same feature set.
* Transitioned to using the public Seeq Connector SDK as an upstream source.
* Set foundation/architecture for Seeq long-term 3rd party plans.

# Introduction

This Seeq connector for the ABB Knowledge Manager Server enables a data connection between ABB Knowledge Manager 
(ABBKM) and Seeq.   

# Documentation

For more on the EEI ABB KM support please see the internal ENDENG Confluence space article here:  
[ABB Knowledge Manager Seeq Connector (ABBKM)](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2363392001/ABB+Knowledge+Manager+ABBKM).

Note: This page is in the private Endeavor Engineering space, so you must have access setup for that space to view.

The most updated documentation can be found on that page.  This README is intended to have basics and act as a pointer 
to that page.  

## Seeq Connector SDK

**GitHub Seeq Connector SDK Reference:** [seeq-connector-sdk-java](https://github.com/seeq12/seeq-connector-sdk-java)

This connector source was refactored to be built from the public Seeq Connector SDK repository on GitHub.  The previously 
mentioned Confluence link will have more information on reasons for the refactor and how to manage it.

While this repo is private, the *Seeq Connector SDK* is used as the upstream source for updates and merges to stay 
aligned with Seeq development. 

The Seeq Connector SDK is intended for developers that wish to write a Seeq datasource connector that can be loaded by 
a Seeq agent and facilitate access to data in Seeq.

Seeq connectors can be written in Java or C#, but this repository is for a Java Connector.  Java development can occur 
on Windows, OSX or Ubuntu operating systems.

## Seeq Environment Setup

See: [EEI KB: ABBKM: Development](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2363293709/ABBKM+Development)

### The Build Environment

See: [EEI KB: ABBKM: Development](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2363293709/ABBKM+Development)

As noted in the proceeding KB, we recommend you to install java 21 from
https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html and change your build/build.bat
scripts to set the JAVA_HOME variable to the location where your java is installed.
The Java version of the SDK is built with Gradle. We recommend that you
familiarize yourself with the [basics of Gradle](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html) before proceeding.

### Verifying your Environment

See: [EEI KB: ABBKM: Development](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2363293709/ABBKM+Development)

### Seeq SDK Instructions

Please note that the Seeq SDK repo on GitHub has updated instructions on how to work with it.  This connector is 
developed on top of that SDK so it is a good idea to understand those underlying instructions.  

You can reference the Seeq Connector SDK instructions on their GitHub home page here: [seeq-connector-sdk-java](https://github.com/seeq12/seeq-connector-sdk-java)

Now you're ready to start development!

### Developing your Connector

The Seeq Connector SDK provides instructions for working with the creation of a basic *my_connector* simulated test
connector.  EEI has adapted (and is still adapting) that basic process to build our unique (ABBKM in this case) 
connector in parallel to *my_connector*.  

For more detail on how to use the added EEI development workflow see: 
[EEI KB: ABBKM: Development](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2363293709/ABBKM+Development)

### Building and Deploying your Connector

See: [EEI KB: ABBKM: Build and Deploy](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2556788737/ABBKM+Build+and+Deploy))

# User Guide
See:  [ABB Knowledge Manager Seeq Connector (ABBKM)](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2363392001/ABB+Knowledge+Manager+ABBKM).

# Installation
See: [ABB Knowledge Manager Seeq Connector (ABBKM)](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2363392001/ABB+Knowledge+Manager+ABBKM).

## Source code

Source is in the EEI endeavoreng GitHub Organization.  It is a private repository, and to work with it, you must be granted
access by an EEI administrator.  

**Repository Link:**  [seeq-connector-abbkm-java](https://github.com/endeavoreng/seeq-connector-abbkm-java)

## Versioning

Current version: see above

Semantic versioning is used for this connector.  The format roughly follows the Seeq versioning started with Seeq 
Continuous Delivery (CD).  In general the versioning will closely match that of the Seeq CD release that it was tested 
agains.  The MAJOR.MINOR.PATCH semantic versioning will be organized as follows:
MARJOR = Tested Seeq major version
MINOR = Tested Seeq minor version. 
PATCH = The release date YYYYMMDD. 

**Note:** This release is not using release date as MINOR.  The next one will.  


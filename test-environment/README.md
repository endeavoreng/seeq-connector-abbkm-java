# ABB Knowledge Manager Test Environment Setup

The ABB Knowledge Manager (ABBKM) test environment is enabled using an ABBKM mock
server provided by ABB.  This mock server simulates the ABBKM API and authentication.

ABBKM Mock Server API call returns come from `MockingData` files found in the same
named folder in the test environment.  In this repo the following are key files. 

* `ABBKM_MockPlant_vxx.xx.xx.xxx.zip` - The Mock Server source files provided by ABB. 
* `ExtDataAccessWS.pdf` - Documentation from ABB describing the ABBKM API. 
* `eei-MockingData` - Contains EEI specific mocking data files used in testing.  This is different then the default provided by ABB.

For more detail on the test environment setup and details see the EEI Confluence site
at: [ABBKM: Server and API](https://endeng.atlassian.net/wiki/spaces/ENDENG/pages/2431352847/ABBKM+Server+and+API). 

The tests include unit and integration tests.  

**Note:** The EEI Confluence space is private for EEI and you must have been provided
access.  If you have questions or would like to request access please contact [James Tilett](mailto:jtillett@endeavoreng.com)
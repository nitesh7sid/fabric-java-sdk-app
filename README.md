## Hyperledger Fabric Java SDK v1.0.0

### Installation guide

#### Pre-requisite

* Eclipse IDE (tested with Luna)
* Java 1.8
* Apache Maven (tested with 3.5.2)
* Apache Tomcat (tested with 8.0)

#### Steps
* Import the fabric-java-ask-app project in Eclipse IDE as a maven project
* Maven will download all the dependencies along with the fabric java sdk v1.0.0
* Add appropriate IP addresses to the [network-config](https://github.com/nitesh7sid/fabric-java-sdk-app/blob/master/fabric-java-sdk-app/WebContent/fixture/network-config.json) file, MSP path.
* Make changes to the [config](https://github.com/nitesh7sid/fabric-java-sdk-app/blob/master/fabric-java-sdk-app/WebContent/fixture/config.json) file with Gopath, endorsement policy file path and network-config ath.
* Run the application on tomcat
* Rest server will be started at port 8080 with Url `http://localhost:8080/fabric-java-sdk-app/api` for testing api's.

#### TODO: 
* Documentation on Rest API's
* Rest API's for Install, Instantiate, invoke, query chaincode.

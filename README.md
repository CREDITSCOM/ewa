## EWA
###### Contract Executor and Wallet Applications
This repository contains following main modules:

- Contract Executor

- [Wallet Desktop](https://github.com/CREDITSCOM/ewa/tree/master/wallet-desktop)

- sc-api

- api-client

##### What is Contract Executor?
Contract Executor is app for deploy and execute methods of smart-contract
##### What is Wallet Desktop? 
It is desktop version of credits wallet
##### What is SC-API?
It is API module contains necessary classes for smart-contact deployment
##### What is API-Client?
API contains general tools for integrate with node 


##### Used environment
- [JDK 11](https://openjdk.java.net/projects/jdk/11/)
- [OpenJFX version 11](https://openjfx.io/)
- [Maven version 3.6.1](https://maven.apache.org/docs/3.6.1/release-notes.html) 

##### How to build
For build all modules use following maven command on root project level
```shell
mvn clean install
```
For run wallet you have to install openjfx sdk and then you need specify path to lib folder of openjfx. 
Use following command as example
```shell
java --module-path %your_module_path_here% --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar wallet-desktop.jar`
```

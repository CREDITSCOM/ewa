## EWA
###### Contract Executor and Wallet Applications
This repository contains following main modules:

- Contract Executor
- Wallet Desktop
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
- Java version openJDK 11
- openjfx version 11
- maven version 3.6.1

##### How to build
For build all modules use following maven command on root project level
<br> `mvn clean install`<br>
For run wallet you have to install openjfx sdk and then you need specify path to lib folder of openjfx. 
Use following command as example
<br>
`java --module-path %your_module_path_here% --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar wallet-desktop.jar`
<br>

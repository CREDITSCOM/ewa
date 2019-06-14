<img src="https://raw.githubusercontent.com/CREDITSCOM/Documentation/master/Src/Logo_Credits_horizontal_black.png" align="center">

[Documentation](https://developers.credits.com/en/Articles/Platform) \|
[Guides](https://developers.credits.com/en/Articles/Guides) \|
[News](https://credits.com/en/Home/News)

[![Twitter](https://img.shields.io/twitter/follow/creditscom.svg?label=Follow&style=social)](https://twitter.com/intent/follow?screen_name=creditscom)
[![AGPL License](https://img.shields.io/github/license/CREDITSCOM/ewa.svg?color=green&style=plastic)](LICENSE)
[![Build Status](http://161.156.96.18:8080/buildStatus/icon?job=ewa_build&lastBuild)](http://161.156.96.18:8080/job/ewa_build/lastBuild/)

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
- [JDK 11 or latest](https://jdk.java.net/)
- [OpenJFX SDK version 11.0.3](https://openjfx.io/)
- [Maven version 3.6.1](https://maven.apache.org/docs/3.6.1/release-notes.html) 

##### How to build
For build all modules use following maven command on root project level
```shell
mvn clean install
```
For run wallet you have to install openjfx sdk and then you need specify path to lib folder of openjfx. 
Use following command as example
 "%your_module_path_here%" - path to the lib folder OpenJFX SDK
```shell
java --module-path %your_module_path_here% --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar wallet-desktop.jar`
```


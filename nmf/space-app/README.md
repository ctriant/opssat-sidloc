# OPSSAT SIDLOC - NMF space application

## Installation

### Requirements
- Java 8 (tested with Openjdk version "1.8.0_232")
- Maven 3.X.X (tested with Maven 3.6.3)

### Steps
1. Install the `dev` branch of NanoSatMO Framework (NMF) following [the NMF quick start guide](https://nanosat-mo-framework.readthedocs.io/en/latest/quickstart.html)

2. Get and build the OPSSAT SIDLOC space application
```
$ git clone https://github.com/georgeslabreche/opssat-sidloc.git
$ cd opssat-orbitai/nmf/space-app/ && mvn install
```

3. Deploy the application in the NMF SDK following [the NMF deployment guide](https://nanosat-mo-framework.readthedocs.io/en/latest/apps/packaging.html). When you follow the guide, replace "sobel" by "opsat-sidloc" everywhere and "Sobel" by "OPSSATSIDLOC" in the main class name.

## Starting
1. To start the application follow [the last 3 steps of the NMF SDK guide](https://nanosat-mo-framework.readthedocs.io/en/latest/sdk.html#running-the-cubesat-simulator)

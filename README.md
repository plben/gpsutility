# GPS Utility

A small utility for GPS data logger configuration, log data upload via serial port or Bluetooth. (should be working on all Windows, Linux and MacOS) With the design framework, it is easy to introduce new loggers supported, if the communication protocol is clear.

----

## Pre-requisites

Java 11+

## How to build & run?

>\> gradle clean jlink<br/>
>\> .\build\GPSUtility\bin\gpsutility.bat

## GPS data loggers

- Holux M-241<br/>
The orange one has been verified.<br/>
*New model in white was not tested.*

- Holux GR-245<br/>
Not tested.

- Holux M-1200<br/>
Not tested.

## Data Format

GPX 1.1 compliant<br/>
KML 2.2 compliant (with Google extension)

## Screenshots

<img src="imgs/pic1.png"><br/>

<img src="imgs/pic2.png"><br/>

<img src="imgs/pic3.png"><br/>

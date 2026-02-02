# PingWatch
## Description

This application is build for testing URL response time, You specify Url you need to check and provide parameters for testing , such as
Duration (Amount of time for run test) or Count(Amount of request per url to be sent).

Also, you need to add url manually item by item, or you could import uls from text file (each url must be in new line)

Then you run test by clicking Launch button and wait until test completed. You could see progress on a main screen and each individual url progress.

## How to build binary
1. Clone git repository
2. Navigate into the project folder
3. run command in terminal based on your system
    ### for macOS
    `./gradlew packageDmg`
    
    ### for Windows
    `.\gradlew.bat packageExe`
    
    ### for Linux
   `./gradlew packageDeb`
4. Navigate into build folder `PingWatch/composeApp/build/compose/binaries/main/dmg` and run application PingWatch-$version.dmg
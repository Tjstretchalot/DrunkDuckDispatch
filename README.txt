WARNING: THIS WILL LOAD ALL PNG FILES IN ITS DIRECTORY INTO MEMORY. ONLY RUN THIS PROGRAM INSIDE A DIRECTORY INCLUDING ONLY
ITSELF AND THIS PROGRAMS RESOURCES.

This was created for the Insanity Jam between July 18th 2014 and July 27th 2014

Controls: Mouse. Escape to exit (mostly for fullscreen mode)

Configuration files:
  graphics.json - Will enforce a 1280x1080 aspect ratio, so really you need only a width or a height parameter. Only tested in 640x512 and 1280x1080. Example file: http://pastebin.com/iQdf4Xng. Fullscreen will override both the width and the height and choose the largest resolution with the appropriate aspect ratio.



This was compiled with:

  json-simple version 1.1.1:
    json-simple-1.1.1.jar
  
  log4j 2.0:
    log4j-api-2.0.jar
    log4j-core-2.0.jar
  
  lwjgl version 2.9.1:
    lwjgl_util.jar 
    lwjgl.jar (with Windows natives)

  Slick2D build 237
    slick.jar

In order to run this program, the compiled jar file and the resources/ folder are required (jar file may be substituted
with the appropriate .class files as usual). There is no special packing - resources are placed either in the same directory
as the program or in the resources/ subfolder.

This program will automatically download LWJGL 2.9.1 into either %appdata%/timgames/lwjgl-2.9.1 on windows or "user.home"/.timgames/lwjgl-2.9.1. It will attempt to download from http://umad-barnyard.com/lwjgl-2.9.1.zip. In the event that fails it will ask if the user wants to open up the official lwjgl website in their default browser, and otherwise will smoothly exit. The program will also automatically set up the appropriate natives on the following systems: 

 If the os name contains "win" -> windows natives
 Else If the os name contains "mac" -> macos natives
 Else If the os name contains "nix" -> linux natives
 Else If the os name contains "sunos" -> solaris natives
 Else prompt the user to pick 1 of those 4 natives

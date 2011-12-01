Rekoda
======

A small background video recorder for android which will record video while the
screen is off. Written to test the capability of video to be used as a background
data probe.

Usage
-----

Run the Rekoda app on your phone. A black screen will appear. When the screen
is turned off, video recording will begin. When the screen is turned back on
video recording will stop. The video will be placed on the sdcard under the
Videos folder.

Limitations
-----------

The video that is recorded is not customizable in any way (Although this could
easily be fixed by adding a menu option to customize it). The highest video
quality allowed will be recorded. There is also no limit to the size of the
video that is recorded. You should be careful to leave the Rekoda Activity
before the screen is turned off otherwise video will start to record.

Issues
------

I'm not sure if a partial wakelock is required to prevent the phone from automatically
stopping the video. I wrote code to get a simple wake lock when the activity starts,
but it is currently commented out.
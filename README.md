# Mod-Detection-Tools

[![Supported by Warframe Community Developers](https://warframestat.us/wfcd.png)](https://github.com/WFCD "Supported by Warframe Community Developers")

Tool(s) for tinkering with specific OpenCV settings to help tweak and tune the detection settings for OpenCV and the detection of mods inside an image. 

## Usage (v0.01)
Currently very strict. Screenshot and crop an image of the Mods screen in Warframe similar to the examples in 
```
src/test/resources/images/*
```
Change the sliders and cascade dropdown to tune the settings so it detects all the mods in the image. 

## OpenCV? Image Recognition? 
Yes, as an overkill for mod recognition I've trained an OpenCV Cascade file to recognise any Warframe mod (standard, prime and riven mods). The cascade took over 35 CPU days to create. 

## Important Information
I'm new to JavaFx (been a backend Java dev for a long time) so there may be parts of JavaFX which breaches the acceptable '[wtf](https://s-media-cache-ak0.pinimg.com/236x/ff/68/a5/ff68a5a9d71c3eea9ae4f9e8fed469d1.jpg)' code review scale. 

This is the beginning of a much bigger project which I hope all ties in in the next few months.

I will be using issues to keep track of bugs and features, both created by the community or myself

## ChangeLog

v0.01 - Init commit, very strict usage


# calc-goggles

visualization platform for aplication of integrals

## Overview
This started out as a project for HackRU fall 2017 and won best use of mongodb
Now im working to move the project to mongodb stitch and deploy it for people
to hopefully and finaly be able to use it. This project will alow me to prove to
myself that I can actually "finish" and deploy a project.

I chose mongodb stitch because I was already using mongo, I was planing on using
atlas, and I didn't want to eventually pay for a VPS to run express,or some other
hosting service.

The current setup uses a one page design and intigrates with the important old
calcgogles code through js interop and simply making it accessable throught the
window. because of this method of using libraries the compiler optimizations
have to be set to simple and anyonce can access the library in the window.
once I add all the externs into a file I will be able to use advanced compilation
and some of the hacky things you can do in the console wont be posible.

## Goals
- better styling
- improve the viewer with lighting and axis or xyz grid
- add capability for
  + rotations
  + custom bounds other than the x axis and vertical lines (may require moving away from d3 and functionplot)
  + adding the related problem to the objects
  + adding a picture or showing the graph in the viewer
  + add a way to edit objects

## Setup

To get an interactive development environment run:

    lein figwheel
    
To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2018 Michael J Winters

Distributed under the GNU AGPL 3.0.

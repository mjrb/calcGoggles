<<<<<<< HEAD
# calc-goggles

visualisaion platform for aplication of integrals

## Overview
This started out as a project for HackRU fall 2017 and won best use of mongodb
Now im working to move the project to mongodb stitch and deploy it for people
to hopefully and finaly be able to use it. This project will alow me to prove to
myself that i can actually "finish" and deploy a project.

I chose mongodb stitch because i was already using mongo and I didn't want to
eventually pay for aws or google cloud, or some other hosting service.

The current plan is for the Browse/search/settings page to be a clojurescript
reagent project so the instructions below are just for that. these parts were
the easiest to rewrite and the ones that needed the most rewriting to move
to stitch, so I decided to use clojurescript because clojure is cool.

the create and view pages im planning on leaving in javascript since they dont
really need to be rewritten as much and they're harder to rewrite.

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
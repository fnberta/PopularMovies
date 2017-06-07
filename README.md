# Popular Movies
Simple app that pulls information from TheMovieDB and displays it in a pretty poster image grid. I use this app to experiment with new patterns or libraries.

The current dev branch showcases an example of a cycle.js MVI like setup leveraging kotlin and the new Android Architecture Components. The 'component' packages of every feature contain all the business logic using top level functions. The code in the 'view' and 'viewmodel' packages connect the dots with the Android framework.

TODO: Re-integrate tablet mode, which was implemented in the original version. 

# Usage
To use this code, you need to get an api key from https://www.themoviedb.org/ and include it as 'MovieDbApiKey="your_key"' in the file 'keys.properties' in the root project dir.

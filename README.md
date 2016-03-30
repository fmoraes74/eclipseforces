# eclipseforces
CodeForces Eclipse plugin

# Features

* create a new project for each contest
* generates a JUnit testcase for each problem with given test cases already setup
* attempts to parse any modulo from the problem statement and make it available via MOD constant
* attempts to parse any relative error from the problem statement and uses it during solution checking
* parses timeout from the problem statement and adds it to the JUnit test case
* add additional test cases easily
* generated code template can be edited for usual input functions, etc...
* currently supports Java only. Other languages can be added via plugins

# Installing

An update site will be provided once I have time to set it up. For now, go to the releases and download the plugin JAR file. Place it inside your Eclipse plugins directory and reestart Eclipse.

# Using

Add the EclipseForces views to your perspective. Available ones:

* Contest List: provides a list of contests fetched from the Codeforces website
* Problem List: a list of all problems with contest, date, level and tags/categories
* Problem Statement: shows the problem statement for the currently opened solution code

From the Contest List, select the view menu and select one of the options to get the list of contests available (current ones, current contest list page or full list).

Once the contest list is updated, double-click on the desired contest and a new project will be created and the first problem opened in the editor.

# Bugs

If you find any bugs or issues, feel free to report them via the Github issues for this project.

# Acknowledgement

This plugin would not have been possible without the EclipseCoder plugin by Fredrik Fornwall (http://fornwall.net/eclipsecoder) which was used as the basis for this code.

# License

This plugin is available via the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0)

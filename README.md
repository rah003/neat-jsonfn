Neat JSON FN
=======================

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) for the [Magnolia CMS](http://www.magnolia-cms.com) bringing in templating functions for expressing JCR nodes in JSON format.

Module will register new templating function class that one can then register with the renderer e.g. under ```/modules/rendering/renderers/freemarker/contextAttributes``` as 

```
jsonfn
  componentClass=com.neatresults.mgnltweaks.JsonTemplatingFunctions
  name=jsonfn
```

The above registration is done automatically for freemarker renderer and is meant only as example for other type of renderers one might be using.

Once registered, it is possible to use functions in templates as follows:

you tell functions what node you want to work with:

```jsonfn.with(myNode) ```

or maybe you want to work with all child nodes of given node instead:

```jsonfn.withChildNodesOf(myNode) ```

you can exclude all properties by calling

```.excludeAll() ```

after that, you can explicitly name those you want in:

```.butInclude(“myProp1”, “myProp2”, ...) ```

or you can just exclude some properties instead of all:

```.exclude(“myProp1”, “myProp2”, ...) ```

you can also use regex when excluding props:

```.excludeWithRegex(“my.*”, “some.*more”, ...) ```

and most importantly you can also expand any property you want, by providing name of the expanded property and workspace name in which to look for the target:

```.expand(“categoryId”, “category”) ```

and last but not least, when you built your chain of operations, you execute on them by calling:

```.build() ```


License
-------

Released under the GPLv3, see LICENSE.txt. 

Feel free to use this app, but if you modify the source code please fork us on Github.

Maven dependency
-----------------
```xml
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-jsonfn</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
```

Versions
-----------------
Version 1.0.x should be compatible with all Magnolia 5.x versions, but was tested only on 5.4.3 and not before. If you run into any issues w/ older versions, please report them back.

Latest version can be found at https://nexus.magnolia-cms.com/service/local/repositories/magnolia.forge.releases/content/com/neatresults/mgnltweaks/neat-scripted-select/1.0/neat-scripted-select-1.0.jar

Installation & updates 
-----------------
Upon instalation, module will register templating functions class and expose it under name jsonfn under freemarker renderer. To run, module requires Java 8.

Module has also dependency on neat-tweaks-common jar version 3.0.4-SNAPSHOT or higher.

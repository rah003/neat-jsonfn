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

```jsonfn.from(myNode) ```

or maybe you want to work with all child nodes of given node instead:

```jsonfn.fromChildNodesOf(myNode) ```

or maybe you want to work with all child nodes of given workspace (used by your content app) instead:

```jsonfn.fromChildNodesOf(workspaceName) ```

or to add output into existing json (eg. array. Tip: to start w/ empty array, just pass in "[]"):

```.appendFrom(existingJsonString, node) ```

you can include all properties by calling (nothing is included by default)

```.addAll() ```

you can also explicitly name those you want in:

```.add(“myProp1”, “myProp2”, ...) ```

or just use regex:

```.add(“my.*”, ...) ```

if you have included all, you exclude some properties by:

```.exclude(“myProp1”, “myProp2”, ...) ```

you can also use regex when excluding props:

```.exclude(“my.*”, “some.*more”, ...) ```

and most importantly you can also expand any property you want, by providing name of the expanded property and workspace name in which to look for the target:

```.expand(“categoryId”, “category”) ```

You can select nodes to display their children as array (properties will be skipped):

```.childrenAsArray("@nodeType", "productList")```

This also works with regex for key and value:

```.childrenAsArray("(@name|title)", ".*list")```

you can also tell function to include links for renditions of binaries by calling:

```.binaryLinkRendition("rendition1", "rendition2", ...) ```

and control how many levels down should printing node properties as json go (current level only by default):

```.down(int) ```

and to force output in single line rather than formatted (prettyprinter) json array:

```.inline() ```


and to respecting current locale also in the json produced from passed in node

```.wrapForI18n() ```

and to exclude some node types (by default ```^(?!rep:).*$```) This function is not additive, all allowed types need to be written as one regex!!!

```.allowOnlyNodeTypes(nodeTypeRegex) ```

and to replace some chars in property names that are not always loved by js frameworks (e.g. did you try to order by property w/ colon (:) in name in angularjs? )

```.maskChar(':','_') ```

and last but not least, when you built your chain of operations, you execute on them by calling:

```.print() ```


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
      <version>1.0.4</version>
    </dependency>
```

Versions
-----------------
Version 1.0.x should be compatible with all Magnolia 5.x versions, but was tested only on 5.4.3 and not before. If you run into any issues w/ older versions, please report them back.

Latest version can be found at https://nexus.magnolia-cms.com/service/local/repositories/magnolia.forge.releases/content/com/neatresults/mgnltweaks/neat-jsonfn/1.0.4/neat-jsonfn-1.0.4.jar

Installation & updates 
-----------------
Upon instalation, module will register templating functions class and expose it under name jsonfn under freemarker renderer. To run, module requires Java 8.

Neat-JSONFN uses Jackson to produce json, so you will also need jackson-databind (and it's dependencies jackson-core and jackson-annotations). See https://github.com/FasterXML/jackson-databind for more details and download link. I have used version 2.6.4 (for no particular reason), but higher versions should be fine as well.

Module has also dependency on neat-tweaks-common jar version 2.0.3 or higher.

On Magnolia 5.3.x together with neat-tweaks
- use ```neat-tweaks-developers``` & ```neat-tweaks-editors``` jars of version 1.0.x and ```neat-tweaks-common``` jar of version 2.0.x to make it play nicely with this module. If you use maven, all deps should be resolved correctly automatically. (Tested on 5.3.12)


Changes
-----------------
1.0.4
- fixed errorneous comma added when appending to empty array in ```appendFrom()``` 

1.0.3
- #6 add support for substitution of characters in property names using ```.maskChar(':','_')```
- #4 Allow filtering of included sub nodes by node types regex using ```.allowOnlyNodeTypes("regex")```
- Overloaded ```fromChildNodesOf(String workspaceName)``` method to allow iterating over all content from content app easily
- #5 make sure escapes are preserved where and as appropriate
- when wrapping for i18n, wrap also expanded nodes

1.0.2
- added ```inline()``` function to skip pretty printing json
- added ```appendFrom(existingJsonString, node)``` function to support building custom json arrays
- changed output for collections from objects to real json arrays
- added ```wrapForI18n()``` function to allow outputing json arrays from nodes while respecting current locale

1.0.1
- exclude binary properties to not corrupt produced json

1.0
- functions to print JCR nodes in JSON format.

# Active Markdown 

This is a Maven Plugin for Markdown files. It provides
* *actions* that to generate parts of the markdown file
* *man pages* generation
* error message to dangling references


## Actions

An action has a header

    [//]: (code for this header)
  
and a footer

    [//]: (-)
  
Everything in between is the body of the action. Running the plugin changes the body.

From a markdown perspective, header and footer are comments, the don't show up in the rendered document.

Rationale: actions combine Markdown's direct editability (i.e. no generation step required) with functionality
that needs generation: you can continue to directly edit the markdown file, you don't have to re-run the plugin.
But you can run the plugin, to update generated stuff.

The mechanism for actions is similar to SVN keywords - a part of the document is modified. However, it's more 
verbose then a keyword because comments in Markdown a clumsy.


### Include actions

The action 

    [//]: (include myfile)
    content ..
    of ..
    the ..
    specified ..
    file 
    [//]: (-)
    
replaces it's body with the specified file.

### All Synopsis

The action

    [//]: (ALL_SYNOPSIS)
    [//]: (-)
   
collects all =SYNOPSIS= sections in the document and places the in the body.
    
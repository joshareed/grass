Groovy Authoring for Static Sites (GRASS)
=========================================

A simple script inspired by [Rizzo][1] and [Octopress][2] for generating static sites and blogs with Groovy.

Usage:

	groovy GRASS.groovy -s source -d destination


## Getting Started
The first thing you need for a GRASS powered site is a `site-config.groovy` file. This file contains some of the constants that will be used in the packaged templates and to override default configurations. Below is a minimalist `site-config.groovy` file.
	    
	site {
		title = "Some Guy's Blog"
		subtitle = ""
		author = "Some Guy"
		email = "someguy@gmail.com"
		url = "http://someguyblogs.com"
		twitter = "https://twitter.com/someguy"
		github = "https://github.com/someguy"
	}

As you create your own templates and modify the included ones, feel free to add whatever constants you desire to this file.

### Creating a post 
A blog post in GRASS takes the general form of a Markdown document with a couple of extras. 

	<%
		title 'Introducing GRASS'
		tag 'Technology', 'Groovy', 'GRASS'
	%>
	My text here 
	<!-- more -->
	More text here. Link to [something][1]
	
	[1]: http://example.com

In the sample GRASS post listed above, the nonstandard bits are a code block marked by `<% %>` containing the title and tags and an HTML comment `<!-- more -->`. Everything else is standard Markdown. The code block is used by GRASS to make the title header and by several plugins related to tags: 

* the tags plugin, which generates a listing of all posts, and, 
* the feeds plugin, which generates an Atom feed of all posts with a given tag.

The `<!-- more -->` comment tells the summarizer plugin how much of the post to include in index page and Atom feed summaries. If it is omitted, the full post shows on all pages.

### Blog site structure
The default structure of a blog is as follows(with a few example post files). Posts should be titled `YYYY-DD-MM-Name-with-underscores.md`. This file name is used for the permalink. If the plugin and template directories don't exist, GRASS will use its packaged versions. 

	/my-blog-site
	|-- plugins
	|-- posts
	|   |-- 2014-01-01-now-on-GRASS.md
	|   |-- 2013-08-08-introducing-GRASS.md
	|   |-- 2013-08-13-first-GRASS-plugin.md
	|-- site-config.groovy
	`-- templates
	    `-- <overrided templates here>

## Configuring Your Site
`global-config.groovy` sets all the default paths for a GRASS site. Any of these values can be overriden in `site-config.groovy. The current version of `global-config.groovy` is listed below.

	paths {
		pages = ['pages']
		posts = ['posts']
		assets = ['assets/files', 'assets/css', 'assets/js', 'assets/images']
		plugins = ['plugins']
		templates = ['templates']
	}
	
	plugins {
		enabled = [] // add plugin names to whitelist, if empty all plugins loaded
		disabled = [] // add plugin names to blacklist specific plugins
	}
	
	// plugin-specific config
	plugin {
		links {
			dispatch = ['page', 'url']
		}
	}

## Using Plugins

GRASS looks for plugins in the location specified either in your `global-config.groovy` file or what you overrode it with in your `site-config.groovy` file. GRASS searches for plugins relative to the current directory where GRASS is installed *AND* the source directory of your project. You can however give it multiple locations to search, be it absolute or relative paths.

### Selectively Applying Plugins

As shown in `global-config.groovy`, it is possible to selectively enable or disable plugins. To do so, pass in the class name of the plugin. If I wanted to disable the feeds plugin, I would add the following to my `site-config.groovy` file:

	plugins.disabled = ['FeedPlugin']

## TODO Customizing Templates


[1]: https://github.com/fifthposition/rizzo
[2]: http://octopress.org/

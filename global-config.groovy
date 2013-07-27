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
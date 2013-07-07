import groovy.text.SimpleTemplateEngine

class Page {
	File path
	String template
	String name
	String out
	String title
	Date date
	String summary
	String content
	List tags = []
}

// command line parsing
def cli = new CliBuilder(usage: 'groovy grass -s "source" -d "destination"')
cli.s(longOpt: 'source', args: 1, required: true, 'source')
cli.d(longOpt: 'destination', args: 1, required: true, 'destination')

// parse options
opt = cli.parse(args)
if (!opt) { return }

// load our config
config = loadConfig()
config.source = new File("${opt.s}")
config.destination = new File("${opt.d}")
config.destination.mkdirs()

// load the plugins
plugins = loadPlugins()
trigger('init', config)

// find all pages
pages = loadPages()

// render pages
renderPages()

// render the index
renderIndex()

/* helper methods */
def renderIndex() {
	def index = new Page(content: '', template: 'index', name: config?.site?.name ?: 'Index', title: config?.site?.title, date: new Date(), out: new File(config.destination, 'index.html'))
	def engine = new SimpleTemplateEngine()

	// preprocess index
	trigger('beforeIndex', [config, index, pages])

	def binding = newBinding(index)
	binding.pages = pages

	// evaluate index as groovy template
	index.content = engine.createTemplate(index.content).make(binding.variables)

	// render index
	trigger('renderIndex', [config, index])

	// post process index
	trigger('afterIndex', [config, index])

	// apply index template and write out
	applyTemplate(binding)
}

def renderPages() {
	def engine = new SimpleTemplateEngine()

	pages.each { page ->
		// preprocess page
		trigger('beforePage', [config, page])

		def binding = newBinding(page)

		// evaluate page as groovy template
		page.content = engine.createTemplate(page.content).make(binding.variables)

		// render page
		trigger('renderPage', [config, page])

		// post process page
		trigger('afterPage', [config, page])

		// apply page template and write out
		applyTemplate(binding)
	}
}

def applyTemplate(binding) {
	def page = binding.getVariable('page')
	if (!page.out) { return }
	if (page.template) {
		// find the first template that exists
		def template = expandPaths(config?.paths?.templates ?: []).inject([]) { list, dir ->
			list << new File(dir, page.template)
			list << new File(dir, "${page.template}.html")
			list
		}.find { it.exists() }

		if (template) {
			// apply the template
			page.content = new SimpleTemplateEngine().createTemplate(page.content).make(binding.variables)
		}
	}

	if (page.content) {
		writeFile(page.out, page.content)
	}
}

def newBinding(page) {
	def binding = new Binding(config: config, page: page)
	trigger('setupBinding', [config, binding])
	binding
}

def loadConfig() {
	def config = new ConfigObject()
	def global = new File("global-config.groovy")
	if (global.exists()) {
		config.merge(new ConfigSlurper().parse(global.toURL()))
	}
	def local = new File("${opt.s}/site-config.groovy")
	if (local.exists()) {
		config.merge(new ConfigSlurper().parse(local.toURL()))
	}
	config
}

def loadPlugins() {
	def plugins = []
	def disabled = config?.plugins?.disabled ?: []

	// load the plugin classes
	def classloader = new GroovyClassLoader()
	expandPaths(config?.paths?.plugins ?: []).each { dir ->
		dir.eachFileMatch(~/.*\.groovy/) { file ->
			def clazz = classloader.parseClass(file)
			if (!disabled.contains(clazz.simpleName)) {
				plugins << clazz.newInstance()
			}
		}
	}

	plugins
}

def loadPages() {
	def pages = []

	expandPaths(config?.paths?.pages ?: []).each { dir ->
		dir.eachFile { file ->
			// create our page object
			def name = file.name
			if (name.lastIndexOf('.') > 0) {
				name = name[0..(name.lastIndexOf('.') - 1)]
			}
			def title = name.split('-').collect { it.capitalize() }.join(' ')
			def out = file.parentFile.absolutePath - config.source.absolutePath + "${File.separator}${name}.html"

			pages << new Page(path: file, content: file.text, template: 'page', name: name, title: title, date: new Date(file.lastModified()), out: out)
		}
	}

	pages
}

def trigger(event, args) {
	plugins.each { plugin ->
		if (plugin.respondsTo(event)) {
			if (args instanceof List) {
				plugin."$event"(*args)
			} else {
				plugin."$event"(args)
			}
		}
	}
}

def expandPaths(paths) {
	[paths].flatten().inject([]) { list, path ->
		list << new File(path)
		list << new File(config.source, path)
		list
	}.findAll { it.exists() }
}

def writeFile(path, content) {
	if (path && content) {
		def out = new File(config.destination, path)
		out.parentFile.mkdirs()
		out.write(content)
	}
}
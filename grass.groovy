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
}

// command line parsing
def cli = new CliBuilder(usage: 'groovy grass -s "source" -d "destination"')
cli.s(longOpt: 'source', args: 1, required: true, 'source')
cli.d(longOpt: 'destination', args: 1, required: true, 'destination')

// parse options
opt = cli.parse(args)
if (!opt) { return }

// add our plugin methods
this.metaClass.mixin GrassMixin

// load our config
config = loadConfig()
config.source = new File("${opt.s}")
config.destination = new File("${opt.d}")
config.destination.mkdirs()
config.pages = []
config.plugins = []

// needed for the Mixin to access the config
def getConfig() { config }
def getPlugins() { config.plugins }
def getPages() { config.pages }

// load the plugins
loadPlugins()
trigger('init')

// find all the pages
loadPages()

// render the index
renderIndex()

// write pages
writePages()

// trigger a cleanup event
trigger('cleanup')

/* helper methods */
def renderIndex() {
	def index = new Page(content: '', template: 'index', name: config?.site?.name ?: 'Index', title: config?.site?.title, date: new Date(), out: 'index.html')

	// preprocess index
	trigger('beforeIndex', [index, pages])
	addPage(index)
	trigger('afterIndex', [index, pages])
}

def writePages() {
	pages.each { page ->
		trigger('beforeWrite', page)
		writeFile(page.out, page.content)
		trigger('afterWrite', page)
	}
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
	def enabled = config?.plugins?.enabled ?: []
	def disabled = config?.plugins?.disabled ?: []

	// load the plugin classes
	def classloader = new GroovyClassLoader()
	expandPaths(config?.paths?.plugins ?: []).each { dir ->
		dir.eachFileMatch(~/.*\.groovy/) { file ->
			def clazz = classloader.parseClass(file)
			if ((!enabled || enabled.contains(clazz.simpleName)) && !disabled.contains(clazz.simpleName)) {
				def instance = clazz.newInstance()
				if (instance.hasProperty('config')) {
					instance.config = config
				}
				instance.metaClass.mixin GrassMixin
				plugins << instance
			}
		}
	}
}

def loadPages() {
	expandPaths(config?.paths?.pages ?: []).each { dir ->
		dir.eachFile { file ->
			// create our page object
			def name = file.name
			if (name.lastIndexOf('.') > 0) {
				name = name[0..(name.lastIndexOf('.') - 1)]
			}
			def title = name.split('-').collect { it.capitalize() }.join(' ')
			def out = file.parentFile.absolutePath - config.source.absolutePath + "${File.separator}${name}.html"

			addPage(path: file, content: file.text, template: 'page', name: name, title: title, date: new Date(file.lastModified()), out: out)
		}
	}
}

// added to this script as well as all plugins
class GrassMixin {

	def addPage(Map data) {
		addPage(new Page(data))
	}

	def addPage(Page page) {
		def engine = new SimpleTemplateEngine()
		// preprocess page
		trigger('beforePage', page)

		def binding = newBinding(page: page)

		// evaluate page as groovy template
		page.content = evaluate(page.content, binding)

		// render page
		trigger('renderPage', page)

		// apply page template
		applyTemplate(page, binding)

		// post process page
		trigger('afterPage', page)

		// add the page to the list
		config.pages << page
	}

	def evaluate(template, binding) {
		binding.values().each { v ->
			if (v instanceof Closure) {
				v.delegate = binding
			}
		}
		new SimpleTemplateEngine().createTemplate(template).make(binding).toString()
	}

	def applyTemplate(page, binding) {
		if (!page.out || !page.template) { return }
		page.content = applyTemplate(page.template, page.content, binding)
	}

	def applyTemplate(id, content, binding) {
		def template = findTemplate(id, binding)

		if (template) {
			binding['.'] = template
			binding.content = content
			// apply the template
			evaluate(template.text, binding)
		} else {
			content
		}
	}

	def findTemplate(id, binding = [:]) {
		// check relative to '.' in the binding
		if (!id.startsWith('/') && binding['.'] instanceof File) {
			def relative = binding['.']
			if (relative?.isFile()) {
				relative = relative.parentFile
			}
			def test = new File(relative, id)
			if (test.exists()) {
				return test
			}
			test = new File(relative, "${id}.html")
			if (test.exists()) {
				return test
			}
		}

		// strip leading slash
		def path = id
		if (path.startsWith('/')) {
			path = path[1..-1]
		}

		// check all template roots
		expandPaths(config?.paths?.templates ?: []).inject([]) { list, dir ->
			list << new File(dir, path)
			list << new File(dir, "${path}.html")
			list
		}.find { it.exists() }
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

	def newBinding(Map binding = [:]) {
		binding.config = config
		trigger('setupBinding', [binding])
		binding
	}

	def trigger(event, args = null) {
		config.plugins.each { plugin ->
			if (plugin.respondsTo(event)) {
				if (!args) {
					plugin."$event"()
				} else if (args instanceof List) {
					plugin."$event"(*args)
				} else {
					plugin."$event"(args)
				}
			}
		}
	}

	def fail(msg) {
		System.err.println(msg)
		System.exit(-1)
	}
}
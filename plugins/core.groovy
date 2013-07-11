import java.text.SimpleDateFormat
import groovy.text.SimpleTemplateEngine

class CorePlugin {
	def DATE = new SimpleDateFormat('yyyy-MM-dd')
	def DATETIME = new SimpleDateFormat('yyyy-MM-dd hh:mm')

	def config

	def setupBinding(binding) {
		// add bindings to change metadata
		binding.template = bind(binding, pageProperty.curry('template'))
		binding.title = bind(binding, pageProperty.curry('name'))
		binding.title = bind(binding, pageProperty.curry('title'))
		binding.summary = bind(binding, pageProperty.curry('summary'))
		binding.out = bind(binding, pageProperty.curry('out'))
		binding.date = bind(binding, { date ->
			if (date instanceof Date) {
				page.date = date
			} else {
				def str = date.toString()
				try {
					page.date = DATETIME.parse(str)
				} catch (e) {
					try {
						page.date = DATE.parse(str)
					} catch (e2) {
						// not valid format
					}
				}
			}
		})
		binding.include = bind(binding, { path ->
			def relative = page.path.parentFile
			def file = new File(relative, path)
			if (file.exists()) {
				new SimpleTemplateEngine().createTemplate(file.text).make(binding.variables)
			}
		})
	}

	private Closure pageProperty = { p, v ->
		page."$p" = v
	}

	private bind(delegate, closure) {
		closure.delegate = delegate
		closure
	}
}
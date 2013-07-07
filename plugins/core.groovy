import java.text.SimpleDateFormat
import groovy.text.SimpleTemplateEngine

class CorePlugin {
	def DATE = new SimpleDateFormat('yyyy-MM-dd hh:mm')

	def setupBinding(config, binding) {
		// add bindings to change metadata
		binding.template = bind(binding, pageProperty.curry('template'))
		binding.title = bind(binding, pageProperty.curry('name'))
		binding.title = bind(binding, pageProperty.curry('title'))
		binding.summary = bind(binding, pageProperty.curry('summary'))
		binding.out = bind(binding, pageProperty.curry('out'))
		binding.date = bind(binding, { String date ->
			page.date = DATE.parse(date)
		})
		binding.date = bind(binding, { Date date ->
			page.date = date
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
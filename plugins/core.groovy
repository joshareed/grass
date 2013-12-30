import java.text.SimpleDateFormat
import groovy.text.SimpleTemplateEngine

class CorePlugin {
	def DATE = new SimpleDateFormat('yyyy-MM-dd')
	def DATETIME = new SimpleDateFormat('yyyy-MM-dd hh:mm')

	def config

	def setupBinding(binding) {
		// add bindings to change metadata
		binding.template = pageProperty.curry('template')
		binding.title = pageProperty.curry('name')
		binding.title = pageProperty.curry('title')
		binding.summary = pageProperty.curry('summary')
		binding.out = pageProperty.curry('out')
		binding.date = { date ->
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
		}
		binding.include = { path ->
			def file = findTemplate(path, binding)
			if (file.exists()) {
				evaluate(file.text, binding)
			}
		}
		binding.render = { Map args ->
			def pathKey = ['path', 'template', 'using'].find { args.containsKey(it) }
			if (!pathKey) fail('Template path required')
			def path = args[pathKey]

			def file = findTemplate(path, binding)
			if (file.exists()) {
				def data = [:]
				data.putAll(binding)

				def varKey = ['var', 'as'].find { args[it] }
				def var = varKey ? args[varKey] : 'it'

				args.model.collect { m ->
					data[var] = m
					new SimpleTemplateEngine().createTemplate(file.text).make(data).toString()
				}.join('\n')
			}
		}
		binding.when = { test, out ->
			test ? out : ''
		}
		binding.unless = { test, out ->
			test ? '' : out
		}
		binding.isodate = { date ->
			date.format("yyyy-MM-dd'T'HH:mm:ssXXX")
		}
		binding.prettydate = { date ->
			date.format("MMM dd, yyyy")
		}
	}

	private Closure pageProperty = { p, v ->
		page."$p" = v
	}
}
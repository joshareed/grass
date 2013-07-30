class LinksPlugin {
	def config

	private createLinkToPage(Page target, boolean absolute) {
		createLinkToUrl(target.out, absolute)
	}

	private createLinkToUrl(String url, boolean absolute) {
		if (url.toLowerCase().startsWith('http')) {
			url
		} else {
			def buffer = new StringBuilder()
			if (absolute) {
				buffer.append(config?.site?.url ?: '')
			}
			if (!buffer.toString().endsWith('/') && !url.startsWith('/')) {
				buffer.append('/')
			}
			buffer.append(url)
			buffer
		}
	}

	def setupBinding(binding) {
		binding.createLinkToPage = this.&createLinkToPage
		binding.createLinkToUrl = this.&createLinkToUrl

		// dynamic dispatch based on the args to createLink()
		binding.createLink = { target ->
			if (target instanceof Page) {
				binding.createLinkToPage(target, false)
			} else if (target instanceof Map) {
				def type = (config?.plugin?.links?.dispatch ?: []).find { target[it] }
				if (!type) { fail("Don't know how to create a link for: ${target}") }
				def absolute = target.containsKey('absolute') ? target.absolute : false
				if (type) {
					binding."createLinkTo${type.capitalize()}"(target[type], absolute)
				}
			} else {
				binding.createLinkToUrl(target.toString(), false)
			}
		}
	}
}
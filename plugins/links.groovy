class LinksPlugin {
	def config

	private createLinkToPage(Page target) {
		createLinkToUrl(target.out)
	}

	private createLinkToUrl(String url) {
		if (url.toLowerCase().startsWith('http')) {
			url
		} else {
			"${config?.site?.url ?: ''}${!config?.site?.url?.endsWith('/') && !url.startsWith('/') ? '/' : ''}${url}"
		}
	}

	def setupBinding(binding) {
		binding.createLinkToPage = this.&createLinkToPage
		binding.createLinkToUrl = this.&createLinkToUrl

		// dynamic dispatch based on the args to createLink()
		binding.createLink = { target ->
			if (target instanceof Page) {
				binding.createLinkToPage(target)
			} else if (target instanceof Map) {
				def type = (config?.plugin?.links?.dispatch ?: []).find { target[it] }
				if (type) {
					binding."createLinkTo${type.capitalize()}"(target[type])
				}
			} else {
				binding.createLinkToUrl(target.toString())
			}
		}
	}
}
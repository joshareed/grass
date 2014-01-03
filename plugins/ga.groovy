class GoogleAnalyticsPlugin {
	def GA_TAG = /<ga:tracker\/>/

	def config

	def beforeWrite(page) {
		if (page.content.contains(GA_TAG)) {
			def id = config?.plugin?.ga?.id
			if (id) {
				page.content = page.content.replace(GA_TAG, applyTemplate('ga/tracker', '', newBinding(ga: [id: id])).toString())
			} else {
				page.content = page.content.replace(GA_TAG, '')
			}
		}
	}
}
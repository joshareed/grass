class SummarizerPlugin {
	private static final String DIVIDER = '<!-- more -->'

	def afterPage(page) {
		if (page?.summary?.contains(DIVIDER)) {
			page.summary = page.summary.substring(0, page.summary.indexOf(DIVIDER))
		}
	}
}
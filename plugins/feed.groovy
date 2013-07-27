class FeedPlugin {
	def LINKS_TAG = /<feed:links\/>/

	def config

	def afterIndex(index, pages) {
		def posts = pages.findAll { it.post }.sort { a, b -> b.date <=> a.date }
		def content = applyTemplate('feed/feed.xml', '', newBinding(posts: posts, site: config.site))
		addPage(content: content, name: 'Atom Feed', title: 'Atom Feed', date: new Date(), out: 'feed.xml')
	}

	def beforeWrite(page) {
		if (page.content.contains(LINKS_TAG)) {
			page.content = page.content.replace(LINKS_TAG, applyTemplate('feed/links', '', newBinding([:])).toString())
		}
	}

}
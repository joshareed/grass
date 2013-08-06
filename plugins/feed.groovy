class FeedPlugin {
	def LINKS_TAG = /<feed:links\/>/

	def config
	def feeds = [:]

	def afterIndex(index, pages) {
		def posts = pages.findAll { it.post }.sort { a, b -> b.date <=> a.date }
		def content = applyTemplate('feed/atom.xml', '', newBinding(posts: posts, site: config.site))
		addPage(content: content, name: 'Atom Feed', title: 'Atom Feed', date: new Date(), out: 'atom.xml')

		if (config.tags) {
			generateTagFeeds()
		}
	}

	private generateTagFeeds() {
		config.tags.each { k, v ->
			def tag = normalize(k)
			def posts = v.sort { a, b -> b.date <=> a.date }
			def content = applyTemplate('feed/atom.xml', '', newBinding(posts: posts, site: config.site))
			addPage(content: content, name: "${k} Feed", title: "${k} Feed", date: new Date(), out: "feeds/${tag}.xml")
		}
	}

	def beforeWrite(page) {
		if (page.content.contains(LINKS_TAG)) {
			page.content = page.content.replace(LINKS_TAG, applyTemplate('feed/links', '', newBinding([:])).toString())
		}
	}

	private normalize(tag) {
		URLEncoder.encode(tag.toLowerCase().replace(' ', '-'), "UTF-8")
	}
}
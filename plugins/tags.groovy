import java.net.URLEncoder

class TagsPlugin {
	def PAGE_TAG = /<tags:page\/>/

	def config
	def tags = [:]

	def init() {
		if (config?.plugin?.links?.dispatch) {
			config?.plugin?.links?.dispatch << 'tag'
		}
	}


	def setupBinding(binding) {
		binding.tag = { Object[] args ->
			(args as List).flatten().each { tag(page, it) }
		}
		binding.createLinkToTag = { String tag, boolean absolute ->
			binding.createLinkToUrl("tags/${normalize(tag)}.html", absolute)
		}
	}

	def beforePage(page) {
		page.metaClass.tags = []
	}

	def afterIndex(index, pages) {
		tags.each { k, v ->
			def posts = v.sort { a, b -> b.date <=> a.date }
			def content = applyTemplate('tags/index.html', '', newBinding(pages: posts))
			addPage(content: content, name: "Category: $k", title: "Category: $k", date: new Date(), out: "tags/${normalize(k)}.html")
		}
	}

	def beforeWrite(page) {
		if (page.content.contains(PAGE_TAG)) {
			if (page?.tags) {
				page.content = page.content.replace(PAGE_TAG, applyTemplate('tags/page.html', '', newBinding(tags: page.tags)).toString())
			} else {
				page.content = page.content.replace(PAGE_TAG, '')
			}
		}
	}

	private normalize(tag) {
		URLEncoder.encode(tag.toLowerCase().replace(' ', '-'), "UTF-8")
	}

	private tag(page, name) {
		page.tags << name
		if (tags.containsKey(name)) {
			tags[name] << page
		} else {
			tags[name] = [page]
		}
	}
}
class MenuPlugin {
	def PAGES_TAG = /<menu:pages\/>/

	def config

	def menu = [:]

	def setupBinding(binding) {
		binding.menu = { Object[] args ->
			if (args) {
				menu[page.title] = page
			} else {
				menu[args[0]] = page
			}
		}
	}

	def afterIndex(index, pages) {
		pages.each { page ->
			if (page.content.contains(PAGES_TAG)) {
				page.content = page.content.replace(PAGES_TAG, applyTemplate('menu/pages', '', newBinding(pages: menu)).toString())
			}
		}
	}
}
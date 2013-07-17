import java.text.SimpleDateFormat

class BlogPlugin {
	def DATE = new SimpleDateFormat('yyyy-MM-dd')
	def LIST_TAG = /<blog:list\/>/
	def RECENT_TAG = /<blog:recent\/>/

	def paths
	def config
	def posts

	def init() {
		// add 'paths' to the page search path
		paths = config?.paths?.posts ?: []
		if (paths && config?.paths?.pages) {
			config.paths.pages.addAll(paths)
		}
	}

	def beforePage(page) {
		decoratePage(page)

		if (page.post) {
			// set the template to post
			page.template = 'post'

			// check for date in filename
			if (page.name =~ /^(19|20)\d\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])/) {
				page.date = DATE.parse(page.name[0..10])
				page.name = page.name.substring(11)
				page.title = page.title.substring(11)
			}
		}
	}

	def afterPage(page) {
		if (page.post) {
			// rewrite output filename using date
			page.out = "${page.date.format('/yyyy/MM/dd')}/${page.name}.html"
		}
	}

	def beforeIndex(index, pages) {
		// if the index has no content, insert the blog summary tag
		if (!index.content) {
			index.content = LIST_TAG
		}
	}

	def afterIndex(index, pages) {
		posts = pages.findAll { it.post }.sort { a, b -> b.date <=> a.date }
	}

	def beforeWrite(page) {
		if (page.content.contains(LIST_TAG)) {
			populatePostList(page, posts)
		}
		if (page.content.contains(RECENT_TAG)) {
			populateRecentList(page, posts)
		}
	}

	private populatePostList(page, posts) {
		page.content = page.content.replace(LIST_TAG, applyTemplate('blog/list', '', newBinding(posts: posts)).toString())
	}

	private populateRecentList(page, posts) {
		def recent = posts.take(config?.blog?.recent ?: 5)
		page.content = page.content.replace(RECENT_TAG, applyTemplate('blog/recent', '', newBinding(posts: recent)).toString())
	}

	private decoratePage(page) {
		// HACK: removes the need for other blog-ish plugins to implement isPost(page) themselves
		def isPost = paths.find { page.out.startsWith("/${it}") } != null
		if (isPost) {
			page.metaClass.isPost = { -> true }
		} else {
			page.metaClass.isPost = { -> false }
		}
	}
}
import java.text.SimpleDateFormat

class BlogPlugin {
	def DATE = new SimpleDateFormat('yyyy-MM-dd')
	def posts
	def config

	def init() {
		// add 'posts' to the page search path
		posts = config?.paths?.posts ?: []
		if (posts && config?.paths?.pages) {
			config.paths.pages.addAll(posts)
		}
	}

	def beforePage(page) {
		if (isPost(page)) {
			// check for date in filename
			if (page.name =~ /^(19|20)\d\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])/) {
				page.date = DATE.parse(page.name[0..10])
				page.name = page.name.substring(11)
				page.title = page.title.substring(11)
			}
		}
	}

	def afterPage(page) {
		if (isPost(page)) {
			// rewrite output filename using date
			page.out = "${page.date.format('/yyyy/MM/dd')}/${page.name}.html"
		}
	}

	private boolean isPost(page) {
		posts.find { page.out.startsWith("/${it}") } != null
	}
}
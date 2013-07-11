import com.petebevin.markdown.MarkdownProcessor

@Grab('com.madgag:markdownj-core:0.4.1')
class MarkdownPlugin {

	def renderPage(page) {
		if (page?.path?.name?.endsWith('.md') || page?.path?.name?.endsWith('.markdown')) {
			def processor = new MarkdownProcessor()
			page.content = processor.markdown(page.content)
		}
	}
}
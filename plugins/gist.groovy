class GistPlugin {
	def config

	def setupBinding(binding) {
		binding.gist = { user, id ->
			"""<script src="https://gist.github.com/${user}/${id}.js"></script>"""
		}
	}
}
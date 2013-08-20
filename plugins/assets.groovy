class AssetsPlugin {
	def config

	def init() {
		if (config?.plugin?.links?.dispatch) {
			config?.plugin?.links?.dispatch << 'asset'
		}
	}

	def setupBinding(binding) {
		binding.createLinkToAsset = { String name, boolean absolute ->
			// check if the asset exists
			def asset = config?.paths?.assets?.find { path ->
				def global = new File(path)
				if (global.exists() && new File(global, name).exists()) {
					return path
				}

				def local = new File(config.source, path)
				if (local.exists() && new File(local, name)) {
					return path
				}

				return null
			}
			binding.createLinkToUrl(asset ? "/${asset}/${name}" : "${name}", absolute)
		}
	}

	def afterIndex(index, pages) {
		// walk through our paths and copy over assets
		config?.paths?.assets?.each { path ->
			def global = new File(path)
			if (global.exists()) {
				copy(global, new File(config.destination, path))
			}

			def local = new File(config.source, path)
			if (local.exists()) {
				copy(local, new File(config.destination, path))
			}
		}
	}

	private copy(from, to) {
		def ant = new AntBuilder()
		if (from.directory) {
			ant.copy(todir: to.absolutePath) {
				fileset(dir: from.absolutePath)
			}
		} else {
			ant.copy(todir: config.destination.absolutePath) {
				fileset(file: from.absolutePath)
			}
		}
	}
}
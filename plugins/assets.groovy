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
		def ant = new AntBuilder()

		// walk through our paths and copy over assets
		config?.paths?.assets?.each { path ->
			def global = new File(path)
			if (global.exists()) {
				ant.copy(todir: new File(config.destination, path).absolutePath) {
					global.directory ? fileset(dir: global.absolutePath) : fileset(file: global.absolutePath)
				}
			}

			def local = new File(config.source, path)
			if (local.exists()) {
				ant.copy(todir: new File(config.destination, path).absolutePath) {
					local.directory ? fileset(dir: local.absolutePath) : fileset(file: local.absolutePath)
				}
			}
		}
	}
}
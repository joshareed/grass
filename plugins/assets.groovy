class AssetsPlugin {
	def config

	def afterIndex(index) {
		def ant = new AntBuilder()

		// walk through our paths and copy over assets
		config?.paths?.assets?.each { path ->
			def global = new File(path)
			if (global.exists()) {
				ant.copy(todir: new File(config.destination, path).absolutePath) {
					fileset(dir: global.absolutePath)
				}
			}

			def local = new File(config.source, path)
			if (local.exists()) {
				ant.copy(todir: new File(config.destination, path).absolutePath) {
					fileset(dir: local.absolutePath)
				}
			}
		}
	}
}
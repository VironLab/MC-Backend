package eu.vironlab.mc.feature


class DefaultFeatureRegistry : FeatureRegistry {

    val features: MutableMap<Class<*>, Any> = mutableMapOf()

    override fun <T> getFeature(featureClass: Class<T>): T {
        return (this.features[featureClass] ?: throw IllegalStateException("Feature not Registered")) as T
    }

    override fun <T, E : T> registerFeature(featureClass: Class<T>, impl: Any): Any {
        this.features[featureClass] = impl!!
        return impl
    }

}

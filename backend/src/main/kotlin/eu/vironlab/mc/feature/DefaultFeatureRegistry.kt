package eu.vironlab.mc.feature


class DefaultFeatureRegistry : FeatureRegistry {

    val features: MutableMap<Class<*>, Any> = mutableMapOf()

    override fun <T> getFeature(featureClass: Class<T>): T? {
        return this.features[featureClass] as T?
    }

    override fun <T, E : T> registerFeature(featureClass: Class<T>, impl: E): E {
        this.features[featureClass] = impl!!
        return impl
    }

}

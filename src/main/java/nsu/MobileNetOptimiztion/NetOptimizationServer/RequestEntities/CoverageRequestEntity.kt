package nsu.MobileNetOptimiztion.NetOptimizationServer.RequestEntities

data class CoverageRequestEntity(
    val lat00: Double,
    val lon00: Double,
    val lat11: Double,
    val lon11: Double,
    val connectionType: String,
    val step: Int
)
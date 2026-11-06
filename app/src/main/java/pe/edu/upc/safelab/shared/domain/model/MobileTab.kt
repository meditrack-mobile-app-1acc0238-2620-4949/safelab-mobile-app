package pe.edu.upc.safelab.shared.domain.model

import androidx.annotation.DrawableRes
import pe.edu.upc.safelab.R

// Only frequently used destinations appear in the bottom bar.
enum class MobileTab(
    val labelEn: String,
    val labelEs: String,
    @DrawableRes val icon: Int
) {
    HOME("Home", "Inicio", R.drawable.ic_nav_home),
    SENSORS("Sensors", "Sensores", R.drawable.ic_nav_sensors),
    ALERTS("Alerts", "Alertas", R.drawable.ic_nav_alerts),
    EQUIPMENT("Equipment", "Equipos", R.drawable.ic_nav_equipment),
    MORE("More", "Más", R.drawable.ic_nav_more);

    fun label(language: String) = if (language == "ES") labelEs else labelEn
}
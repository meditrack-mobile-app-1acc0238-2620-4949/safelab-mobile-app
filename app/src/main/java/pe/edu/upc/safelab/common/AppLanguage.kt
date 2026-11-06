package pe.edu.upc.safelab.common

import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.common.LocalAppLanguage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalAppLanguage = compositionLocalOf { "EN" }

@Composable
fun tr(en: String, es: String): String = if (LocalAppLanguage.current == "ES") es else en

fun tr(language: String, en: String, es: String): String = if (language == "ES") es else en

fun firstName(value: String): String = value.trim().split(Regex("\\s+")).firstOrNull().orEmpty()

fun secondNameInitial(value: String): String {
    val parts = value.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return parts.getOrNull(1)?.firstOrNull()?.uppercase() ?: ""
}

fun firstSurname(value: String): String = value.trim().split(Regex("\\s+")).firstOrNull().orEmpty()

fun secondSurnameInitial(value: String): String {
    val parts = value.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return parts.getOrNull(1)?.firstOrNull()?.uppercase() ?: ""
}

fun buildDisplayName(firstNames: String, lastNames: String): String {
    val name = firstName(firstNames)
    val secondName = secondNameInitial(firstNames)
    val surname = firstSurname(lastNames)
    val secondSurname = secondSurnameInitial(lastNames)
    val middle = if (secondName.isNotBlank()) " $secondName." else ""
    val end = if (secondSurname.isNotBlank()) " $secondSurname." else ""
    return listOf(name, middle, surname, end).joinToString(" ").replace("  ", " ").trim()
}


fun buildAvatarInitials(firstNames: String): String {
    val parts = firstNames.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val first = parts.getOrNull(0)?.firstOrNull()?.uppercase() ?: "S"
    val second = parts.getOrNull(1)?.firstOrNull()?.uppercase() ?: parts.getOrNull(0)?.getOrNull(1)?.uppercase() ?: "L"
    return "$first$second"
}
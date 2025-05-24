package com.example.vitaescan

data class DatosCV(
    val documento: String = "",
    val nombre: String = "",
    val carrera: String,
    val nivelEducacion: String,
    val experiencia: String // "Sí" o "No"

)

fun extraerDatosCV(texto: String): DatosCV {
    val textoNormalizado = texto.lowercase().replace("\n", " ")

    val carreraRegex = Regex(
        "(ingeniería\\s+\\w+|medicina|psicología|derecho|administración|marketing|informática|sistemas|diseño\\s+gráfico|contabilidad|arquitectura|bioquímica|finanzas|economía|pedagogía|educación|biología|farmacia|trabajo\\s+social|ingeniería\\s+industrial|tecnología\\s+de\\s+la\\s+información|ingeniería\\s+en\\s+computación|ciencias\\s+de\\s+la\\s+computación|matemáticas|física|química|ingeniería\\s+eléctrica|ingeniería\\s+mecánica|derecho\\s+empresarial|ingeniería\\s+civil|odontología|enfermería|ingeniería\\s+química|sociología|derecho\\s+penal|historia|tecnología\\s+alimentaria|bioinformática|ingeniería\\s+en\\s+telecomunicaciones|ciencias\\s+ambientales|fisioterapia|tecnología\\s+de\\s+alimentos|ciencias\\s+de\\s+la\\s+salud|ciencias\\s+de\\s+la\\s+educación)",
        RegexOption.IGNORE_CASE
    )
    val carrera = carreraRegex.find(textoNormalizado)?.value?.replaceFirstChar { it.uppercase() } ?: "No detectada"

    val educacionRegex = Regex("(doctorado|maestría|specialization|degree|pregrado|técnico|bachelor|master|licenciatura|technologist)", RegexOption.IGNORE_CASE)
    val nivelEducacion = educacionRegex.find(textoNormalizado)?.value?.replaceFirstChar { it.uppercase() } ?: "No detectado"

    val experienciaRegex = Regex("(?i)(experience|worked|job|position|company|labor|trabajo|puesto|empresa)")
    val experiencia = if (experienciaRegex.containsMatchIn(textoNormalizado)) "Sí" else "No"

    return DatosCV(carrera = carrera, nivelEducacion = nivelEducacion, experiencia = experiencia)
}

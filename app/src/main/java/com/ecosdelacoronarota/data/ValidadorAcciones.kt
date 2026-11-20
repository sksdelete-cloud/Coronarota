package com.ecosdelacoronarota.data

import java.text.Normalizer

/**
 * Motor determinista de validación de acciones del Panel del GM.
 * Impide estrictamente que los jugadores inventen objetos, lugares, NPCs o situaciones
 * fuera del [CatalogoMundo] vigente.
 */
class ValidadorAcciones {

    companion object {
        const val MENSAJE_INVENTADO =
            "El GM no ha descrito eso. No puedes inventar objetos, lugares ni personajes."
    }

    /**
     * Valida la acción descrita por el jugador [jugador] dentro de su [habitacion] actual
     * usando el [catalogo] del mundo.
     */
    fun validar(
        accion: String,
        jugador: Jugador,
        habitacion: Habitacion,
        catalogo: CatalogoMundo
    ): ResultadoValidacion {
        val textoLimpio = accion.trim()
        if (textoLimpio.isEmpty()) {
            return ResultadoValidacion(
                exitoso = false,
                razon = "Escribe una acción antes de intentar validarla."
            )
        }

        if (jugador.estado == EstadoJugador.MUERTO) {
            return ResultadoValidacion(
                exitoso = false,
                razon = "${jugador.nombre} está MUERTO y no puede realizar acciones."
            )
        }

        if (jugador.estado == EstadoJugador.INCONSCIENTE) {
            return ResultadoValidacion(
                exitoso = false,
                razon = "${jugador.nombre} está INCONSCIENTE y necesita ser estabilizado antes de actuar."
            )
        }

        val norm = normalizar(textoLimpio)
        val comandoEstructurado = parsearComandoEstructurado(norm)
        val tipoDetectado = comandoEstructurado?.first ?: detectarTipoAccion(norm)
        val objetivoEstructurado = comandoEstructurado?.second

        // 1. Verificar primero si el jugador o el traductor menciona algo fuera del catálogo
        val entidadInventada = detectarEntidadNoCatalogada(
            norm = norm,
            objetivoDirecto = objetivoEstructurado,
            tipo = tipoDetectado,
            jugador = jugador,
            habitacion = habitacion,
            catalogo = catalogo
        )
        if (entidadInventada != null) {
            return ResultadoValidacion(
                exitoso = false,
                razon = MENSAJE_INVENTADO,
                tipoDetectado = tipoDetectado
            )
        }

        // 2. Evaluar reglas específicas según el tipo de acción
        return when (tipoDetectado) {
            TipoAccion.ESPERAR -> ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} mantiene la guardia y espera en ${habitacion.nombre}.",
                efectosMecanicos = emptyMap(),
                tipoDetectado = TipoAccion.ESPERAR
            )

            TipoAccion.MOVER -> validarMovimiento(norm, jugador, habitacion, catalogo)

            TipoAccion.EJERCITARSE -> validarEjercicio(jugador, habitacion, catalogo)

            TipoAccion.HABLAR -> validarDialogo(norm, jugador, habitacion, catalogo)

            TipoAccion.LEER -> validarLectura(norm, jugador, habitacion)

            TipoAccion.USAR_OBJETO -> validarUsoObjeto(norm, jugador, habitacion)

            TipoAccion.ATACAR -> validarAtaque(norm, jugador, habitacion, catalogo)

            TipoAccion.EXAMINAR -> validarExamen(norm, jugador, habitacion)
        }
    }

    private fun validarMovimiento(
        norm: String,
        jugador: Jugador,
        habitacion: Habitacion,
        catalogo: CatalogoMundo
    ): ResultadoValidacion {
        val salidaCoincidente = habitacion.salidas.firstOrNull { salida ->
            norm.contains(normalizar(salida))
        }
        if (salidaCoincidente != null) {
            return ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} se desplaza desde ${habitacion.nombre} hacia $salidaCoincidente.",
                efectosMecanicos = emptyMap(),
                tipoDetectado = TipoAccion.MOVER,
                habitacionDestino = salidaCoincidente
            )
        }

        val otraHabitacion = catalogo.habitaciones.firstOrNull { hab ->
            hab.nombre != habitacion.nombre && norm.contains(normalizar(hab.nombre))
        }
        if (otraHabitacion != null) {
            return ResultadoValidacion(
                exitoso = false,
                razon = "No hay salida directa desde ${habitacion.nombre} hacia ${otraHabitacion.nombre}. Salidas disponibles: ${habitacion.salidas.joinToString(", ")}.",
                tipoDetectado = TipoAccion.MOVER
            )
        }

        return ResultadoValidacion(
            exitoso = false,
            razon = MENSAJE_INVENTADO,
            tipoDetectado = TipoAccion.MOVER
        )
    }

    private fun validarEjercicio(
        jugador: Jugador,
        habitacion: Habitacion,
        catalogo: CatalogoMundo
    ): ResultadoValidacion {
        val permitida = catalogo.habitacionesEjercicio.any {
            normalizar(it) == normalizar(habitacion.nombre)
        }
        return if (permitida) {
            ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} entrena su resistencia en ${habitacion.nombre} (+1 HP).",
                efectosMecanicos = mapOf("HP" to 1),
                tipoDetectado = TipoAccion.EJERCITARSE
            )
        } else {
            ResultadoValidacion(
                exitoso = false,
                razon = "No puedes ejercitarte en ${habitacion.nombre}. Solo está permitido en: ${catalogo.habitacionesEjercicio.joinToString(" o ")}.",
                tipoDetectado = TipoAccion.EJERCITARSE
            )
        }
    }

    private fun validarDialogo(
        norm: String,
        jugador: Jugador,
        habitacion: Habitacion,
        catalogo: CatalogoMundo
    ): ResultadoValidacion {
        val jugadorPresente = habitacion.jugadoresPresentes.firstOrNull { otro ->
            normalizar(otro) != normalizar(jugador.nombre) && norm.contains(normalizar(otro))
        }
        if (jugadorPresente != null) {
            return ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} conversa con $jugadorPresente en ${habitacion.nombre}.",
                tipoDetectado = TipoAccion.HABLAR
            )
        }

        val npcEnSala = catalogo.npcsConocidos.firstOrNull { npc ->
            norm.contains(normalizar(npc)) && (
                habitacion.objetosVisibles.any { normalizar(it).contains(normalizar(npc)) || normalizar(npc).contains(normalizar(it)) } ||
                    (normalizar(habitacion.nombre) == "sotano" && (normalizar(npc) == "nicel" || normalizar(npc).contains("masa"))) ||
                    normalizar(npc) == "gm_1"
                )
        }
        if (npcEnSala != null) {
            return ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} se dirige a $npcEnSala en ${habitacion.nombre}.",
                tipoDetectado = TipoAccion.HABLAR
            )
        }

        return ResultadoValidacion(
            exitoso = false,
            razon = "No hay ningún jugador o NPC compatible presente en ${habitacion.nombre} para hablar.",
            tipoDetectado = TipoAccion.HABLAR
        )
    }

    private fun validarLectura(
        norm: String,
        jugador: Jugador,
        habitacion: Habitacion
    ): ResultadoValidacion {
        val librosConocidos = listOf(
            "magia i", "magia ii", "magia v", "invocacion i", "enfermeria i",
            "criaturas magicas i", "tomo de criaturas i", "tomo de magia v",
            "libros", "manual de cocina", "revista velvet & stars", "diario"
        )
        val libroEnSala = habitacion.objetosVisibles.firstOrNull { obj ->
            val objNorm = normalizar(obj)
            (objNorm.contains("libro") || objNorm.contains("manual") || objNorm.contains("tomo")) &&
                (norm.contains(objNorm) || librosConocidos.any { norm.contains(it) })
        }
        val libroEnInventario = jugador.inventario.firstOrNull { item ->
            val itemNorm = normalizar(item)
            (itemNorm.contains("tomo") || itemNorm.contains("diario") || itemNorm.contains("revista") || itemNorm.contains("magia") || itemNorm.contains("criaturas")) &&
                norm.contains(itemNorm)
        }

        val fuente = libroEnSala ?: libroEnInventario
        return if (fuente != null) {
            ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} lee $fuente en ${habitacion.nombre} (+34 EXP).",
                efectosMecanicos = mapOf("EXP" to 34),
                tipoDetectado = TipoAccion.LEER
            )
        } else {
            ResultadoValidacion(
                exitoso = false,
                razon = "No hay ningún libro o tomo disponible en ${habitacion.nombre} ni en tu inventario que coincida con la lectura.",
                tipoDetectado = TipoAccion.LEER
            )
        }
    }

    private fun validarUsoObjeto(
        norm: String,
        jugador: Jugador,
        habitacion: Habitacion
    ): ResultadoValidacion {
        val enInventario = jugador.inventario.firstOrNull { norm.contains(normalizar(it)) }
        val enSala = habitacion.objetosVisibles.firstOrNull {
            normalizar(it) == "botiquin" && norm.contains("botiquin")
        }

        val objeto = enInventario ?: enSala
        if (objeto == null) {
            return ResultadoValidacion(
                exitoso = false,
                razon = "Solo puedes usar objetos que estén en tu inventario actual (${jugador.inventario.joinToString(", ")}).",
                tipoDetectado = TipoAccion.USAR_OBJETO
            )
        }

        val efectos = if (normalizar(objeto) == "botiquin") mapOf("HP" to 5) else emptyMap()
        return ResultadoValidacion(
            exitoso = true,
            razon = "${jugador.nombre} utiliza $objeto en ${habitacion.nombre}.",
            efectosMecanicos = efectos,
            tipoDetectado = TipoAccion.USAR_OBJETO
        )
    }

    private fun validarAtaque(
        norm: String,
        jugador: Jugador,
        habitacion: Habitacion,
        catalogo: CatalogoMundo
    ): ResultadoValidacion {
        val objetivoJugador = habitacion.jugadoresPresentes.firstOrNull {
            normalizar(it) != normalizar(jugador.nombre) && norm.contains(normalizar(it))
        }
        val objetivoNpc = catalogo.npcsConocidos.firstOrNull { npc ->
            norm.contains(normalizar(npc)) && (
                habitacion.objetosVisibles.any { normalizar(it).contains(normalizar(npc)) || normalizar(npc).contains(normalizar(it)) } ||
                    normalizar(habitacion.nombre) == "sotano"
                )
        }

        val objetivo = objetivoJugador ?: objetivoNpc
        return if (objetivo != null) {
            ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} inicia una acción ofensiva contra $objetivo en ${habitacion.nombre}.",
                efectosMecanicos = mapOf("HP" to -2),
                tipoDetectado = TipoAccion.ATACAR
            )
        } else {
            ResultadoValidacion(
                exitoso = false,
                razon = "No hay un objetivo válido presente en ${habitacion.nombre} para atacar.",
                tipoDetectado = TipoAccion.ATACAR
            )
        }
    }

    private fun validarExamen(
        norm: String,
        jugador: Jugador,
        habitacion: Habitacion
    ): ResultadoValidacion {
        val objetoVisible = habitacion.objetosVisibles.firstOrNull { norm.contains(normalizar(it)) }
        val objetoInventario = jugador.inventario.firstOrNull { norm.contains(normalizar(it)) }
        val objetivo = objetoVisible ?: objetoInventario
        return if (objetivo != null) {
            ResultadoValidacion(
                exitoso = true,
                razon = "${jugador.nombre} examina detenidamente $objetivo en ${habitacion.nombre}.",
                tipoDetectado = TipoAccion.EXAMINAR
            )
        } else {
            ResultadoValidacion(
                exitoso = false,
                razon = "Ese objeto no se encuentra visible en ${habitacion.nombre}. Objetos visibles: ${habitacion.objetosVisibles.joinToString(", ")}.",
                tipoDetectado = TipoAccion.EXAMINAR
            )
        }
    }

    /**
     * Interpreta el formato "ACCIÓN: Objeto/Destino" generado por el traductor IA en Fase 2.
     */
    private fun parsearComandoEstructurado(norm: String): Pair<TipoAccion, String>? {
        val sinComillas = norm.trim().trim('"', '\'', '`')
        val match = Regex("""^(mover|hablar|leer|ejercitarse|usar_objeto|usar objeto|usar|atacar|examinar|esperar)\s*:\s*(.*)$""").find(sinComillas)
            ?: return null
        val tipo = when (match.groupValues[1]) {
            "mover" -> TipoAccion.MOVER
            "hablar" -> TipoAccion.HABLAR
            "leer" -> TipoAccion.LEER
            "ejercitarse" -> TipoAccion.EJERCITARSE
            "usar_objeto", "usar objeto", "usar" -> TipoAccion.USAR_OBJETO
            "atacar" -> TipoAccion.ATACAR
            "examinar" -> TipoAccion.EXAMINAR
            else -> TipoAccion.ESPERAR
        }
        val objetivoLimpio = match.groupValues[2].trim().trim('"', '\'', '`').trimEnd('.', ';', ',')
        return tipo to objetivoLimpio
    }

    /**
     * Comprueba si el texto menciona un sustantivo/frase objetivo que no forma parte de
     * ninguna habitación, objeto conocido, inventario ni NPC del catálogo.
     */
    private fun detectarEntidadNoCatalogada(
        norm: String,
        objetivoDirecto: String?,
        tipo: TipoAccion,
        jugador: Jugador,
        habitacion: Habitacion,
        catalogo: CatalogoMundo
    ): String? {
        val conocidos = buildList {
            add(normalizar(jugador.nombre))
            catalogo.habitaciones.forEach { add(normalizar(it.nombre)) }
            catalogo.objetosConocidosGlobales.forEach { add(normalizar(it)) }
            habitacion.objetosVisibles.forEach { add(normalizar(it)) }
            habitacion.jugadoresPresentes.forEach { add(normalizar(it)) }
            jugador.inventario.forEach { add(normalizar(it)) }
            catalogo.npcsConocidos.forEach { add(normalizar(it)) }
        }.distinct()

        // Palabras neutras que forman parte de la redacción natural en español y no son entidades
        val palabrasNeutras = setOf(
            "suelo", "piso", "aire", "habitacion", "sala", "puerta", "silencio",
            "alrededor", "turno", "guardia", "cuidado", "cerca", "lejos", "todos",
            "nadie", "algo", "mismo", "lugar", "estancia", "cuerpo", "mente",
            "energia", "caminando", "despacio", "rapido", "-", "ninguno", "nada"
        )

        if (objetivoDirecto != null && tipo != TipoAccion.ESPERAR && tipo != TipoAccion.EJERCITARSE) {
            val limpio = objetivoDirecto
                .replace(Regex("""^(?:el|la|los|las|un|una)\s+"""), "")
                .trim()
            if (limpio.length >= 3 && !palabrasNeutras.contains(limpio)) {
                val coincide = conocidos.any { conocido ->
                    conocido == limpio ||
                        conocido.contains(limpio) ||
                        limpio.contains(conocido)
                }
                if (!coincide) return limpio
            }
        }

        val patronesObjetivo = listOf(
            Regex("""\b(?:un|una|unos|unas)\s+([a-z0-9\s-]{3,40})(?=\s+(?:de|del|en|para|con|hacia)|$)"""),
            Regex("""\b(?:ir|va|mover|mueve|moverse|entrar|entra|salir|sale|caminar|camina|correr|corre)\s+(?:a|al|hacia|a\s+la|hacia\s+la|hacia\s+el)\s+(?:el\s+|la\s+|los\s+|las\s+|un\s+|una\s+)?([a-z0-9\s-]{3,40})$"""),
            Regex("""\b(?:hablar|habla|conversar|conversa|preguntar|pregunta|atacar|ataca|golpear|golpea)\s+(?:a|al|con|con\s+el|con\s+la|a\s+la)\s+(?:el\s+|la\s+|los\s+|las\s+|un\s+|una\s+)?([a-z0-9\s-]{3,40})$"""),
            Regex("""\b(?:usar|usa|utilizar|utiliza|examinar|examina|inspeccionar|inspecciona|observar|observa|leer|lee|tomar|toma|coger|coge|agarrar|agarra|recoger|recoge)\s+(?:el\s+|la\s+|los\s+|las\s+|un\s+|una\s+)?([a-z0-9\s-]{3,40})$""")
        )

        for (patron in patronesObjetivo) {
            val match = patron.find(norm) ?: continue
            val candidato = match.groupValues[1]
                .replace(Regex("""\b(en\s+la\s+sala|del\s+suelo|por\s+favor|ahora)\b"""), "")
                .trim()
            if (candidato.length < 3 || palabrasNeutras.contains(candidato)) continue

            val coincide = conocidos.any { conocido ->
                conocido == candidato ||
                    conocido.contains(candidato) ||
                    candidato.contains(conocido)
            }
            if (!coincide) {
                return candidato
            }
        }
        return null
    }

    private fun detectarTipoAccion(norm: String): TipoAccion {
        return when {
            Regex("""\b(espera|esperar|aguarda|aguardar|descansa|descansar|vigila|vigilar)\b""").containsMatchIn(norm) ->
                TipoAccion.ESPERAR
            Regex("""\b(mueve|mover|moverse|va|ir|camina|caminar|entra|entrar|sale|salir|desplaza|desplazarse|dirige)\b""").containsMatchIn(norm) ->
                TipoAccion.MOVER
            Regex("""\b(ejercita|ejercitarse|entrena|entrenar|ejercicio|pesas)\b""").containsMatchIn(norm) ->
                TipoAccion.EJERCITARSE
            Regex("""\b(habla|hablar|conversa|conversar|pregunta|preguntar|dice|decir|comunica)\b""").containsMatchIn(norm) ->
                TipoAccion.HABLAR
            Regex("""\b(lee|leer|estudia|estudiar|hojear)\b""").containsMatchIn(norm) ->
                TipoAccion.LEER
            Regex("""\b(ataca|atacar|golpea|golpear|dispara|disparar|hiere|herir)\b""").containsMatchIn(norm) ->
                TipoAccion.ATACAR
            Regex("""\b(usa|usar|utiliza|utilizar|aplica|aplicar|activa|activar|toma|tomar|coge|coger|agarra|agarrar)\b""").containsMatchIn(norm) ->
                TipoAccion.USAR_OBJETO
            Regex("""\b(examina|examinar|inspecciona|inspeccionar|observa|observar|revisa|revisar|mira|mirar)\b""").containsMatchIn(norm) ->
                TipoAccion.EXAMINAR
            else -> TipoAccion.EXAMINAR
        }
    }

    private fun normalizar(texto: String): String {
        val sinAcentos = Normalizer.normalize(texto.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        return sinAcentos.replace(Regex("""\s+"""), " ")
    }
}

package com.ecosdelacoronarota.data

/**
 * Objeto estático inicial con la arquitectura completa de la mansión,
 * objetos conocidos por habitación y NPCs reconocidos por el sistema.
 */
object CatalogoMundoInicial {

    val npcsConocidos: List<String> = listOf(
        "GM_1",
        "robot bibliotecario",
        "robot chef CV-9",
        "robot CV-9",
        "Nicel",
        "masa de carne",
        "marionetas de Hana y Silas",
        "marionetas"
    )

    val objetosPorHabitacionBase: Map<String, List<String>> = mapOf(
        "Biblioteca" to listOf("robot bibliotecario", "libros", "torretas"),
        "Cocina" to listOf("ollas", "cuchillos", "manual de cocina", "robot CV-9"),
        "Zona de Limpieza" to listOf("lavadoras", "químicos", "flores azules", "botiquín"),
        "Teatro" to listOf("escenario", "butacas", "cabina insonorizada", "camarín"),
        "Sala de Música" to listOf("consola de mezclas", "tocadiscos", "micrófonos", "cabina insonorizada"),
        "Cabina Insonorizada" to listOf("micrófono de estudio", "cintas magnéticas", "panel acústico"),
        "Sótano" to listOf("panel de control", "almacén de chatarra", "celda de Nicel"),
        "Gimnasio" to listOf("pesas de hierro", "colchonetas", "saco de boxeo"),
        "Sala Principal" to listOf("reloj de péndulo", "retrato cubierto", "candelabro"),
        "Pasillo Norte" to listOf("lámparas parpadeantes", "alfombra húmeda"),
        "Pasillo Oeste" to listOf("vitrina vacía", "cuadro torcido"),
        "Pasillo Sur" to listOf("escalera de servicio", "placa conmemorativa"),
        "Pasillo Este" to listOf("reloj detenido", "cerradura sellada")
    )

    /**
     * Objetos globales conocidos que pueden estar en inventarios o mencionados en tomos.
     */
    val objetosConocidosGlobales: List<String> = listOf(
        "Llave",
        "Diario",
        "Soga de la Rendición",
        "Tomo de Criaturas I",
        "Tomo de Magia V",
        "Magia I",
        "Magia II",
        "Magia V",
        "Invocación I",
        "Enfermería I",
        "Criaturas Mágicas I",
        "Cámara",
        "Revista Velvet & Stars",
        "Espejo de plata",
        "botiquín",
        "ollas",
        "cuchillos",
        "manual de cocina",
        "robot CV-9",
        "robot bibliotecario",
        "libros",
        "torretas",
        "lavadoras",
        "químicos",
        "flores azules",
        "escenario",
        "butacas",
        "cabina insonorizada",
        "camarín",
        "consola de mezclas",
        "tocadiscos",
        "micrófonos",
        "panel de control",
        "almacén de chatarra",
        "celda de Nicel"
    )

    /**
     * Lista completa de habitaciones agrupadas por zona, incluyendo las 12 habitaciones privadas,
     * los 4 pasillos cardinales, la Sala Principal y todas las alas especiales.
     */
    val habitacionesBase: List<Habitacion> = buildList {
        add(
            Habitacion(
                id = "sala_principal",
                nombre = "Sala Principal",
                zona = "Sala Principal",
                descripcion = "Vestíbulo circular bajo una cúpula agrietada. Un reloj de péndulo marca horas imposibles.",
                objetosVisibles = objetosPorHabitacionBase["Sala Principal"].orEmpty(),
                salidas = listOf("Pasillo Norte", "Pasillo Oeste", "Pasillo Sur", "Pasillo Este", "Sótano")
            )
        )
        add(
            Habitacion(
                id = "pasillo_norte",
                nombre = "Pasillo Norte",
                zona = "Pasillo Norte",
                descripcion = "Corredor frío iluminado por apliques intermitentes que da acceso a las primeras alcobas y al Teatro.",
                objetosVisibles = objetosPorHabitacionBase["Pasillo Norte"].orEmpty(),
                salidas = listOf("Sala Principal", "Teatro", "Biblioteca", "Habitación 1", "Habitación 2", "Habitación 3")
            )
        )
        add(
            Habitacion(
                id = "pasillo_oeste",
                nombre = "Pasillo Oeste",
                zona = "Pasillo Oeste",
                descripcion = "Galería silenciosa con tapices oscuros que conecta el ala cultural y tres habitaciones privadas.",
                objetosVisibles = objetosPorHabitacionBase["Pasillo Oeste"].orEmpty(),
                salidas = listOf("Sala Principal", "Sala de Música", "Cabina Insonorizada", "Habitación 4", "Habitación 5", "Habitación 6")
            )
        )
        add(
            Habitacion(
                id = "pasillo_sur",
                nombre = "Pasillo Sur",
                zona = "Pasillo Sur",
                descripcion = "Ala de servicio con olor a humedad, lejía y aceite metálico.",
                objetosVisibles = objetosPorHabitacionBase["Pasillo Sur"].orEmpty(),
                salidas = listOf("Sala Principal", "Cocina", "Zona de Limpieza", "Habitación 7", "Habitación 8", "Habitación 9")
            )
        )
        add(
            Habitacion(
                id = "pasillo_este",
                nombre = "Pasillo Este",
                zona = "Pasillo Este",
                descripcion = "Ala de entrenamiento y reposo con puertas numeradas y muros reforzados.",
                objetosVisibles = objetosPorHabitacionBase["Pasillo Este"].orEmpty(),
                salidas = listOf("Sala Principal", "Gimnasio", "Habitación 10", "Habitación 11", "Habitación 12")
            )
        )
        add(
            Habitacion(
                id = "sotano",
                nombre = "Sótano",
                zona = "Sótano",
                descripcion = "Subsuelo húmedo donde la gravedad parece espesarse. Se oyen ecos orgánicos tras las calderas.",
                objetosVisibles = objetosPorHabitacionBase["Sótano"].orEmpty(),
                salidas = listOf("Sala Principal", "Zona de Limpieza")
            )
        )
        add(
            Habitacion(
                id = "teatro",
                nombre = "Teatro",
                zona = "Teatro",
                descripcion = "Escenario de terciopelo negro con butacas vacías. Permite entrenar la agilidad y la presencia.",
                objetosVisibles = objetosPorHabitacionBase["Teatro"].orEmpty(),
                salidas = listOf("Pasillo Norte", "Sala de Música")
            )
        )
        add(
            Habitacion(
                id = "sala_de_musica",
                nombre = "Sala de Música",
                zona = "Sala de Música",
                descripcion = "Estancia resonante con un piano vertical y acceso directo a la Cabina Insonorizada.",
                objetosVisibles = objetosPorHabitacionBase["Sala de Música"].orEmpty(),
                salidas = listOf("Pasillo Oeste", "Teatro", "Cabina Insonorizada")
            )
        )
        add(
            Habitacion(
                id = "cabina_insonorizada",
                nombre = "Cabina Insonorizada",
                zona = "Sala de Música",
                descripcion = "Recinto revestido de espuma acústica gruesa. Ningún grito atraviesa su puerta blindada.",
                objetosVisibles = objetosPorHabitacionBase["Cabina Insonorizada"].orEmpty(),
                salidas = listOf("Sala de Música", "Pasillo Oeste")
            )
        )
        add(
            Habitacion(
                id = "cocina",
                nombre = "Cocina",
                zona = "Cocina",
                descripcion = "Cocina industrial de acero frío atendida mecánicamente por el autómata chef CV-9.",
                objetosVisibles = objetosPorHabitacionBase["Cocina"].orEmpty(),
                salidas = listOf("Pasillo Sur", "Zona de Limpieza")
            )
        )
        add(
            Habitacion(
                id = "biblioteca",
                nombre = "Biblioteca",
                zona = "Biblioteca",
                descripcion = "Altas estanterías de roble custodiadas por torretas inertes y el robot bibliotecario.",
                objetosVisibles = objetosPorHabitacionBase["Biblioteca"].orEmpty(),
                salidas = listOf("Pasillo Norte")
            )
        )
        add(
            Habitacion(
                id = "zona_de_limpieza",
                nombre = "Zona de Limpieza",
                zona = "Zona de Limpieza",
                descripcion = "Lavandería de vapor denso donde crecen extrañas flores azules junto a los desagües.",
                objetosVisibles = objetosPorHabitacionBase["Zona de Limpieza"].orEmpty(),
                salidas = listOf("Pasillo Sur", "Cocina", "Sótano")
            )
        )
        add(
            Habitacion(
                id = "gimnasio",
                nombre = "Gimnasio",
                zona = "Pasillo Este",
                descripcion = "Sala de acondicionamiento físico con pesas de hierro y colchonetas desgastadas.",
                objetosVisibles = objetosPorHabitacionBase["Gimnasio"].orEmpty(),
                salidas = listOf("Pasillo Este")
            )
        )

        // Habitaciones privadas 1 a 12 distribuidas en los cuatro pasillos
        for (numero in 1..12) {
            val pasilloPadre = when (numero) {
                in 1..3 -> "Pasillo Norte"
                in 4..6 -> "Pasillo Oeste"
                in 7..9 -> "Pasillo Sur"
                else -> "Pasillo Este"
            }
            add(
                Habitacion(
                    id = "habitacion_$numero",
                    nombre = "Habitación $numero",
                    zona = "Habitaciones 1-12",
                    descripcion = "Alcoba privada número $numero asignada a uno de los huéspedes de la mansión.",
                    objetosVisibles = listOf("cama deshecha", "mesita de noche"),
                    salidas = listOf(pasilloPadre)
                )
            )
        }
    }

    /**
     * Construye el catálogo del mundo combinando la arquitectura fija con los objetos actuales por sala.
     */
    fun construirCatalogo(objetosActualesPorHabitacion: Map<String, List<String>>): CatalogoMundo {
        val mapaObjetosCombinado = objetosPorHabitacionBase.toMutableMap()
        objetosActualesPorHabitacion.forEach { (nombreHab, items) ->
            mapaObjetosCombinado[nombreHab] = items
        }

        val habitacionesConObjetos = habitacionesBase.map { hab ->
            hab.copy(objetosVisibles = mapaObjetosCombinado[hab.nombre] ?: hab.objetosVisibles)
        }

        val todosObjetos = (objetosConocidosGlobales + mapaObjetosCombinado.values.flatten())
            .distinctBy { it.lowercase() }

        return CatalogoMundo(
            habitaciones = habitacionesConObjetos,
            objetosPorHabitacion = mapaObjetosCombinado,
            npcsConocidos = npcsConocidos,
            objetosConocidosGlobales = todosObjetos
        )
    }
}

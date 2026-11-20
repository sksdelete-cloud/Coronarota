import type {
  CatalogoMundo,
  EstadoPartida,
  Habitacion,
  Jugador,
  MomentoDia,
  ResultadoValidacion,
  TipoAccion,
  Turno,
} from '../types/game';

export const MENSAJE_INVENTADO =
  'El GM no ha descrito eso. No puedes inventar objetos, lugares ni personajes.';

export function normalizarTexto(texto: string): string {
  return texto
    .toLowerCase()
    .trim()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\s+/g, ' ');
}

export function contarObjetosUtiles(inventario: string[]): number {
  return inventario.filter((item) => {
    const n = normalizarTexto(item);
    return !n.startsWith('llave') && !n.startsWith('diario');
  }).length;
}

export function calcularExpLibro(lecturas: number): number {
  if (lecturas >= 3) return 100;
  if (lecturas <= 0) return 0;
  return lecturas * 33;
}

function detectarTipoAccion(norm: string): TipoAccion {
  if (/\b(espera|esperar|aguarda|aguardar|descansa|descansar|vigila|vigilar)\b/.test(norm)) {
    return 'ESPERAR';
  }
  if (
    /\b(mueve|mover|moverse|va|ir|camina|caminar|entra|entrar|sale|salir|desplaza|desplazarse|dirige)\b/.test(
      norm
    )
  ) {
    return 'MOVER';
  }
  if (/\b(ejercita|ejercitarse|entrena|entrenar|ejercicio|pesas)\b/.test(norm)) {
    return 'EJERCITARSE';
  }
  if (/\b(habla|hablar|conversa|conversar|pregunta|preguntar|dice|decir|comunica)\b/.test(norm)) {
    return 'HABLAR';
  }
  if (/\b(lee|leer|estudia|estudiar|hojear)\b/.test(norm)) {
    return 'LEER';
  }
  if (/\b(ataca|atacar|golpea|golpear|dispara|disparar|hiere|herir)\b/.test(norm)) {
    return 'ATACAR';
  }
  if (
    /\b(usa|usar|utiliza|utilizar|aplica|aplicar|activa|activar|toma|tomar|coge|coger|agarra|agarrar)\b/.test(
      norm
    )
  ) {
    return 'USAR_OBJETO';
  }
  if (/\b(examina|examinar|inspecciona|inspeccionar|observa|observar|revisa|revisar|mira|mirar)\b/.test(norm)) {
    return 'EXAMINAR';
  }
  return 'EXAMINAR';
}

function parsearComandoEstructurado(norm: string): { tipo: TipoAccion; objetivo: string } | null {
  const sinComillas = norm.trim().replace(/^["'`]+|["'`]+$/g, '');
  const match =
    /^(mover|hablar|leer|ejercitarse|usar_objeto|usar objeto|usar|atacar|examinar|esperar)\s*:\s*(.*)$/.exec(
      sinComillas
    );
  if (!match) return null;

  let tipo: TipoAccion = 'ESPERAR';
  switch (match[1]) {
    case 'mover':
      tipo = 'MOVER';
      break;
    case 'hablar':
      tipo = 'HABLAR';
      break;
    case 'leer':
      tipo = 'LEER';
      break;
    case 'ejercitarse':
      tipo = 'EJERCITARSE';
      break;
    case 'usar_objeto':
    case 'usar objeto':
    case 'usar':
      tipo = 'USAR_OBJETO';
      break;
    case 'atacar':
      tipo = 'ATACAR';
      break;
    case 'examinar':
      tipo = 'EXAMINAR';
      break;
    default:
      tipo = 'ESPERAR';
  }

  const objetivo = match[2]
    .trim()
    .replace(/^["'`]+|["'`]+$/g, '')
    .replace(/[.;,]+$/, '');
  return { tipo, objetivo };
}

function detectarEntidadNoCatalogada(
  norm: string,
  objetivoDirecto: string | null,
  tipo: TipoAccion,
  jugador: Jugador,
  habitacion: Habitacion,
  catalogo: CatalogoMundo
): string | null {
  const conocidos = Array.from(
    new Set([
      normalizarTexto(jugador.nombre),
      ...catalogo.habitaciones.map((h) => normalizarTexto(h.nombre)),
      ...catalogo.objetosConocidosGlobales.map(normalizarTexto),
      ...habitacion.objetosVisibles.map(normalizarTexto),
      ...habitacion.jugadoresPresentes.map(normalizarTexto),
      ...jugador.inventario.map(normalizarTexto),
      ...catalogo.npcsConocidos.map(normalizarTexto),
    ])
  );

  const palabrasNeutras = new Set([
    'suelo',
    'piso',
    'aire',
    'habitacion',
    'sala',
    'puerta',
    'silencio',
    'alrededor',
    'turno',
    'guardia',
    'cuidado',
    'cerca',
    'lejos',
    'todos',
    'nadie',
    'algo',
    'mismo',
    'lugar',
    'estancia',
    'cuerpo',
    'mente',
    'energia',
    'caminando',
    'despacio',
    'rapido',
    '-',
    'ninguno',
    'nada',
  ]);

  if (objetivoDirecto !== null && tipo !== 'ESPERAR' && tipo !== 'EJERCITARSE') {
    const limpio = objetivoDirecto.replace(/^(?:el|la|los|las|un|una)\s+/, '').trim();
    if (limpio.length >= 3 && !palabrasNeutras.has(limpio)) {
      const coincide = conocidos.some(
        (conocido) =>
          conocido === limpio || conocido.includes(limpio) || limpio.includes(conocido)
      );
      if (!coincide) {
        return limpio;
      }
    }
  }

  const patrones: RegExp[] = [
    /\b(?:un|una|unos|unas)\s+([a-z0-9\s-]{3,40}?)(?=\s+(?:de|del|en|para|con|hacia)\b|$)/,
    /\b(?:ir|va|mover|mueve|moverse|entrar|entra|salir|sale|caminar|camina|correr|corre)\s+(?:a|al|hacia|a\s+la|hacia\s+la|hacia\s+el)\s+(?:el\s+|la\s+|los\s+|las\s+|un\s+|una\s+)?([a-z0-9\s-]{3,40})$/,
    /\b(?:hablar|habla|conversar|conversa|preguntar|pregunta|atacar|ataca|golpear|golpea)\s+(?:a|al|con|con\s+el|con\s+la|a\s+la)\s+(?:el\s+|la\s+|los\s+|las\s+|un\s+|una\s+)?([a-z0-9\s-]{3,40})$/,
    /\b(?:usar|usa|utilizar|utiliza|examinar|examina|inspeccionar|inspecciona|observar|observa|leer|lee|tomar|toma|coger|coge|agarrar|agarra|recoger|recoge)\s+(?:el\s+|la\s+|los\s+|las\s+|un\s+|una\s+)?([a-z0-9\s-]{3,40})$/,
  ];

  for (const patron of patrones) {
    const match = patron.exec(norm);
    if (!match || !match[1]) continue;
    const candidato = match[1]
      .replace(/\b(en\s+la\s+sala|del\s+suelo|por\s+favor|ahora)\b/g, '')
      .trim();
    if (candidato.length < 3 || palabrasNeutras.has(candidato)) continue;

    const coincide = conocidos.some(
      (conocido) =>
        conocido === candidato ||
        conocido.includes(candidato) ||
        candidato.includes(conocido)
    );
    if (!coincide) {
      return candidato;
    }
  }

  return null;
}

export function validarAccion(
  accion: string,
  jugador: Jugador,
  habitacion: Habitacion,
  catalogo: CatalogoMundo
): ResultadoValidacion {
  const limpio = accion.trim();
  if (!limpio) {
    return {
      exitoso: false,
      razon: 'Escribe una acción antes de intentar validarla.',
      efectosMecanicos: {},
      tipoDetectado: 'ESPERAR',
      habitacionDestino: null,
    };
  }

  if (jugador.estado === 'MUERTO') {
    return {
      exitoso: false,
      razon: `${jugador.nombre} está MUERTO y no puede realizar acciones.`,
      efectosMecanicos: {},
      tipoDetectado: 'ESPERAR',
      habitacionDestino: null,
    };
  }

  if (jugador.estado === 'INCONSCIENTE') {
    return {
      exitoso: false,
      razon: `${jugador.nombre} está INCONSCIENTE y necesita ser estabilizado antes de actuar.`,
      efectosMecanicos: {},
      tipoDetectado: 'ESPERAR',
      habitacionDestino: null,
    };
  }

  const norm = normalizarTexto(limpio);
  const comando = parsearComandoEstructurado(norm);
  const tipoDetectado = comando?.tipo ?? detectarTipoAccion(norm);

  const entidadInventada = detectarEntidadNoCatalogada(
    norm,
    comando?.objetivo ?? null,
    tipoDetectado,
    jugador,
    habitacion,
    catalogo
  );
  if (entidadInventada !== null) {
    return {
      exitoso: false,
      razon: MENSAJE_INVENTADO,
      efectosMecanicos: {},
      tipoDetectado,
      habitacionDestino: null,
    };
  }

  if (tipoDetectado === 'ESPERAR') {
    return {
      exitoso: true,
      razon: `${jugador.nombre} mantiene la guardia y espera en ${habitacion.nombre}.`,
      efectosMecanicos: {},
      tipoDetectado: 'ESPERAR',
      habitacionDestino: null,
    };
  }

  if (tipoDetectado === 'MOVER') {
    const salida = habitacion.salidas.find((s) => norm.includes(normalizarTexto(s)));
    if (salida) {
      return {
        exitoso: true,
        razon: `${jugador.nombre} se desplaza desde ${habitacion.nombre} hacia ${salida}.`,
        efectosMecanicos: {},
        tipoDetectado: 'MOVER',
        habitacionDestino: salida,
      };
    }

    const otraSala = catalogo.habitaciones.find(
      (h) => h.nombre !== habitacion.nombre && norm.includes(normalizarTexto(h.nombre))
    );
    if (otraSala) {
      return {
        exitoso: false,
        razon: `No hay salida directa desde ${habitacion.nombre} hacia ${otraSala.nombre}. Salidas disponibles: ${habitacion.salidas.join(', ')}.`,
        efectosMecanicos: {},
        tipoDetectado: 'MOVER',
        habitacionDestino: null,
      };
    }

    return {
      exitoso: false,
      razon: MENSAJE_INVENTADO,
      efectosMecanicos: {},
      tipoDetectado: 'MOVER',
      habitacionDestino: null,
    };
  }

  if (tipoDetectado === 'EJERCITARSE') {
    const permitida = catalogo.habitacionesEjercicio.some(
      (h) => normalizarTexto(h) === normalizarTexto(habitacion.nombre)
    );
    if (permitida) {
      return {
        exitoso: true,
        razon: `${jugador.nombre} entrena su resistencia en ${habitacion.nombre} (+1 HP).`,
        efectosMecanicos: { HP: 1 },
        tipoDetectado: 'EJERCITARSE',
        habitacionDestino: null,
      };
    }
    return {
      exitoso: false,
      razon: `No puedes ejercitarte en ${habitacion.nombre}. Solo está permitido en: ${catalogo.habitacionesEjercicio.join(' o ')}.`,
      efectosMecanicos: {},
      tipoDetectado: 'EJERCITARSE',
      habitacionDestino: null,
    };
  }

  if (tipoDetectado === 'HABLAR') {
    const otroJugador = habitacion.jugadoresPresentes.find(
      (p) => normalizarTexto(p) !== normalizarTexto(jugador.nombre) && norm.includes(normalizarTexto(p))
    );
    if (otroJugador) {
      return {
        exitoso: true,
        razon: `${jugador.nombre} conversa con ${otroJugador} en ${habitacion.nombre}.`,
        efectosMecanicos: {},
        tipoDetectado: 'HABLAR',
        habitacionDestino: null,
      };
    }

    const npcPresente = catalogo.npcsConocidos.find((npc) => {
      const npcNorm = normalizarTexto(npc);
      if (!norm.includes(npcNorm)) return false;
      const enObjetos = habitacion.objetosVisibles.some(
        (obj) => normalizarTexto(obj).includes(npcNorm) || npcNorm.includes(normalizarTexto(obj))
      );
      const enSotano =
        normalizarTexto(habitacion.nombre) === 'sotano' &&
        (npcNorm === 'nicel' || npcNorm.includes('masa'));
      return enObjetos || enSotano || npcNorm === 'gm_1';
    });

    if (npcPresente) {
      return {
        exitoso: true,
        razon: `${jugador.nombre} se dirige a ${npcPresente} en ${habitacion.nombre}.`,
        efectosMecanicos: {},
        tipoDetectado: 'HABLAR',
        habitacionDestino: null,
      };
    }

    return {
      exitoso: false,
      razon: `No hay ningún jugador o NPC compatible presente en ${habitacion.nombre} para hablar.`,
      efectosMecanicos: {},
      tipoDetectado: 'HABLAR',
      habitacionDestino: null,
    };
  }

  if (tipoDetectado === 'LEER') {
    const librosCatalogados = [
      'magia i',
      'magia ii',
      'magia v',
      'invocacion i',
      'enfermeria i',
      'criaturas magicas i',
      'tomo de criaturas i',
      'tomo de magia v',
      'libros',
      'manual de cocina',
      'revista velvet & stars',
      'diario',
    ];

    const enSala = habitacion.objetosVisibles.find((obj) => {
      const objNorm = normalizarTexto(obj);
      return (
        (objNorm.includes('libro') || objNorm.includes('manual') || objNorm.includes('tomo')) &&
        (norm.includes(objNorm) || librosCatalogados.some((l) => norm.includes(l)))
      );
    });

    const enInventario = jugador.inventario.find((item) => {
      const itemNorm = normalizarTexto(item);
      return (
        (itemNorm.includes('tomo') ||
          itemNorm.includes('diario') ||
          itemNorm.includes('revista') ||
          itemNorm.includes('magia') ||
          itemNorm.includes('criaturas')) &&
        norm.includes(itemNorm)
      );
    });

    const fuente = enSala ?? enInventario;
    if (fuente) {
      return {
        exitoso: true,
        razon: `${jugador.nombre} lee ${fuente} en ${habitacion.nombre} (+34 EXP).`,
        efectosMecanicos: { EXP: 34 },
        tipoDetectado: 'LEER',
        habitacionDestino: null,
      };
    }

    return {
      exitoso: false,
      razon: `No hay ningún libro o tomo disponible en ${habitacion.nombre} ni en tu inventario que coincida con la lectura.`,
      efectosMecanicos: {},
      tipoDetectado: 'LEER',
      habitacionDestino: null,
    };
  }

  if (tipoDetectado === 'USAR_OBJETO') {
    const enInv = jugador.inventario.find((item) => norm.includes(normalizarTexto(item)));
    const botiquinEnSala = habitacion.objetosVisibles.find(
      (obj) => normalizarTexto(obj) === 'botiquin' && norm.includes('botiquin')
    );

    const objeto = enInv ?? botiquinEnSala;
    if (!objeto) {
      return {
        exitoso: false,
        razon: `Solo puedes usar objetos que estén en tu inventario actual (${jugador.inventario.join(', ')}).`,
        efectosMecanicos: {},
        tipoDetectado: 'USAR_OBJETO',
        habitacionDestino: null,
      };
    }

    const efectos: Record<string, number> = normalizarTexto(objeto) === 'botiquin' ? { HP: 5 } : {};
    return {
      exitoso: true,
      razon: `${jugador.nombre} utiliza ${objeto} en ${habitacion.nombre}.`,
      efectosMecanicos: efectos,
      tipoDetectado: 'USAR_OBJETO',
      habitacionDestino: null,
    };
  }

  if (tipoDetectado === 'ATACAR') {
    const objetivoJugador = habitacion.jugadoresPresentes.find(
      (p) => normalizarTexto(p) !== normalizarTexto(jugador.nombre) && norm.includes(normalizarTexto(p))
    );
    const objetivoNpc = catalogo.npcsConocidos.find((npc) => {
      const npcNorm = normalizarTexto(npc);
      if (!norm.includes(npcNorm)) return false;
      return (
        habitacion.objetosVisibles.some(
          (obj) => normalizarTexto(obj).includes(npcNorm) || npcNorm.includes(normalizarTexto(obj))
        ) || normalizarTexto(habitacion.nombre) === 'sotano'
      );
    });

    const objetivo = objetivoJugador ?? objetivoNpc;
    if (objetivo) {
      return {
        exitoso: true,
        razon: `${jugador.nombre} inicia una acción ofensiva contra ${objetivo} en ${habitacion.nombre}.`,
        efectosMecanicos: { HP: -2 },
        tipoDetectado: 'ATACAR',
        habitacionDestino: null,
      };
    }

    return {
      exitoso: false,
      razon: `No hay un objetivo válido presente en ${habitacion.nombre} para atacar.`,
      efectosMecanicos: {},
      tipoDetectado: 'ATACAR',
      habitacionDestino: null,
    };
  }

  // EXAMINAR
  const objVisible = habitacion.objetosVisibles.find((obj) => norm.includes(normalizarTexto(obj)));
  const objInv = jugador.inventario.find((item) => norm.includes(normalizarTexto(item)));
  const objetivo = objVisible ?? objInv;
  if (objetivo) {
    return {
      exitoso: true,
      razon: `${jugador.nombre} examina detenidamente ${objetivo} en ${habitacion.nombre}.`,
      efectosMecanicos: {},
      tipoDetectado: 'EXAMINAR',
      habitacionDestino: null,
    };
  }

  return {
    exitoso: false,
    razon: `Ese objeto no se encuentra visible en ${habitacion.nombre}. Objetos visibles: ${habitacion.objetosVisibles.join(', ')}.`,
    efectosMecanicos: {},
    tipoDetectado: 'EXAMINAR',
    habitacionDestino: null,
  };
}

export function avanzarTurnoLogic(turno: Turno): Turno {
  const momentos: MomentoDia[] = ['MANANA', 'TARDE', 'NOCHE', 'VELADA'];
  const idx = momentos.indexOf(turno.momento);
  const nextIdx = (idx + 1) % momentos.length;
  const nuevoDia = nextIdx === 0 ? turno.dia + 1 : turno.dia;
  const siguienteGlobal = Math.min(turno.turnoGlobal + 1, turno.turnoMaximo);

  return {
    ...turno,
    dia: nuevoDia,
    momento: momentos[nextIdx],
    turnoGlobal: siguienteGlobal,
    accionesRestantes: 3,
  };
}

export function aplicarResultadoAccion(
  estado: EstadoPartida,
  jugadorId: string,
  textoAccion: string,
  resultado: ResultadoValidacion
): EstadoPartida {
  if (!resultado.exitoso) return estado;

  const jugadorPrevio = estado.jugadores.find((j) => j.id === jugadorId);
  if (!jugadorPrevio) return estado;

  const deltaHp = resultado.efectosMecanicos.HP ?? 0;
  const deltaExp = resultado.efectosMecanicos.EXP ?? 0;
  const nuevaHabitacion = resultado.habitacionDestino ?? jugadorPrevio.habitacion;

  const jugadores = estado.jugadores.map((j) => {
    if (j.id !== jugadorId) return j;
    const nuevoHp = Math.max(0, Math.min(j.hpMaximo, j.hpActual + deltaHp));
    const nuevoEstado =
      nuevoHp === 0
        ? 'MUERTO'
        : j.estado === 'INCONSCIENTE' && nuevoHp > 0
        ? 'ACTIVO'
        : j.estado;
    const exp =
      deltaExp > 0
        ? {
            ...j.exp,
            'Magia I': Math.min(100, (j.exp['Magia I'] ?? 0) + deltaExp),
          }
        : j.exp;
    return {
      ...j,
      habitacion: nuevaHabitacion,
      hpActual: nuevoHp,
      estado: nuevoEstado,
      exp,
    };
  });

  const nuevaAccion = {
    id: `${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
    jugadorId: jugadorPrevio.id,
    jugadorNombre: jugadorPrevio.nombre,
    tipo: resultado.tipoDetectado,
    descripcion: textoAccion.trim(),
    habitacionOrigen: jugadorPrevio.habitacion,
    habitacionDestino: resultado.habitacionDestino,
    turnoGlobal: estado.turno.turnoGlobal,
    resumenEfectos: resultado.razon,
  };

  return {
    ...estado,
    turno: {
      ...estado.turno,
      accionesRestantes: Math.max(0, estado.turno.accionesRestantes - 1),
    },
    jugadores,
    historialAcciones: [nuevaAccion, ...estado.historialAcciones],
  };
}

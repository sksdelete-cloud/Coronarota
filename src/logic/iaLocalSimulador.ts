import type {
  CatalogoMundo,
  Habitacion,
  Jugador,
  ResultadoValidacion,
} from '../types/game';
import { normalizarTexto } from './validadorAcciones';

export const SYSTEM_PROMPT_TRADUCTOR =
  "Eres un traductor de acciones de rol. Convierte la acción del usuario en un formato de comando para el sistema. Si menciona un objeto de su inventario, usa su nombre exacto. Responde SOLO con el comando en formato 'ACCIÓN: Objeto/Destino'. Ejemplo: 'LEER: Diario personal-Isaac'.";

export function construirSystemPromptNarrador(
  comandoValidado: string,
  habitacion: Habitacion
): string {
  const presentes =
    habitacion.jugadoresPresentes.length > 0
      ? habitacion.jugadoresPresentes.join(', ')
      : 'ningún otro investigador';
  const contexto = `${habitacion.nombre} (${habitacion.descripcion}). Presentes: ${presentes}.`;
  return `Eres el GM de un juego de horror cósmico. Narra en español la siguiente acción exitosa de forma inmersiva, breve (máximo 2 párrafos) y con tono oscuro. Acción: ${comandoValidado}. Contexto: ${contexto}.`;
}

export function validarCabeceraGguf(
  bytes: Uint8Array,
  nombreArchivo: string
): { valido: boolean; error?: string } {
  if (!nombreArchivo.toLowerCase().endsWith('.gguf')) {
    return {
      valido: false,
      error: 'Selecciona un archivo con extensión .gguf válida.',
    };
  }
  if (bytes.byteLength < 4) {
    return {
      valido: false,
      error: 'El archivo .gguf está vacío o dañado.',
    };
  }
  const esMagicGguf =
    bytes[0] === 0x47 &&
    bytes[1] === 0x47 &&
    bytes[2] === 0x55 &&
    bytes[3] === 0x46;

  if (!esMagicGguf) {
    return {
      valido: false,
      error: 'El archivo seleccionado no tiene cabecera GGUF válida.',
    };
  }
  return { valido: true };
}

function encontrarCoincidenciaCatalogo(
  normTexto: string,
  candidatos: string[]
): string | null {
  const ordenados = [...candidatos].sort((a, b) => b.length - a.length);
  for (const cand of ordenados) {
    const candNorm = normalizarTexto(cand);
    if (candNorm && normTexto.includes(candNorm)) {
      return cand;
    }
  }
  return null;
}

/**
 * Simulador determinista para la vista web del flujo TRADUCTOR de LlamaEngine:
 * convierte lenguaje natural ("Isaac lee su diario") a "ACCIÓN: Objeto/Destino"
 * ("LEER: Diario personal-Isaac") usando el inventario del jugador y la habitación.
 * Si menciona un objeto/lugar no catalogado, conserva el texto extraído para que
 * ValidadorAcciones lo rechace con seguridad.
 */
export function traducirAccionNatural(
  textoUsuario: string,
  jugador: Jugador,
  habitacion: Habitacion,
  catalogo: CatalogoMundo
): string {
  const limpio = textoUsuario.trim();
  const norm = normalizarTexto(limpio);

  // Si ya viene en formato estructurado, respetarlo
  if (/^(mover|hablar|leer|ejercitarse|usar_objeto|usar objeto|usar|atacar|examinar|esperar)\s*:/i.test(norm)) {
    return limpio;
  }

  // Detectar intención principal
  if (/\b(espera|esperar|aguarda|aguardar|descansa|descansar|vigila|vigilar)\b/.test(norm)) {
    return `ESPERAR: ${habitacion.nombre}`;
  }

  if (/\b(ejercita|ejercitarse|entrena|entrenar|ejercicio|pesas)\b/.test(norm)) {
    return `EJERCITARSE: ${habitacion.nombre}`;
  }

  if (/\b(lee|leer|estudia|estudiar|hojear)\b/.test(norm)) {
    if (/\bdiario\b/.test(norm)) {
      const diario = jugador.inventario.find((item) =>
        normalizarTexto(item).startsWith('diario')
      );
      if (diario) return `LEER: ${diario}`;
    }
    const enInv = encontrarCoincidenciaCatalogo(norm, jugador.inventario);
    if (enInv) return `LEER: ${enInv}`;
    const enSala = encontrarCoincidenciaCatalogo(norm, habitacion.objetosVisibles);
    if (enSala) return `LEER: ${enSala}`;
    return `LEER: ${limpio}`;
  }

  if (
    /\b(mueve|mover|moverse|va|ir|camina|caminar|entra|entrar|sale|salir|desplaza|desplazarse|dirige)\b/.test(
      norm
    )
  ) {
    const habDestino = encontrarCoincidenciaCatalogo(
      norm,
      catalogo.habitaciones.map((h) => h.nombre)
    );
    if (habDestino) return `MOVER: ${habDestino}`;
    const matchLugar = /\b(?:a|al|hacia)\s+(?:el\s+|la\s+)?([a-z0-9\s-]{3,40})$/.exec(norm);
    return `MOVER: ${matchLugar?.[1]?.trim() ?? limpio}`;
  }

  if (/\b(habla|hablar|conversa|conversar|pregunta|preguntar|dice|decir|comunica)\b/.test(norm)) {
    const presentesOtros = habitacion.jugadoresPresentes.filter(
      (p) => normalizarTexto(p) !== normalizarTexto(jugador.nombre)
    );
    const pj = encontrarCoincidenciaCatalogo(norm, presentesOtros);
    if (pj) return `HABLAR: ${pj}`;
    const npc = encontrarCoincidenciaCatalogo(norm, catalogo.npcsConocidos);
    if (npc) return `HABLAR: ${npc}`;
    const matchHablar = /\bcon\s+(?:el\s+|la\s+)?([a-z0-9\s-]{3,40})$/.exec(norm);
    return `HABLAR: ${matchHablar?.[1]?.trim() ?? limpio}`;
  }

  if (/\b(ataca|atacar|golpea|golpear|dispara|disparar|hiere|herir)\b/.test(norm)) {
    const presentesOtros = habitacion.jugadoresPresentes.filter(
      (p) => normalizarTexto(p) !== normalizarTexto(jugador.nombre)
    );
    const objetivo = encontrarCoincidenciaCatalogo(norm, [
      ...presentesOtros,
      ...catalogo.npcsConocidos,
    ]);
    return `ATACAR: ${objetivo ?? limpio}`;
  }

  if (
    /\b(usa|usar|utiliza|utilizar|aplica|aplicar|activa|activar|toma|tomar|coge|coger|agarra|agarrar)\b/.test(
      norm
    )
  ) {
    if (/\bllave\b/.test(norm)) {
      const llave = jugador.inventario.find((item) =>
        normalizarTexto(item).startsWith('llave')
      );
      if (llave) return `USAR_OBJETO: ${llave}`;
    }
    const enInv = encontrarCoincidenciaCatalogo(norm, jugador.inventario);
    if (enInv) return `USAR_OBJETO: ${enInv}`;
    const enSala = encontrarCoincidenciaCatalogo(norm, habitacion.objetosVisibles);
    if (enSala) return `USAR_OBJETO: ${enSala}`;
    return `USAR_OBJETO: ${limpio}`;
  }

  const objSala = encontrarCoincidenciaCatalogo(norm, habitacion.objetosVisibles);
  if (objSala) return `EXAMINAR: ${objSala}`;
  const objInv = encontrarCoincidenciaCatalogo(norm, jugador.inventario);
  if (objInv) return `EXAMINAR: ${objInv}`;

  return `EXAMINAR: ${limpio}`;
}

/**
 * Simula la salida del NARRADOR en 1-2 párrafos oscuros de horror cósmico en español
 * para previsualizar el flujo completo de la Fase 2 en la web.
 */
export function generarNarracionCosmica(
  comandoValidado: string,
  jugador: Jugador,
  habitacion: Habitacion,
  resultado: ResultadoValidacion
): string {
  const presentesOtros = habitacion.jugadoresPresentes.filter(
    (p) => normalizarTexto(p) !== normalizarTexto(jugador.nombre)
  );
  const compania =
    presentesOtros.length > 0
      ? `bajo la mirada inquieta de ${presentesOtros.join(' y ')}`
      : 'rodeado por un silencio que parece observar desde las esquinas';

  const parrafoUno = `En ${habitacion.nombre}, la penumbra vibra con un pulso imperceptible mientras ${jugador.nombre} ejecuta «${comandoValidado}», ${compania}. Cada sombra de la estancia se alarga un instante más de lo natural, como si la propia mansión recordara el peso de la Corona Rota.`;

  const parrafoDos =
    resultado.tipoDetectado === 'LEER'
      ? `Las líneas de tinta parecen reacomodarse ante sus ojos antes de asentarse en la memoria. ${resultado.razon} Un frío súbito recorre el aire y deja la certeza de que algo en las profundidades del sótano acaba de notar su progreso.`
      : `${resultado.razon} El eco de la acción se disuelve lentamente entre los muros húmedos, recordándoles que el reloj del turno avanza implacable.`;

  return `${parrafoUno}\n\n${parrafoDos}`;
}

import test from 'node:test';
import assert from 'node:assert/strict';
import initialStateJson from '../../app/src/main/assets/initial_state.json';
import { construirCatalogoMundo, formatearIndicadorTurno } from './catalogoMundo';
import {
  construirSystemPromptNarrador,
  generarNarracionCosmica,
  traducirAccionNatural,
  validarCabeceraGguf,
} from './iaLocalSimulador';
import {
  MENSAJE_INVENTADO,
  aplicarResultadoAccion,
  avanzarTurnoLogic,
  calcularExpLibro,
  contarObjetosUtiles,
  validarAccion,
} from './validadorAcciones';
import type { EstadoPartida, Jugador, Turno } from '../types/game';

const jugadoresBase: Jugador[] = initialStateJson.jugadores.map((j) => ({
  id: j.id,
  nombre: j.nombre,
  habitacion: j.habitacion,
  hpActual: j.hp,
  hpMaximo: j.hpMax,
  estado: j.estado as Jugador['estado'],
  inventario: j.inventario,
  exp: j.exp ?? {},
  ubicacionVisible: j.estado !== 'MUERTO',
  notas: '',
}));

const catalogo = construirCatalogoMundo(initialStateJson.objetosVisibles, jugadoresBase);

test('indicador de turno inicial formatea Día 1 — Mañana — Turno 1/15', () => {
  const turno: Turno = {
    dia: 1,
    momento: 'MANANA',
    turnoGlobal: initialStateJson.turnoGlobal,
    turnoMaximo: 15,
    accionesRestantes: 3,
  };
  assert.equal(formatearIndicadorTurno(turno), 'Día 1 — Mañana — Turno 1/15');
});

test('contarObjetosUtiles excluye Llave de dormitorio-* y Diario personal-* del máximo de 3', () => {
  const isaac = jugadoresBase.find((j) => j.id === 'isaac-1')!;
  assert.equal(contarObjetosUtiles(isaac.inventario), 0);
  assert.equal(
    contarObjetosUtiles([...isaac.inventario, 'Soga de la Rendición', 'Cámara']),
    2
  );
});

test('rechaza objetos, lugares o personajes inventados con el mensaje exacto del GM', () => {
  const isaac = jugadoresBase.find((j) => j.id === 'isaac-1')!;
  const habitacion1 = catalogo.habitaciones.find((h) => h.nombre === 'Habitación 1')!;

  const resObjeto = validarAccion('Isaac toma una espada laser del suelo', isaac, habitacion1, catalogo);
  assert.equal(resObjeto.exitoso, false);
  assert.equal(resObjeto.razon, MENSAJE_INVENTADO);

  const resLugar = validarAccion('Isaac va al Jardin Secreto', isaac, habitacion1, catalogo);
  assert.equal(resLugar.exitoso, false);
  assert.equal(resLugar.razon, MENSAJE_INVENTADO);

  const resNpc = validarAccion('Isaac habla con el detective Ramirez', isaac, habitacion1, catalogo);
  assert.equal(resNpc.exitoso, false);
  assert.equal(resNpc.razon, MENSAJE_INVENTADO);
});

test('permite moverse solo a habitaciones conectadas por salidas y rechaza saltos directos', () => {
  const isaac = jugadoresBase.find((j) => j.id === 'isaac-1')!;
  const habitacion1 = catalogo.habitaciones.find((h) => h.nombre === 'Habitación 1')!;

  const valido = validarAccion('Isaac se mueve a Pasillo Norte', isaac, habitacion1, catalogo);
  assert.equal(valido.exitoso, true);
  assert.equal(valido.habitacionDestino, 'Pasillo Norte');

  const invalido = validarAccion('Isaac va hacia Teatro', isaac, habitacion1, catalogo);
  assert.equal(invalido.exitoso, false);
  assert.match(invalido.razon, /No hay salida directa/);
});

test('permite examinar nuevos objetos visibles en Teatro y Sótano y ejercitarse solo en Gimnasio/Teatro', () => {
  const mara = jugadoresBase.find((j) => j.id === 'mara-10')!;
  const teatro = catalogo.habitaciones.find((h) => h.nombre === 'Teatro')!;
  const sotano = catalogo.habitaciones.find((h) => h.nombre === 'Sótano')!;
  const habitacion10 = catalogo.habitaciones.find((h) => h.nombre === 'Habitación 10')!;

  const examinarCamarin = validarAccion('Mara examinar camarin', mara, teatro, catalogo);
  assert.equal(examinarCamarin.exitoso, true);

  const examinarCelda = validarAccion('Mara examinar celda de Nicel', mara, sotano, catalogo);
  assert.equal(examinarCelda.exitoso, true);

  const ejercicioTeatro = validarAccion('Mara se ejercita en el escenario', mara, teatro, catalogo);
  assert.equal(ejercicioTeatro.exitoso, true);
  assert.equal(ejercicioTeatro.efectosMecanicos.HP, 1);

  const ejercicioHab = validarAccion('Mara se ejercita', mara, habitacion10, catalogo);
  assert.equal(ejercicioHab.exitoso, false);
});

test('avanzarTurnoLogic incrementa turnoGlobal desde 1 a 2 y avanza franja horaria', () => {
  const turnoInicial: Turno = {
    dia: 1,
    momento: 'MANANA',
    turnoGlobal: 1,
    turnoMaximo: 15,
    accionesRestantes: 1,
  };
  const siguiente = avanzarTurnoLogic(turnoInicial);
  assert.equal(siguiente.turnoGlobal, 2);
  assert.equal(siguiente.momento, 'TARDE');
  assert.equal(siguiente.accionesRestantes, 3);
});

test('calcularExpLibro desbloquea 100/100 tras 3 lecturas', () => {
  assert.equal(calcularExpLibro(0), 0);
  assert.equal(calcularExpLibro(1), 33);
  assert.equal(calcularExpLibro(2), 66);
  assert.equal(calcularExpLibro(3), 100);
});

test('aplicarResultadoAccion actualiza habitacion del jugador y reduce accionesRestantes', () => {
  const estadoInicial: EstadoPartida = {
    turno: {
      dia: 1,
      momento: 'MANANA',
      turnoGlobal: 1,
      turnoMaximo: 15,
      accionesRestantes: 3,
    },
    jugadores: jugadoresBase,
    objetosPorHabitacion: initialStateJson.objetosVisibles,
    lecturasPorLibro: {},
    historialAcciones: [],
  };
  const isaac = jugadoresBase.find((j) => j.id === 'isaac-1')!;
  const habitacion1 = catalogo.habitaciones.find((h) => h.nombre === 'Habitación 1')!;
  const res = validarAccion('Isaac mover a Pasillo Norte', isaac, habitacion1, catalogo);

  const actualizado = aplicarResultadoAccion(estadoInicial, 'isaac-1', 'Isaac mover a Pasillo Norte', res);
  assert.equal(actualizado.jugadores.find((j) => j.id === 'isaac-1')!.habitacion, 'Pasillo Norte');
  assert.equal(actualizado.turno.accionesRestantes, 2);
  assert.equal(actualizado.historialAcciones.length, 1);
});

test('Fase 2: validarAccion acepta formato estructurado "LEER: Diario personal-Isaac" y rechaza inventados', () => {
  const isaac = jugadoresBase.find((j) => j.id === 'isaac-1')!;
  const habitacion1 = catalogo.habitaciones.find((h) => h.nombre === 'Habitación 1')!;

  const valido = validarAccion('LEER: Diario personal-Isaac', isaac, habitacion1, catalogo);
  assert.equal(valido.exitoso, true);
  assert.equal(valido.tipoDetectado, 'LEER');
  assert.equal(valido.efectosMecanicos.EXP, 34);

  const inventado = validarAccion('EXAMINAR: espada laser', isaac, habitacion1, catalogo);
  assert.equal(inventado.exitoso, false);
  assert.equal(inventado.razon, MENSAJE_INVENTADO);
});

test('Fase 2: traductor natural, validación GGUF y narración cósmica funcionan de extremo a extremo', () => {
  const isaac = jugadoresBase.find((j) => j.id === 'isaac-1')!;
  const habitacion1 = catalogo.habitaciones.find((h) => h.nombre === 'Habitación 1')!;

  // Validar cabecera mágica GGUF
  const cabeceraValida = new Uint8Array([0x47, 0x47, 0x55, 0x46, 0x03, 0x00, 0x00, 0x00]);
  assert.equal(validarCabeceraGguf(cabeceraValida, 'qwen2.5-1.5b-q4.gguf').valido, true);
  const cabeceraMala = new Uint8Array([0x50, 0x4b, 0x03, 0x04]);
  assert.equal(validarCabeceraGguf(cabeceraMala, 'falso.gguf').valido, false);
  assert.equal(validarCabeceraGguf(cabeceraValida, 'modelo.bin').valido, false);

  // Traducir "Isaac lee su diario" -> "LEER: Diario personal-Isaac" -> validar -> narrar
  const traducido = traducirAccionNatural('Isaac lee su diario', isaac, habitacion1, catalogo);
  assert.equal(traducido, 'LEER: Diario personal-Isaac');

  const resultado = validarAccion(traducido, isaac, habitacion1, catalogo);
  assert.equal(resultado.exitoso, true);

  const promptNarrador = construirSystemPromptNarrador(traducido, habitacion1);
  assert.match(promptNarrador, /Eres el GM de un juego de horror cósmico/);
  assert.match(promptNarrador, /LEER: Diario personal-Isaac/);

  const narracion = generarNarracionCosmica(traducido, isaac, habitacion1, resultado);
  assert.equal(narracion.split('\n\n').length, 2);
  assert.match(narracion, /Habitación 1/);
});

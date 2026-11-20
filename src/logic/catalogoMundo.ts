import type { CatalogoMundo, EstadoJugador, Habitacion, Jugador, MomentoDia, Turno } from '../types/game';

export const NPCS_CONOCIDOS: string[] = [
  'GM_1',
  'robot bibliotecario',
  'robot chef CV-9',
  'robot CV-9',
  'Nicel',
  'masa de carne',
  'marionetas de Hana y Silas',
  'marionetas',
];

export const OBJETOS_POR_HABITACION_BASE: Record<string, string[]> = {
  Biblioteca: ['robot bibliotecario', 'libros', 'torretas'],
  Cocina: ['ollas', 'cuchillos', 'manual de cocina', 'robot CV-9'],
  'Zona de Limpieza': ['lavadoras', 'químicos', 'flores azules', 'botiquín'],
  Teatro: ['escenario', 'butacas', 'cabina insonorizada', 'camarín'],
  'Sala de Música': ['consola de mezclas', 'tocadiscos', 'micrófonos', 'cabina insonorizada'],
  'Cabina Insonorizada': ['micrófono de estudio', 'cintas magnéticas', 'panel acústico'],
  Sótano: ['panel de control', 'almacén de chatarra', 'celda de Nicel'],
  Gimnasio: ['pesas de hierro', 'colchonetas', 'saco de boxeo'],
  'Sala Principal': ['reloj de péndulo', 'retrato cubierto', 'candelabro'],
  'Pasillo Norte': ['lámparas parpadeantes', 'alfombra húmeda'],
  'Pasillo Oeste': ['vitrina vacía', 'cuadro torcido'],
  'Pasillo Sur': ['escalera de servicio', 'placa conmemorativa'],
  'Pasillo Este': ['reloj detenido', 'cerradura sellada'],
};

export const OBJETOS_CONOCIDOS_GLOBALES: string[] = [
  'Llave',
  'Diario',
  'Soga de la Rendición',
  'Tomo de Criaturas I',
  'Tomo de Magia V',
  'Magia I',
  'Magia II',
  'Magia V',
  'Invocación I',
  'Enfermería I',
  'Criaturas Mágicas I',
  'Cámara',
  'Revista Velvet & Stars',
  'Espejo de plata',
  'botiquín',
  'ollas',
  'cuchillos',
  'manual de cocina',
  'robot CV-9',
  'robot bibliotecario',
  'libros',
  'torretas',
  'lavadoras',
  'químicos',
  'flores azules',
  'escenario',
  'butacas',
  'cabina insonorizada',
  'camarín',
  'consola de mezclas',
  'tocadiscos',
  'micrófonos',
  'panel de control',
  'almacén de chatarra',
  'celda de Nicel',
];

const HABITACIONES_FIJAS: Habitacion[] = [
  {
    id: 'sala_principal',
    nombre: 'Sala Principal',
    zona: 'Sala Principal',
    descripcion: 'Vestíbulo circular bajo una cúpula agrietada. Un reloj de péndulo marca horas imposibles.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Sala Principal'],
    jugadoresPresentes: [],
    salidas: ['Pasillo Norte', 'Pasillo Oeste', 'Pasillo Sur', 'Pasillo Este', 'Sótano'],
  },
  {
    id: 'pasillo_norte',
    nombre: 'Pasillo Norte',
    zona: 'Pasillo Norte',
    descripcion: 'Corredor frío iluminado por apliques intermitentes que da acceso a las primeras alcobas y al Teatro.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Pasillo Norte'],
    jugadoresPresentes: [],
    salidas: ['Sala Principal', 'Teatro', 'Biblioteca', 'Habitación 1', 'Habitación 2', 'Habitación 3'],
  },
  {
    id: 'pasillo_oeste',
    nombre: 'Pasillo Oeste',
    zona: 'Pasillo Oeste',
    descripcion: 'Galería silenciosa con tapices oscuros que conecta el ala cultural y tres habitaciones privadas.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Pasillo Oeste'],
    jugadoresPresentes: [],
    salidas: ['Sala Principal', 'Sala de Música', 'Cabina Insonorizada', 'Habitación 4', 'Habitación 5', 'Habitación 6'],
  },
  {
    id: 'pasillo_sur',
    nombre: 'Pasillo Sur',
    zona: 'Pasillo Sur',
    descripcion: 'Ala de servicio con olor a humedad, lejía y aceite metálico.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Pasillo Sur'],
    jugadoresPresentes: [],
    salidas: ['Sala Principal', 'Cocina', 'Zona de Limpieza', 'Habitación 7', 'Habitación 8', 'Habitación 9'],
  },
  {
    id: 'pasillo_este',
    nombre: 'Pasillo Este',
    zona: 'Pasillo Este',
    descripcion: 'Ala de entrenamiento y reposo con puertas numeradas y muros reforzados.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Pasillo Este'],
    jugadoresPresentes: [],
    salidas: ['Sala Principal', 'Gimnasio', 'Habitación 10', 'Habitación 11', 'Habitación 12'],
  },
  {
    id: 'sotano',
    nombre: 'Sótano',
    zona: 'Sótano',
    descripcion: 'Subsuelo húmedo donde la gravedad parece espesarse. Se oyen ecos orgánicos tras las calderas.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Sótano'],
    jugadoresPresentes: [],
    salidas: ['Sala Principal', 'Zona de Limpieza'],
  },
  {
    id: 'teatro',
    nombre: 'Teatro',
    zona: 'Teatro',
    descripcion: 'Escenario de terciopelo negro con butacas vacías. Permite entrenar la agilidad y la presencia.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Teatro'],
    jugadoresPresentes: [],
    salidas: ['Pasillo Norte', 'Sala de Música'],
  },
  {
    id: 'sala_de_musica',
    nombre: 'Sala de Música',
    zona: 'Sala de Música',
    descripcion: 'Estancia resonante con un piano vertical y acceso directo a la Cabina Insonorizada.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Sala de Música'],
    jugadoresPresentes: [],
    salidas: ['Pasillo Oeste', 'Teatro', 'Cabina Insonorizada'],
  },
  {
    id: 'cabina_insonorizada',
    nombre: 'Cabina Insonorizada',
    zona: 'Sala de Música',
    descripcion: 'Recinto revestido de espuma acústica gruesa. Ningún grito atraviesa su puerta blindada.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Cabina Insonorizada'],
    jugadoresPresentes: [],
    salidas: ['Sala de Música', 'Pasillo Oeste'],
  },
  {
    id: 'cocina',
    nombre: 'Cocina',
    zona: 'Cocina',
    descripcion: 'Cocina industrial de acero frío atendida mecánicamente por el autómata chef CV-9.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Cocina'],
    jugadoresPresentes: [],
    salidas: ['Pasillo Sur', 'Zona de Limpieza'],
  },
  {
    id: 'biblioteca',
    nombre: 'Biblioteca',
    zona: 'Biblioteca',
    descripcion: 'Altas estanterías de roble custodiadas por torretas inertes y el robot bibliotecario.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Biblioteca'],
    jugadoresPresentes: [],
    salidas: ['Pasillo Norte'],
  },
  {
    id: 'zona_de_limpieza',
    nombre: 'Zona de Limpieza',
    zona: 'Zona de Limpieza',
    descripcion: 'Lavandería de vapor denso donde crecen extrañas flores azules junto a los desagües.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Zona de Limpieza'],
    jugadoresPresentes: [],
    salidas: ['Pasillo Sur', 'Cocina', 'Sótano'],
  },
  {
    id: 'gimnasio',
    nombre: 'Gimnasio',
    zona: 'Pasillo Este',
    descripcion: 'Sala de acondicionamiento físico con pesas de hierro y colchonetas desgastadas.',
    objetosVisibles: OBJETOS_POR_HABITACION_BASE['Gimnasio'],
    jugadoresPresentes: [],
    salidas: ['Pasillo Este'],
  },
];

const HABITACIONES_PRIVADAS: Habitacion[] = Array.from({ length: 12 }, (_, idx) => {
  const num = idx + 1;
  const pasilloPadre =
    num <= 3
      ? 'Pasillo Norte'
      : num <= 6
      ? 'Pasillo Oeste'
      : num <= 9
      ? 'Pasillo Sur'
      : 'Pasillo Este';
  return {
    id: `habitacion_${num}`,
    nombre: `Habitación ${num}`,
    zona: 'Habitaciones 1-12',
    descripcion: `Alcoba privada número ${num} asignada a uno de los huéspedes de la mansión.`,
    objetosVisibles: ['cama deshecha', 'mesita de noche'],
    jugadoresPresentes: [],
    salidas: [pasilloPadre],
  };
});

export const HABITACIONES_BASE: Habitacion[] = [...HABITACIONES_FIJAS, ...HABITACIONES_PRIVADAS];

export function construirCatalogoMundo(
  objetosActualesPorHabitacion: Record<string, string[]>,
  jugadores: Jugador[] = []
): CatalogoMundo {
  const mapaObjetos: Record<string, string[]> = {
    ...OBJETOS_POR_HABITACION_BASE,
    ...objetosActualesPorHabitacion,
  };

  const habitaciones: Habitacion[] = HABITACIONES_BASE.map((hab) => ({
    ...hab,
    objetosVisibles: mapaObjetos[hab.nombre] ?? hab.objetosVisibles,
    jugadoresPresentes: jugadores
      .filter((j) => j.habitacion === hab.nombre && (j.estado as EstadoJugador) !== 'MUERTO')
      .map((j) => j.nombre),
  }));

  const objetosGlobales = Array.from(
    new Set([
      ...OBJETOS_CONOCIDOS_GLOBALES,
      ...Object.values(mapaObjetos).flat(),
    ])
  );

  return {
    habitaciones,
    objetosPorHabitacion: mapaObjetos,
    npcsConocidos: NPCS_CONOCIDOS,
    objetosConocidosGlobales: objetosGlobales,
    habitacionesEjercicio: ['Gimnasio', 'Teatro'],
  };
}

export function formatearIndicadorTurno(turno: Turno): string {
  const etiquetas: Record<MomentoDia, string> = {
    MANANA: 'Mañana',
    TARDE: 'Tarde',
    NOCHE: 'Noche',
    VELADA: 'Velada',
  };
  return `Día ${turno.dia} — ${etiquetas[turno.momento]} — Turno ${turno.turnoGlobal}/${turno.turnoMaximo}`;
}

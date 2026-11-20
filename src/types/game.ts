export type EstadoJugador = 'ACTIVO' | 'INCONSCIENTE' | 'MUERTO';

export type MomentoDia = 'MANANA' | 'TARDE' | 'NOCHE' | 'VELADA';

export type TipoAccion =
  | 'MOVER'
  | 'HABLAR'
  | 'LEER'
  | 'EJERCITARSE'
  | 'USAR_OBJETO'
  | 'ATACAR'
  | 'EXAMINAR'
  | 'ESPERAR';

export interface Jugador {
  id: string;
  nombre: string;
  habitacion: string;
  hpActual: number;
  hpMaximo: number;
  estado: EstadoJugador;
  inventario: string[];
  exp: Record<string, number>;
  ubicacionVisible: boolean;
  notas: string;
}

export interface Turno {
  dia: number;
  momento: MomentoDia;
  turnoGlobal: number;
  turnoMaximo: number;
  accionesRestantes: number;
}

export interface Accion {
  id: string;
  jugadorId: string;
  jugadorNombre: string;
  tipo: TipoAccion;
  descripcion: string;
  habitacionOrigen: string;
  habitacionDestino: string | null;
  turnoGlobal: number;
  resumenEfectos: string;
}

export interface Pagina {
  titulo: string;
  texto: string;
}

export interface Capitulo {
  numero: number;
  titulo: string;
  paginas: Pagina[];
}

export interface Libro {
  id: string;
  titulo: string;
  categoria: string;
  capitulos: Capitulo[];
}

export interface Habitacion {
  id: string;
  nombre: string;
  zona: string;
  descripcion: string;
  objetosVisibles: string[];
  jugadoresPresentes: string[];
  salidas: string[];
}

export interface CatalogoMundo {
  habitaciones: Habitacion[];
  objetosPorHabitacion: Record<string, string[]>;
  npcsConocidos: string[];
  objetosConocidosGlobales: string[];
  habitacionesEjercicio: string[];
}

export interface ResultadoValidacion {
  exitoso: boolean;
  razon: string;
  efectosMecanicos: Record<string, number>;
  tipoDetectado: TipoAccion;
  habitacionDestino: string | null;
  comandoTraducido?: string | null;
}

export interface EstadoPartida {
  turno: Turno;
  jugadores: Jugador[];
  objetosPorHabitacion: Record<string, string[]>;
  lecturasPorLibro: Record<string, number>;
  historialAcciones: Accion[];
}

export type PestanaApp = 'estado' | 'mapa' | 'biblioteca' | 'gm';

import { useEffect, useMemo, useState } from 'react';
import { BookOpen, Code2, Edit3, Map as MapIcon, Users } from 'lucide-react';
import booksJson from '../app/src/main/assets/books.json';
import initialStateJson from '../app/src/main/assets/initial_state.json';
import { AndroidSourceModal } from './components/AndroidSourceModal';
import { BibliotecaScreen } from './components/BibliotecaScreen';
import { EstadoScreen } from './components/EstadoScreen';
import { GmPanelScreen } from './components/GmPanelScreen';
import { MapaScreen } from './components/MapaScreen';
import { TurnBanner } from './components/TurnBanner';
import { construirCatalogoMundo } from './logic/catalogoMundo';
import {
  aplicarResultadoAccion,
  avanzarTurnoLogic,
  validarAccion,
} from './logic/validadorAcciones';
import {
  generarNarracionCosmica,
  traducirAccionNatural,
  validarCabeceraGguf,
} from './logic/iaLocalSimulador';
import type {
  EstadoPartida,
  Jugador,
  Libro,
  MomentoDia,
  PestanaApp,
  ResultadoValidacion,
} from './types/game';

const STORAGE_KEY = 'ecos_corona_rota_estado_v2';
const NARRACION_INICIAL = 'La IA narrará aquí';

function mapearMomentoDia(valor: string): MomentoDia {
  const norm = valor.trim().toUpperCase().replace('Ñ', 'N');
  if (norm === 'MANANA' || norm === 'TARDE' || norm === 'NOCHE' || norm === 'VELADA') {
    return norm;
  }
  return 'MANANA';
}

function crearEstadoInicial(): EstadoPartida {
  return {
    turno: {
      dia: 1,
      momento: mapearMomentoDia(initialStateJson.dia),
      turnoGlobal: initialStateJson.turnoGlobal,
      turnoMaximo: 15,
      accionesRestantes: 3,
    },
    jugadores: initialStateJson.jugadores.map((j) => ({
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
    })),
    objetosPorHabitacion: initialStateJson.objetosVisibles,
    lecturasPorLibro: {},
    historialAcciones: [],
  };
}

function cargarEstadoGuardado(): EstadoPartida {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return crearEstadoInicial();
    const parsed = JSON.parse(raw) as EstadoPartida;
    if (!parsed?.jugadores?.length || !parsed?.turno) return crearEstadoInicial();
    return parsed;
  } catch {
    return crearEstadoInicial();
  }
}

export default function App() {
  const [estado, setEstado] = useState<EstadoPartida>(() => cargarEstadoGuardado());
  const [pestana, setPestana] = useState<PestanaApp>('estado');
  const [jugadorSeleccionadoId, setJugadorSeleccionadoId] = useState<string>('isaac-1');
  const [ultimoResultado, setUltimoResultado] = useState<ResultadoValidacion | null>(null);
  const [visorAndroidAbierto, setVisorAndroidAbierto] = useState(false);

  // Estado Fase 2: Motor IA local (.gguf)
  const [modeloIaCargado, setModeloIaCargado] = useState(false);
  const [nombreModeloIa, setNombreModeloIa] = useState<string | null>(null);
  const [errorModeloIa, setErrorModeloIa] = useState<string | null>(null);
  const [narracionActual, setNarracionActual] = useState<string>(NARRACION_INICIAL);

  const libros = useMemo(() => booksJson.libros as Libro[], []);
  const catalogo = useMemo(
    () => construirCatalogoMundo(estado.objetosPorHabitacion, estado.jugadores),
    [estado.objetosPorHabitacion, estado.jugadores]
  );

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(estado));
    } catch {
      // Ignorar si el almacenamiento local está restringido
    }
  }, [estado]);

  const manejarCargarModeloIa = async (archivo: File) => {
    try {
      setErrorModeloIa(null);
      const buffer = await archivo.slice(0, 64).arrayBuffer();
      const validacion = validarCabeceraGguf(new Uint8Array(buffer), archivo.name);
      if (!validacion.valido) {
        setModeloIaCargado(false);
        setNombreModeloIa(null);
        setErrorModeloIa(validacion.error ?? 'Archivo .gguf inválido.');
        return;
      }
      setModeloIaCargado(true);
      setNombreModeloIa(archivo.name);
      setErrorModeloIa(null);
    } catch {
      setErrorModeloIa('No se pudo leer el archivo seleccionado desde el dispositivo.');
    }
  };

  const manejarDescargarModeloIa = () => {
    setModeloIaCargado(false);
    setNombreModeloIa(null);
    setErrorModeloIa(null);
    setNarracionActual(NARRACION_INICIAL);
  };

  const manejarValidarYEnviar = (textoAccion: string): boolean => {
    const jugador = estado.jugadores.find((j) => j.id === jugadorSeleccionadoId);
    if (!jugador) return false;
    const habitacion = catalogo.habitaciones.find((h) => h.nombre === jugador.habitacion);
    if (!habitacion) return false;

    const textoLimpio = textoAccion.trim();
    if (!textoLimpio) {
      setUltimoResultado(validarAccion('', jugador, habitacion, catalogo));
      return false;
    }

    // Flujo Fase 2: Si el modelo .gguf está cargado, traduce primero; si no, validación estricta Fase 1
    let comandoParaValidar = textoLimpio;
    let comandoTraducido: string | null = null;
    if (modeloIaCargado) {
      comandoTraducido = traducirAccionNatural(textoLimpio, jugador, habitacion, catalogo);
      comandoParaValidar = comandoTraducido;
    }

    const resultadoBase = validarAccion(comandoParaValidar, jugador, habitacion, catalogo);
    const resultado: ResultadoValidacion = {
      ...resultadoBase,
      comandoTraducido,
    };
    setUltimoResultado(resultado);

    if (resultado.exitoso) {
      const nuevoEstado = aplicarResultadoAccion(estado, jugador.id, textoLimpio, resultado);
      setEstado(nuevoEstado);

      if (modeloIaCargado) {
        const catalogoActualizado = construirCatalogoMundo(
          nuevoEstado.objetosPorHabitacion,
          nuevoEstado.jugadores
        );
        const nuevaHabNombre = resultado.habitacionDestino ?? jugador.habitacion;
        const habContexto =
          catalogoActualizado.habitaciones.find((h) => h.nombre === nuevaHabNombre) ??
          habitacion;
        setNarracionActual(
          generarNarracionCosmica(
            comandoTraducido ?? comandoParaValidar,
            jugador,
            habContexto,
            resultado
          )
        );
      } else {
        setNarracionActual(NARRACION_INICIAL);
      }
    }

    return resultado.exitoso;
  };

  const manejarAvanzarTurno = () => {
    setUltimoResultado(null);
    setEstado((prev) => ({
      ...prev,
      turno: avanzarTurnoLogic(prev.turno),
    }));
  };

  const manejarRegistrarLectura = (libroId: string) => {
    setEstado((prev) => {
      const prevCount = prev.lecturasPorLibro[libroId] ?? 0;
      return {
        ...prev,
        lecturasPorLibro: {
          ...prev.lecturasPorLibro,
          [libroId]: Math.min(3, prevCount + 1),
        },
      };
    });
  };

  const manejarGuardarNota = (jugadorId: string, nota: string) => {
    setEstado((prev) => ({
      ...prev,
      jugadores: prev.jugadores.map((j) =>
        j.id === jugadorId ? { ...j, notas: nota } : j
      ),
    }));
  };

  const manejarReiniciarPartida = () => {
    setUltimoResultado(null);
    setNarracionActual(NARRACION_INICIAL);
    setEstado(crearEstadoInicial());
  };

  const tabs: { id: PestanaApp; label: string; shortLabel: string; icon: typeof Users }[] = [
    { id: 'estado', label: 'Estado', shortLabel: 'Estado', icon: Users },
    { id: 'mapa', label: 'Mapa', shortLabel: 'Mapa', icon: MapIcon },
    { id: 'biblioteca', label: 'Biblioteca', shortLabel: 'Biblioteca', icon: BookOpen },
    { id: 'gm', label: 'Panel del GM', shortLabel: 'Panel GM', icon: Edit3 },
  ];

  return (
    <div className="min-h-screen bg-[#0D0D0D] pb-24 text-[#F2EFE9] md:pb-10">
      <header className="sticky top-0 z-30 border-b border-[#2B2B33] bg-[#0D0D0D]/95 backdrop-blur-md">
        <div className="mx-auto flex h-14 max-w-7xl items-center justify-between gap-8 px-4 sm:px-6">
          <a
            href="#estado"
            onClick={(e) => {
              e.preventDefault();
              setPestana('estado');
            }}
            className="font-serif-title shrink-0 whitespace-nowrap text-lg font-bold tracking-tight text-[#F2EFE9] sm:text-xl"
          >
            Ecos de la Corona Rota
          </a>

          <nav aria-label="Navegación principal" className="hidden items-center gap-6 md:flex">
            {tabs.map((tab) => {
              const activo = pestana === tab.id;
              return (
                <button
                  key={tab.id}
                  type="button"
                  onClick={() => setPestana(tab.id)}
                  className={`shrink-0 whitespace-nowrap border-b-2 py-4 text-sm font-medium transition-colors ${
                    activo
                      ? 'border-[#00BFFF] text-[#00BFFF]'
                      : 'border-transparent text-[#A0A0AB] hover:text-[#F2EFE9]'
                  }`}
                >
                  {tab.label}
                </button>
              );
            })}
          </nav>

          <button
            type="button"
            onClick={() => setVisorAndroidAbierto(true)}
            className="inline-flex min-h-[38px] shrink-0 items-center gap-2 whitespace-nowrap rounded-lg bg-[#8B0000] px-3.5 py-1.5 text-xs font-medium text-[#F2EFE9] transition-colors hover:bg-[#A41414]"
          >
            <Code2 className="h-3.5 w-3.5" aria-hidden="true" />
            Código Android
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-7xl space-y-6 px-4 py-6 sm:px-6">
        <TurnBanner turno={estado.turno} onResetPartida={manejarReiniciarPartida} />

        {pestana === 'estado' && (
          <EstadoScreen
            jugadores={estado.jugadores}
            onGuardarNota={manejarGuardarNota}
          />
        )}

        {pestana === 'mapa' && <MapaScreen habitaciones={catalogo.habitaciones} />}

        {pestana === 'biblioteca' && (
          <BibliotecaScreen
            libros={libros}
            lecturasPorLibro={estado.lecturasPorLibro}
            onRegistrarLectura={manejarRegistrarLectura}
          />
        )}

        {pestana === 'gm' && (
          <GmPanelScreen
            jugadores={estado.jugadores}
            catalogo={catalogo}
            jugadorSeleccionadoId={jugadorSeleccionadoId}
            ultimoResultado={ultimoResultado}
            historialAcciones={estado.historialAcciones}
            modeloIaCargado={modeloIaCargado}
            nombreModeloIa={nombreModeloIa}
            errorModeloIa={errorModeloIa}
            narracionActual={narracionActual}
            onSeleccionarJugador={(id) => {
              setJugadorSeleccionadoId(id);
              setUltimoResultado(null);
            }}
            onCargarModeloIa={manejarCargarModeloIa}
            onDescargarModeloIa={manejarDescargarModeloIa}
            onLimpiarErrorModeloIa={() => setErrorModeloIa(null)}
            onValidarYEnviar={manejarValidarYEnviar}
            onAvanzarTurno={manejarAvanzarTurno}
          />
        )}
      </main>

      <nav
        aria-label="Barra inferior móvil"
        className="fixed right-0 bottom-0 left-0 z-40 grid h-16 grid-cols-4 border-t border-[#2B2B33] bg-[#151518]/95 backdrop-blur-md md:hidden"
      >
        {tabs.map((tab) => {
          const Icono = tab.icon;
          const activo = pestana === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setPestana(tab.id)}
              className={`flex min-h-[44px] flex-col items-center justify-center gap-1 text-[11px] font-medium transition-colors ${
                activo ? 'text-[#00BFFF]' : 'text-[#A0A0AB]'
              }`}
            >
              <Icono className="h-5 w-5" aria-hidden="true" />
              <span className="whitespace-nowrap">{tab.shortLabel}</span>
            </button>
          );
        })}
      </nav>

      <AndroidSourceModal
        abierto={visorAndroidAbierto}
        onCerrar={() => setVisorAndroidAbierto(false)}
      />
    </div>
  );
}

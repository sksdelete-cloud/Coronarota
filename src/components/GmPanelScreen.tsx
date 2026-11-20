import { useRef, useState } from 'react';
import { FolderOpen, X } from 'lucide-react';
import type { Accion, CatalogoMundo, Jugador, ResultadoValidacion } from '../types/game';

interface GmPanelScreenProps {
  jugadores: Jugador[];
  catalogo: CatalogoMundo;
  jugadorSeleccionadoId: string;
  ultimoResultado: ResultadoValidacion | null;
  historialAcciones: Accion[];
  modeloIaCargado: boolean;
  nombreModeloIa: string | null;
  errorModeloIa: string | null;
  narracionActual: string;
  onSeleccionarJugador: (id: string) => void;
  onCargarModeloIa: (file: File) => void;
  onDescargarModeloIa: () => void;
  onLimpiarErrorModeloIa: () => void;
  onValidarYEnviar: (texto: string) => boolean;
  onAvanzarTurno: () => void;
}

export function GmPanelScreen({
  jugadores,
  catalogo,
  jugadorSeleccionadoId,
  ultimoResultado,
  historialAcciones,
  modeloIaCargado,
  nombreModeloIa,
  errorModeloIa,
  narracionActual,
  onSeleccionarJugador,
  onCargarModeloIa,
  onDescargarModeloIa,
  onLimpiarErrorModeloIa,
  onValidarYEnviar,
  onAvanzarTurno,
}: GmPanelScreenProps) {
  const [textoAccion, setTextoAccion] = useState('');
  const inputArchivoRef = useRef<HTMLInputElement | null>(null);
  const jugadorActivo = jugadores.find((j) => j.id === jugadorSeleccionadoId) ?? jugadores[0];
  const habitacionActual = catalogo.habitaciones.find(
    (h) => h.nombre === jugadorActivo?.habitacion
  );

  const manejarEnvio = (e: React.FormEvent) => {
    e.preventDefault();
    const valido = onValidarYEnviar(textoAccion);
    if (valido) {
      setTextoAccion('');
    }
  };

  const manejarSeleccionArchivo = (e: React.ChangeEvent<HTMLInputElement>) => {
    const archivo = e.target.files?.[0];
    if (archivo) {
      onCargarModeloIa(archivo);
    }
    e.target.value = '';
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-serif-title text-3xl font-bold tracking-tight text-[#F2EFE9]">
          Panel del GM
        </h1>
        <p className="mt-1 text-sm text-[#A0A0AB]">
          Traduce acciones en lenguaje natural con el modelo .gguf local, valida contra el catálogo y genera la narración
        </p>
      </div>

      <section
        aria-label="Configuración del modelo IA local"
        className={`rounded-xl border bg-[#151518] p-4 sm:p-5 ${
          modeloIaCargado ? 'border-[#3FA36C]/60' : 'border-[#2B2B33]'
        }`}
      >
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p
              className={`font-serif-title text-base font-bold ${
                modeloIaCargado ? 'text-[#3FA36C]' : 'text-[#A0A0AB]'
              }`}
            >
              {modeloIaCargado ? 'Modelo IA: Cargado' : 'Modelo IA: No cargado'}
            </p>
            <p className="mt-0.5 text-xs text-[#A0A0AB]">
              {modeloIaCargado && nombreModeloIa
                ? `Archivo activo: ${nombreModeloIa}`
                : 'Sin archivo .gguf cargado — validación estricta Fase 1 activa'}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <input
              ref={inputArchivoRef}
              type="file"
              accept=".gguf"
              onChange={manejarSeleccionArchivo}
              className="hidden"
              aria-label="Seleccionar archivo .gguf"
            />
            <button
              type="button"
              onClick={() => inputArchivoRef.current?.click()}
              className="inline-flex min-h-[42px] items-center gap-2 whitespace-nowrap rounded-lg border border-[#00BFFF] bg-transparent px-4 py-2 text-xs font-medium text-[#00BFFF] transition-colors hover:bg-[#00BFFF]/10"
            >
              <FolderOpen className="h-4 w-4" aria-hidden="true" />
              Cargar Modelo IA (.gguf)
            </button>

            {modeloIaCargado && (
              <button
                type="button"
                onClick={onDescargarModeloIa}
                className="inline-flex min-h-[42px] items-center gap-1.5 whitespace-nowrap rounded-lg border border-[#2B2B33] bg-[#0D0D0D] px-3 py-2 text-xs font-medium text-[#A0A0AB] transition-colors hover:text-[#F2EFE9]"
              >
                <X className="h-3.5 w-3.5" aria-hidden="true" />
                Descargar
              </button>
            )}
          </div>
        </div>

        {errorModeloIa && (
          <div
            role="alert"
            className="mt-3 flex items-center justify-between gap-3 rounded-lg border border-[#CF3A3A] bg-[#211214] px-3.5 py-2.5 text-xs text-[#CF3A3A]"
          >
            <span>{errorModeloIa}</span>
            <button
              type="button"
              onClick={onLimpiarErrorModeloIa}
              className="shrink-0 font-medium text-[#F2EFE9] underline"
            >
              Cerrar
            </button>
          </div>
        )}
      </section>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1.35fr_1fr]">
        <div className="space-y-5">
          <div className="rounded-xl border border-[#2B2B33] bg-[#151518] p-5">
            <p className="text-xs font-medium text-[#A0A0AB]">
              Jugador que ejecuta la acción
            </p>
            <div className="mt-2.5 flex flex-wrap gap-2">
              {jugadores.map((j) => {
                const seleccionado = j.id === jugadorSeleccionadoId;
                return (
                  <button
                    key={j.id}
                    type="button"
                    onClick={() => onSeleccionarJugador(j.id)}
                    className={`min-h-[38px] whitespace-nowrap rounded-lg border px-3 py-1.5 text-xs font-medium transition-colors ${
                      seleccionado
                        ? 'border-[#8B0000] bg-[#8B0000] text-[#F2EFE9]'
                        : 'border-[#2B2B33] bg-[#0D0D0D] text-[#A0A0AB] hover:text-[#F2EFE9]'
                    }`}
                  >
                    {j.nombre}
                    {j.estado !== 'ACTIVO' ? ` (${j.estado.toLowerCase()})` : ''}
                  </button>
                );
              })}
            </div>

            <form onSubmit={manejarEnvio} className="mt-5 space-y-4">
              <div>
                <label
                  htmlFor="accion-gm-textarea"
                  className="block text-sm font-medium text-[#F2EFE9]"
                >
                  Acción del jugador
                </label>
                <textarea
                  id="accion-gm-textarea"
                  rows={4}
                  value={textoAccion}
                  onChange={(e) => setTextoAccion(e.target.value)}
                  placeholder={
                    modeloIaCargado
                      ? 'Ej.: Isaac lee su diario · Isaac camina al Pasillo Norte · Mara revisa el camarin'
                      : 'Ej.: Isaac leer Diario personal-Isaac · Isaac mover a Pasillo Norte · Mara ejercitarse'
                  }
                  className="mt-2 w-full rounded-xl border border-[#2B2B33] bg-[#0D0D0D] p-3.5 text-sm text-[#F2EFE9] placeholder:text-[#6C6C78] focus:border-[#00BFFF] focus:outline-none"
                />
              </div>

              <div className="flex flex-wrap items-center gap-3">
                <button
                  type="submit"
                  className="min-h-[44px] flex-1 whitespace-nowrap rounded-lg bg-[#8B0000] px-5 py-2.5 text-sm font-medium text-[#F2EFE9] transition-colors hover:bg-[#A41414]"
                >
                  Validar y Enviar
                </button>
                <button
                  type="button"
                  onClick={onAvanzarTurno}
                  className="min-h-[44px] whitespace-nowrap rounded-lg border border-[#00BFFF] bg-transparent px-5 py-2.5 text-sm font-medium text-[#00BFFF] transition-colors hover:bg-[#00BFFF]/10"
                >
                  Avanzar Turno
                </button>
              </div>
            </form>
          </div>

          {ultimoResultado && (
            <div
              role="status"
              aria-live="polite"
              className={`rounded-xl border p-4 ${
                ultimoResultado.exitoso
                  ? 'border-[#3FA36C] bg-[#151518]'
                  : 'border-[#CF3A3A] bg-[#1A1214]'
              }`}
            >
              <p
                className={`font-serif-title text-lg font-bold ${
                  ultimoResultado.exitoso ? 'text-[#3FA36C]' : 'text-[#CF3A3A]'
                }`}
              >
                {ultimoResultado.exitoso ? 'Acción registrada' : 'Validación rechazada'}
              </p>
              {ultimoResultado.comandoTraducido && (
                <p className="font-mono-data mt-1 text-xs text-[#00BFFF]">
                  Comando IA: {ultimoResultado.comandoTraducido}
                </p>
              )}
              <p className="mt-1 text-sm text-[#F2EFE9]">{ultimoResultado.razon}</p>
            </div>
          )}

          <section
            aria-label="Narración pendiente"
            className="rounded-xl border border-[#2B2B33] bg-[#151518] p-5"
          >
            <h2 className="font-serif-title text-lg font-bold text-[#00BFFF]">
              Narración pendiente
            </h2>
            <div
              className={`mt-2 space-y-3 whitespace-pre-line text-sm leading-relaxed ${
                narracionActual === 'La IA narrará aquí' ? 'text-[#A0A0AB]' : 'text-[#F2EFE9]'
              }`}
            >
              {narracionActual}
            </div>
          </section>
        </div>

        <aside className="space-y-5">
          {jugadorActivo && habitacionActual && (
            <div className="rounded-xl border border-[#2B2B33] bg-[#151518] p-5">
              <h2 className="font-serif-title text-xl font-bold text-[#F2EFE9]">
                Contexto en {habitacionActual.nombre}
              </h2>
              <p className="mt-1 text-xs text-[#A0A0AB]">
                Entidades permitidas para {jugadorActivo.nombre} en este turno
              </p>

              <div className="mt-4 space-y-3 border-t border-[#2B2B33] pt-3 text-xs">
                <div>
                  <span className="font-medium text-[#00BFFF]">Salidas conectadas: </span>
                  <span className="text-[#F2EFE9]">{habitacionActual.salidas.join(', ')}</span>
                </div>
                <div>
                  <span className="font-medium text-[#00BFFF]">Jugadores presentes: </span>
                  <span className="text-[#F2EFE9]">
                    {habitacionActual.jugadoresPresentes.length > 0
                      ? habitacionActual.jugadoresPresentes.join(', ')
                      : 'Ninguno'}
                  </span>
                </div>
                <div>
                  <span className="font-medium text-[#00BFFF]">Objetos visibles en sala: </span>
                  <span className="text-[#F2EFE9]">
                    {habitacionActual.objetosVisibles.length > 0
                      ? habitacionActual.objetosVisibles.join(', ')
                      : 'Ninguno'}
                  </span>
                </div>
                <div>
                  <span className="font-medium text-[#00BFFF]">Inventario del jugador: </span>
                  <span className="text-[#F2EFE9]">
                    {jugadorActivo.inventario.length > 0
                      ? jugadorActivo.inventario.join(', ')
                      : 'Vacío'}
                  </span>
                </div>
              </div>
            </div>
          )}

          <div className="rounded-xl border border-[#2B2B33] bg-[#151518] p-5">
            <h2 className="font-serif-title text-xl font-bold text-[#F2EFE9]">
              Bitácora de acciones
            </h2>
            {historialAcciones.length === 0 ? (
              <p className="mt-2 text-sm text-[#A0A0AB]">
                Aún no hay acciones registradas en esta sesión.
              </p>
            ) : (
              <ul className="mt-3 space-y-2.5">
                {historialAcciones.slice(0, 6).map((acc) => (
                  <li
                    key={acc.id}
                    className="rounded-lg border border-[#2B2B33] bg-[#0D0D0D] p-3 text-xs"
                  >
                    <p className="font-mono-data text-[#00BFFF]">
                      Turno {acc.turnoGlobal} · {acc.jugadorNombre} · {acc.tipo}
                    </p>
                    <p className="mt-1 text-sm text-[#F2EFE9]">{acc.descripcion}</p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </aside>
      </div>
    </div>
  );
}

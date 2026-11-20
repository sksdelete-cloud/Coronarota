import { useState } from 'react';
import { X } from 'lucide-react';
import { contarObjetosUtiles } from '../logic/validadorAcciones';
import type { EstadoJugador, Jugador } from '../types/game';

interface EstadoScreenProps {
  jugadores: Jugador[];
  onGuardarNota: (jugadorId: string, nota: string) => void;
}

function colorBarraHp(hpActual: number, hpMaximo: number, estado: EstadoJugador): string {
  if (estado === 'MUERTO' || hpActual <= 0) return 'bg-[#8B0000]';
  const ratio = hpMaximo > 0 ? hpActual / hpMaximo : 0;
  if (ratio <= 0.3) return 'bg-[#CF3A3A]';
  if (ratio <= 0.55) return 'bg-[#D99B26]';
  return 'bg-[#3FA36C]';
}

function colorTextoEstado(estado: EstadoJugador): string {
  if (estado === 'ACTIVO') return 'text-[#00BFFF]';
  if (estado === 'INCONSCIENTE') return 'text-[#D99B26]';
  return 'text-[#CF3A3A]';
}

export function EstadoScreen({ jugadores, onGuardarNota }: EstadoScreenProps) {
  const [filtroEstado, setFiltroEstado] = useState<'TODOS' | EstadoJugador>('TODOS');
  const [jugadorDetalleId, setJugadorDetalleId] = useState<string | null>(null);
  const [notaDraft, setNotaDraft] = useState('');

  const jugadoresFiltrados =
    filtroEstado === 'TODOS'
      ? jugadores
      : jugadores.filter((j) => j.estado === filtroEstado);

  const jugadorDetalle = jugadores.find((j) => j.id === jugadorDetalleId) ?? null;

  const abrirDetalle = (j: Jugador) => {
    setJugadorDetalleId(j.id);
    setNotaDraft(j.notas);
  };

  const activos = jugadores.filter((j) => j.estado === 'ACTIVO').length;
  const inconscientes = jugadores.filter((j) => j.estado === 'INCONSCIENTE').length;
  const muertos = jugadores.filter((j) => j.estado === 'MUERTO').length;

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <h1 className="font-serif-title text-3xl font-bold tracking-tight text-[#F2EFE9]">
            Estado de los 12 Jugadores
          </h1>
          <p className="mt-1 text-sm text-[#A0A0AB]">
            {activos} activos · {inconscientes} inconscientes · {muertos} muertos · Toca un jugador para abrir EXP, inventario y notas
          </p>
        </div>

        <div
          role="tablist"
          aria-label="Filtrar jugadores por estado"
          className="inline-flex max-w-full shrink-0 items-center gap-1 overflow-x-auto rounded-lg border border-[#2B2B33] bg-[#151518] p-1"
        >
          {(['TODOS', 'ACTIVO', 'INCONSCIENTE', 'MUERTO'] as const).map((opcion) => (
            <button
              key={opcion}
              type="button"
              role="tab"
              aria-selected={filtroEstado === opcion}
              onClick={() => setFiltroEstado(opcion)}
              className={`min-h-[36px] whitespace-nowrap rounded-md px-3 py-1.5 text-xs font-medium transition-colors ${
                filtroEstado === opcion
                  ? 'bg-[#8B0000] text-[#F2EFE9]'
                  : 'text-[#A0A0AB] hover:text-[#F2EFE9]'
              }`}
            >
              {opcion === 'TODOS' ? 'Todos (12)' : opcion}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        {jugadoresFiltrados.map((jugador) => {
          const pct = jugador.hpMaximo > 0 ? Math.max(0, Math.min(100, (jugador.hpActual / jugador.hpMaximo) * 100)) : 0;
          const utiles = contarObjetosUtiles(jugador.inventario);

          return (
            <button
              key={jugador.id}
              type="button"
              onClick={() => abrirDetalle(jugador)}
              className="flex flex-col justify-between rounded-xl border border-[#2B2B33] bg-[#151518] p-4 text-left transition-colors hover:border-[#00BFFF]/60 focus-visible:outline-2 focus-visible:outline-[#00BFFF]"
            >
              <div className="w-full space-y-3">
                <div className="flex items-baseline justify-between gap-2">
                  <h2 className="font-serif-title text-xl font-bold text-[#F2EFE9]">
                    {jugador.nombre}
                  </h2>
                  <span className="truncate text-xs text-[#A0A0AB]">
                    {jugador.ubicacionVisible ? jugador.habitacion : 'Ubicación desconocida'}
                  </span>
                </div>

                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-mono-data font-medium text-[#F2EFE9]">
                      HP {jugador.hpActual}/{jugador.hpMaximo}
                    </span>
                    <span className={`font-mono-data font-semibold ${colorTextoEstado(jugador.estado)}`}>
                      {jugador.estado}
                    </span>
                  </div>
                  <div className="h-2 w-full overflow-hidden rounded-full bg-[#24242A]">
                    <div
                      className={`h-full transition-all duration-200 ${colorBarraHp(
                        jugador.hpActual,
                        jugador.hpMaximo,
                        jugador.estado
                      )}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              </div>

              <div className="mt-4 w-full border-t border-[#2B2B33] pt-3">
                <p className="text-xs text-[#A0A0AB]">
                  Inventario ({utiles}/3 útiles)
                </p>
                <p className="mt-0.5 truncate text-sm text-[#F2EFE9]">
                  {jugador.inventario.length === 0 ? 'Sin objetos' : jugador.inventario.join(' · ')}
                </p>
              </div>
            </button>
          );
        })}
      </div>

      {jugadorDetalle && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="detalle-jugador-titulo"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm"
        >
          <div className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-[#2B2B33] bg-[#151518] p-6">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2
                  id="detalle-jugador-titulo"
                  className="font-serif-title text-2xl font-bold text-[#F2EFE9]"
                >
                  {jugadorDetalle.nombre}
                </h2>
                <p className="mt-0.5 text-xs text-[#A0A0AB]">
                  <span className={colorTextoEstado(jugadorDetalle.estado)}>
                    {jugadorDetalle.estado}
                  </span>{' '}
                  · {jugadorDetalle.ubicacionVisible ? jugadorDetalle.habitacion : 'Ubicación desconocida'}
                </p>
              </div>
              <button
                type="button"
                aria-label={`Cerrar detalle de ${jugadorDetalle.nombre}`}
                onClick={() => setJugadorDetalleId(null)}
                className="flex min-h-[40px] min-w-[40px] items-center justify-center rounded-lg border border-[#2B2B33] text-[#A0A0AB] hover:text-[#F2EFE9]"
              >
                <X className="h-4 w-4" aria-hidden="true" />
              </button>
            </div>

            <div className="mt-5 space-y-5">
              <div>
                <div className="flex items-center justify-between text-xs">
                  <span className="font-mono-data text-[#F2EFE9]">
                    Vitalidad: {jugadorDetalle.hpActual}/{jugadorDetalle.hpMaximo} HP
                  </span>
                  <span className="text-[#A0A0AB]">
                    {contarObjetosUtiles(jugadorDetalle.inventario)}/3 objetos útiles
                  </span>
                </div>
                <div className="mt-1.5 h-2.5 w-full overflow-hidden rounded-full bg-[#24242A]">
                  <div
                    className={`h-full ${colorBarraHp(
                      jugadorDetalle.hpActual,
                      jugadorDetalle.hpMaximo,
                      jugadorDetalle.estado
                    )}`}
                    style={{
                      width: `${Math.max(
                        0,
                        Math.min(100, (jugadorDetalle.hpActual / jugadorDetalle.hpMaximo) * 100)
                      )}%`,
                    }}
                  />
                </div>
              </div>

              <div className="border-t border-[#2B2B33] pt-4">
                <h3 className="font-serif-title text-lg font-semibold text-[#F2EFE9]">
                  Objetos (Llave y Diario no cuentan)
                </h3>
                <p className="mt-1 text-sm text-[#A0A0AB]">
                  {jugadorDetalle.inventario.length === 0
                    ? 'No porta objetos.'
                    : jugadorDetalle.inventario.join(' · ')}
                </p>
              </div>

              <div className="border-t border-[#2B2B33] pt-4">
                <h3 className="font-serif-title text-lg font-semibold text-[#F2EFE9]">
                  EXP por conocimiento
                </h3>
                {Object.keys(jugadorDetalle.exp).length === 0 ? (
                  <p className="mt-1 text-sm text-[#A0A0AB]">
                    Sin EXP registrada todavía.
                  </p>
                ) : (
                  <div className="mt-2 grid grid-cols-1 gap-2 sm:grid-cols-2">
                    {Object.entries(jugadorDetalle.exp).map(([tomo, valor]) => (
                      <div
                        key={tomo}
                        className="flex items-center justify-between rounded-lg border border-[#2B2B33] bg-[#0D0D0D] px-3 py-2 text-xs"
                      >
                        <span className="text-[#F2EFE9]">{tomo}</span>
                        <span className="font-mono-data text-[#00BFFF]">{valor}/100</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="border-t border-[#2B2B33] pt-4">
                <label
                  htmlFor="nota-jugador-input"
                  className="block font-serif-title text-lg font-semibold text-[#F2EFE9]"
                >
                  Notas del GM
                </label>
                <textarea
                  id="nota-jugador-input"
                  rows={3}
                  value={notaDraft}
                  onChange={(e) => setNotaDraft(e.target.value)}
                  className="mt-2 w-full rounded-lg border border-[#2B2B33] bg-[#0D0D0D] p-3 text-sm text-[#F2EFE9] focus:border-[#00BFFF] focus:outline-none"
                />
              </div>
            </div>

            <div className="mt-6 flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setJugadorDetalleId(null)}
                className="min-h-[42px] rounded-lg border border-[#2B2B33] px-4 py-2 text-sm text-[#A0A0AB] hover:text-[#F2EFE9]"
              >
                Cerrar
              </button>
              <button
                type="button"
                onClick={() => {
                  onGuardarNota(jugadorDetalle.id, notaDraft);
                  setJugadorDetalleId(null);
                }}
                className="min-h-[42px] rounded-lg bg-[#8B0000] px-4 py-2 text-sm font-medium text-[#F2EFE9] hover:bg-[#A41414]"
              >
                Guardar nota
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

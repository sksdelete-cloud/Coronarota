import { useMemo, useState } from 'react';
import { DoorOpen, X } from 'lucide-react';
import type { Habitacion } from '../types/game';

interface MapaScreenProps {
  habitaciones: Habitacion[];
}

export function MapaScreen({ habitaciones }: MapaScreenProps) {
  const zonas = useMemo(() => {
    const mapa = new Map<string, Habitacion[]>();
    for (const h of habitaciones) {
      const lista = mapa.get(h.zona) ?? [];
      lista.push(h);
      mapa.set(h.zona, lista);
    }
    return Array.from(mapa.entries());
  }, [habitaciones]);

  const [zonaFiltro, setZonaFiltro] = useState<string>('TODAS');
  const [habitacionDetalle, setHabitacionDetalle] = useState<Habitacion | null>(null);

  const zonasVisibles =
    zonaFiltro === 'TODAS' ? zonas : zonas.filter(([z]) => z === zonaFiltro);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-serif-title text-3xl font-bold tracking-tight text-[#F2EFE9]">
          Mapa de la Mansión
        </h1>
        <p className="mt-1 text-sm text-[#A0A0AB]">
          {habitaciones.length} estancias agrupadas por zona · Toca cualquier habitación para inspeccionar salidas disponibles
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[240px_1fr]">
        <aside className="space-y-1.5">
          <p className="mb-2 text-xs font-medium text-[#A0A0AB]">Filtrar por zona</p>
          <button
            type="button"
            onClick={() => setZonaFiltro('TODAS')}
            className={`flex min-h-[40px] w-full items-center justify-between rounded-lg px-3 py-2 text-left text-sm transition-colors ${
              zonaFiltro === 'TODAS'
                ? 'bg-[#8B0000] font-medium text-[#F2EFE9]'
                : 'bg-[#151518] text-[#A0A0AB] hover:text-[#F2EFE9]'
            }`}
          >
            <span>Todas las zonas</span>
            <span className="font-mono-data text-xs">{habitaciones.length}</span>
          </button>
          {zonas.map(([zona, salas]) => (
            <button
              key={zona}
              type="button"
              onClick={() => setZonaFiltro(zona)}
              className={`flex min-h-[40px] w-full items-center justify-between rounded-lg px-3 py-2 text-left text-sm transition-colors ${
                zonaFiltro === zona
                  ? 'bg-[#8B0000] font-medium text-[#F2EFE9]'
                  : 'bg-[#151518] text-[#A0A0AB] hover:text-[#F2EFE9]'
              }`}
            >
              <span className="truncate">{zona}</span>
              <span className="font-mono-data text-xs">{salas.length}</span>
            </button>
          ))}
        </aside>

        <div className="space-y-8">
          {zonasVisibles.map(([zona, salas]) => (
            <section key={zona} className="space-y-3">
              <h2 className="font-serif-title border-b border-[#2B2B33] pb-2 text-xl font-bold text-[#00BFFF]">
                {zona}
              </h2>
              <div
                className={
                  salas.length === 1
                    ? 'grid grid-cols-1 gap-3'
                    : 'grid grid-cols-1 gap-3 sm:grid-cols-2'
                }
              >
                {salas.map((hab) => (
                  <button
                    key={hab.id}
                    type="button"
                    onClick={() => setHabitacionDetalle(hab)}
                    className="flex flex-col justify-between rounded-xl border border-[#2B2B33] bg-[#151518] p-4 text-left transition-colors hover:border-[#00BFFF]/60 focus-visible:outline-2 focus-visible:outline-[#00BFFF]"
                  >
                    <div>
                      <div className="flex items-center justify-between gap-2">
                        <h3 className="font-serif-title text-lg font-bold text-[#F2EFE9]">
                          {hab.nombre}
                        </h3>
                        <span className="font-mono-data text-xs text-[#A0A0AB]">
                          {hab.salidas.length} salidas
                        </span>
                      </div>
                      <p className="mt-1.5 text-sm leading-relaxed text-[#A0A0AB]">
                        {hab.descripcion}
                      </p>
                    </div>

                    <div className="mt-4 space-y-1 border-t border-[#2B2B33] pt-3 text-xs">
                      <p className={hab.jugadoresPresentes.length > 0 ? 'text-[#00BFFF]' : 'text-[#A0A0AB]'}>
                        Presentes:{' '}
                        {hab.jugadoresPresentes.length === 0
                          ? 'Ninguno'
                          : hab.jugadoresPresentes.join(', ')}
                      </p>
                      <p className="text-[#F2EFE9]">
                        Objetos:{' '}
                        {hab.objetosVisibles.length === 0
                          ? 'Sin objetos visibles'
                          : hab.objetosVisibles.join(', ')}
                      </p>
                    </div>
                  </button>
                ))}
              </div>
            </section>
          ))}
        </div>
      </div>

      {habitacionDetalle && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="detalle-habitacion-titulo"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm"
        >
          <div className="w-full max-w-lg rounded-2xl border border-[#2B2B33] bg-[#151518] p-6">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2
                  id="detalle-habitacion-titulo"
                  className="font-serif-title text-2xl font-bold text-[#F2EFE9]"
                >
                  {habitacionDetalle.nombre}
                </h2>
                <p className="mt-0.5 text-xs text-[#A0A0AB]">
                  Zona: {habitacionDetalle.zona}
                </p>
              </div>
              <button
                type="button"
                aria-label={`Cerrar detalle de ${habitacionDetalle.nombre}`}
                onClick={() => setHabitacionDetalle(null)}
                className="flex min-h-[40px] min-w-[40px] items-center justify-center rounded-lg border border-[#2B2B33] text-[#A0A0AB] hover:text-[#F2EFE9]"
              >
                <X className="h-4 w-4" aria-hidden="true" />
              </button>
            </div>

            <p className="mt-4 text-sm leading-relaxed text-[#F2EFE9]">
              {habitacionDetalle.descripcion}
            </p>

            <div className="mt-5 space-y-4 border-t border-[#2B2B33] pt-4">
              <div>
                <h3 className="flex items-center gap-2 font-serif-title text-lg font-semibold text-[#00BFFF]">
                  <DoorOpen className="h-4 w-4" aria-hidden="true" />
                  Salidas disponibles
                </h3>
                <p className="mt-1.5 text-sm text-[#F2EFE9]">
                  {habitacionDetalle.salidas.join(' · ')}
                </p>
              </div>

              <div>
                <h3 className="font-serif-title text-base font-semibold text-[#F2EFE9]">
                  Jugadores presentes
                </h3>
                <p className="mt-1 text-sm text-[#A0A0AB]">
                  {habitacionDetalle.jugadoresPresentes.length === 0
                    ? 'Ningún jugador activo en esta estancia.'
                    : habitacionDetalle.jugadoresPresentes.join(', ')}
                </p>
              </div>

              <div>
                <h3 className="font-serif-title text-base font-semibold text-[#F2EFE9]">
                  Objetos visibles
                </h3>
                <p className="mt-1 text-sm text-[#A0A0AB]">
                  {habitacionDetalle.objetosVisibles.length === 0
                    ? 'Ninguno.'
                    : habitacionDetalle.objetosVisibles.join(', ')}
                </p>
              </div>
            </div>

            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={() => setHabitacionDetalle(null)}
                className="min-h-[42px] rounded-lg bg-[#8B0000] px-5 py-2 text-sm font-medium text-[#F2EFE9] hover:bg-[#A41414]"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

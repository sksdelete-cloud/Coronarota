import { useState } from 'react';
import { ArrowLeft, BookOpen } from 'lucide-react';
import { calcularExpLibro } from '../logic/validadorAcciones';
import type { Capitulo, Libro } from '../types/game';

interface BibliotecaScreenProps {
  libros: Libro[];
  lecturasPorLibro: Record<string, number>;
  onRegistrarLectura: (libroId: string) => void;
}

export function BibliotecaScreen({
  libros,
  lecturasPorLibro,
  onRegistrarLectura,
}: BibliotecaScreenProps) {
  const [libroSeleccionado, setLibroSeleccionado] = useState<Libro | null>(null);
  const [capituloSeleccionado, setCapituloSeleccionado] = useState<Capitulo | null>(null);

  if (libroSeleccionado && capituloSeleccionado) {
    const lecturas = lecturasPorLibro[libroSeleccionado.id] ?? 0;
    const exp = calcularExpLibro(lecturas);

    return (
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <button
            type="button"
            onClick={() => setCapituloSeleccionado(null)}
            className="inline-flex min-h-[42px] items-center gap-2 rounded-lg border border-[#2B2B33] bg-[#151518] px-3.5 py-2 text-sm text-[#00BFFF] hover:border-[#00BFFF]"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            Volver a capítulos
          </button>
          <span className="font-mono-data text-xs text-[#A0A0AB]">
            EXP: {exp}/100 · {lecturas}/3 lecturas
          </span>
        </div>

        <header className="border-b border-[#2B2B33] pb-4">
          <p className="text-xs text-[#A0A0AB]">
            {libroSeleccionado.titulo} · {libroSeleccionado.categoria}
          </p>
          <h1 className="font-serif-title mt-1 text-3xl font-bold text-[#F2EFE9]">
            {capituloSeleccionado.titulo}
          </h1>
        </header>

        <div className="space-y-5">
          {capituloSeleccionado.paginas.map((pagina) => (
            <article
              key={pagina.titulo}
              className="rounded-xl border border-[#2B2B33] bg-[#151518] p-6"
            >
              <h2 className="font-serif-title text-xl font-semibold text-[#00BFFF]">
                {pagina.titulo}
              </h2>
              <p className="mt-3 whitespace-pre-line text-base leading-relaxed text-[#F2EFE9]">
                {pagina.texto}
              </p>
            </article>
          ))}
        </div>

        <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-[#2B2B33] bg-[#151518] p-5">
          <p className="text-sm text-[#A0A0AB]">
            Nota: cada tomo se desbloquea completamente al leerlo 3 veces (100/100 EXP).
          </p>
          <button
            type="button"
            disabled={lecturas >= 3}
            onClick={() => onRegistrarLectura(libroSeleccionado.id)}
            className={`min-h-[44px] rounded-lg px-5 py-2.5 text-sm font-medium transition-colors ${
              lecturas >= 3
                ? 'cursor-not-allowed border border-[#2B2B33] bg-[#1D1D22] text-[#A0A0AB]'
                : 'bg-[#8B0000] text-[#F2EFE9] hover:bg-[#A41414]'
            }`}
          >
            {lecturas >= 3
              ? 'Conocimiento desbloqueado (3/3)'
              : `Registrar lectura completa (${lecturas}/3)`}
          </button>
        </div>
      </div>
    );
  }

  if (libroSeleccionado) {
    const lecturas = lecturasPorLibro[libroSeleccionado.id] ?? 0;
    const exp = calcularExpLibro(lecturas);

    return (
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <button
            type="button"
            onClick={() => setLibroSeleccionado(null)}
            className="inline-flex min-h-[42px] items-center gap-2 rounded-lg border border-[#2B2B33] bg-[#151518] px-3.5 py-2 text-sm text-[#00BFFF] hover:border-[#00BFFF]"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            Volver a los 6 tomos
          </button>
          <span className="font-mono-data text-xs text-[#00BFFF]">
            EXP: {exp}/100 · Se desbloquea al leerlo 3 veces
          </span>
        </div>

        <div>
          <p className="text-xs text-[#A0A0AB]">{libroSeleccionado.categoria}</p>
          <h1 className="font-serif-title mt-1 text-3xl font-bold text-[#F2EFE9]">
            {libroSeleccionado.titulo}
          </h1>
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {libroSeleccionado.capitulos.map((cap) => (
            <button
              key={cap.numero}
              type="button"
              onClick={() => setCapituloSeleccionado(cap)}
              className="flex flex-col justify-between rounded-xl border border-[#2B2B33] bg-[#151518] p-5 text-left transition-colors hover:border-[#00BFFF]/60"
            >
              <div>
                <h2 className="font-serif-title text-xl font-bold text-[#F2EFE9]">
                  {cap.titulo}
                </h2>
                <p className="mt-1 text-xs text-[#A0A0AB]">
                  {cap.paginas.length} {cap.paginas.length === 1 ? 'página' : 'páginas'}
                </p>
              </div>
              <span className="mt-4 inline-flex items-center gap-1.5 text-xs font-medium text-[#00BFFF]">
                <BookOpen className="h-3.5 w-3.5" aria-hidden="true" />
                Leer capítulo
              </span>
            </button>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-serif-title text-3xl font-bold tracking-tight text-[#F2EFE9]">
          Biblioteca de la Mansión
        </h1>
        <p className="mt-1 text-sm text-[#A0A0AB]">
          {libros.length} tomos iniciales cargados desde books.json · Cada tomo se desbloquea al leerlo 3 veces
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        {libros.map((libro) => {
          const lecturas = lecturasPorLibro[libro.id] ?? 0;
          const exp = calcularExpLibro(lecturas);

          return (
            <button
              key={libro.id}
              type="button"
              onClick={() => setLibroSeleccionado(libro)}
              className="flex flex-col justify-between rounded-xl border border-[#2B2B33] bg-[#151518] p-5 text-left transition-colors hover:border-[#00BFFF]/60 focus-visible:outline-2 focus-visible:outline-[#00BFFF]"
            >
              <div>
                <p className="text-xs text-[#A0A0AB]">
                  {libro.categoria} · {libro.capitulos.length} capítulos
                </p>
                <h2 className="font-serif-title mt-1.5 text-xl font-bold text-[#F2EFE9]">
                  {libro.titulo}
                </h2>
              </div>

              <div className="mt-5 border-t border-[#2B2B33] pt-3">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-mono-data font-semibold text-[#00BFFF]">
                    EXP: {exp}/100
                  </span>
                  <span className="text-[#A0A0AB]">{lecturas}/3 lecturas</span>
                </div>
                <p className="mt-1 text-xs text-[#A0A0AB]">
                  {lecturas >= 3
                    ? 'Conocimiento desbloqueado por completo.'
                    : 'Se desbloquea al leerlo 3 veces.'}
                </p>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}

import { RotateCcw } from 'lucide-react';
import { formatearIndicadorTurno } from '../logic/catalogoMundo';
import type { Turno } from '../types/game';

interface TurnBannerProps {
  turno: Turno;
  onResetPartida?: () => void;
}

export function TurnBanner({ turno, onResetPartida }: TurnBannerProps) {
  return (
    <section
      aria-label="Indicador de turno actual"
      className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-[#2B2B33] bg-[#151518] px-5 py-3.5"
    >
      <div className="flex flex-col gap-0.5">
        <p className="font-serif-title text-lg font-bold tracking-tight text-[#F2EFE9] sm:text-xl">
          {formatearIndicadorTurno(turno)}
        </p>
        <p className="font-mono-data text-xs text-[#00BFFF]">
          Acciones restantes en el turno: {turno.accionesRestantes} · Campaña local en curso
        </p>
      </div>

      {onResetPartida && (
        <button
          type="button"
          onClick={onResetPartida}
          className="inline-flex min-h-[40px] items-center gap-2 whitespace-nowrap rounded-lg border border-[#2B2B33] bg-[#1D1D22] px-3.5 py-2 text-xs font-medium text-[#A0A0AB] transition-colors hover:border-[#8B0000] hover:text-[#F2EFE9] focus-visible:outline-2 focus-visible:outline-[#00BFFF]"
        >
          <RotateCcw className="h-3.5 w-3.5" aria-hidden="true" />
          Reiniciar partida
        </button>
      )}
    </section>
  );
}

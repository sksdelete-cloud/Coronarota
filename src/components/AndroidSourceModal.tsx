import { useState } from 'react';
import { Check, Copy, Download, X } from 'lucide-react';
import { ANDROID_PROJECT_FILES } from '../data/androidFiles';

interface AndroidSourceModalProps {
  abierto: boolean;
  onCerrar: () => void;
}

export function AndroidSourceModal({ abierto, onCerrar }: AndroidSourceModalProps) {
  const [archivoActivoPath, setArchivoActivoPath] = useState(
    ANDROID_PROJECT_FILES[0].path
  );
  const [copiado, setCopiado] = useState(false);

  if (!abierto) return null;

  const archivoActivo =
    ANDROID_PROJECT_FILES.find((f) => f.path === archivoActivoPath) ??
    ANDROID_PROJECT_FILES[0];

  const copiarArchivo = async () => {
    try {
      await navigator.clipboard.writeText(archivoActivo.content);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 1800);
    } catch {
      setCopiado(false);
    }
  };

  const descargarArchivo = () => {
    const nombreArchivo = archivoActivo.path.split('/').pop() ?? 'archivo.txt';
    const blob = new Blob([archivoActivo.content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombreArchivo;
    enlace.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="android-code-modal-title"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-3 backdrop-blur-sm sm:p-6"
    >
      <div className="flex h-[88vh] w-full max-w-6xl flex-col overflow-hidden rounded-2xl border border-[#2B2B33] bg-[#121215]">
        <header className="flex items-center justify-between gap-4 border-b border-[#2B2B33] px-5 py-4">
          <div>
            <h2
              id="android-code-modal-title"
              className="font-serif-title text-xl font-bold text-[#F2EFE9]"
            >
              Proyecto Android Nativo (Kotlin + Jetpack Compose)
            </h2>
            <p className="text-xs text-[#A0A0AB]">
              {ANDROID_PROJECT_FILES.length} archivos en el repositorio · Single-activity · ViewModel + StateFlow + DataStore Preferences
            </p>
          </div>
          <button
            type="button"
            aria-label="Cerrar visor de código Android"
            onClick={onCerrar}
            className="flex min-h-[40px] min-w-[40px] items-center justify-center rounded-lg border border-[#2B2B33] text-[#A0A0AB] hover:text-[#F2EFE9]"
          >
            <X className="h-4 w-4" aria-hidden="true" />
          </button>
        </header>

        <div className="grid min-h-0 flex-1 grid-cols-1 md:grid-cols-[290px_1fr]">
          <aside className="max-h-48 overflow-y-auto border-b border-[#2B2B33] bg-[#0D0D0D] p-3 md:max-h-none md:border-r md:border-b-0">
            {(['Kotlin (App)', 'UI Compose', 'Assets JSON', 'Gradle y Manifest'] as const).map(
              (grupo) => (
                <div key={grupo} className="mb-4">
                  <p className="px-2 pb-1.5 text-xs font-semibold text-[#00BFFF]">{grupo}</p>
                  <div className="space-y-1">
                    {ANDROID_PROJECT_FILES.filter((f) => f.group === grupo).map((file) => {
                      const nombreCorto = file.path.split('/').pop() ?? file.path;
                      const activo = file.path === archivoActivo.path;
                      return (
                        <button
                          key={file.path}
                          type="button"
                          onClick={() => setArchivoActivoPath(file.path)}
                          className={`block w-full truncate rounded-lg px-2.5 py-2 text-left text-xs transition-colors ${
                            activo
                              ? 'bg-[#8B0000] font-medium text-[#F2EFE9]'
                              : 'text-[#A0A0AB] hover:bg-[#151518] hover:text-[#F2EFE9]'
                          }`}
                          title={file.path}
                        >
                          {nombreCorto}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )
            )}
          </aside>

          <div className="flex min-h-0 flex-col bg-[#0D0D0D]">
            <div className="flex items-center justify-between gap-3 border-b border-[#2B2B33] bg-[#151518] px-4 py-2.5">
              <span className="font-mono-data truncate text-xs text-[#F2EFE9]">
                {archivoActivo.path}
              </span>
              <div className="flex shrink-0 items-center gap-2">
                <button
                  type="button"
                  onClick={descargarArchivo}
                  className="inline-flex min-h-[34px] shrink-0 items-center gap-1.5 rounded-md border border-[#2B2B33] px-3 py-1 text-xs text-[#F2EFE9] hover:border-[#00BFFF]"
                >
                  <Download className="h-3.5 w-3.5" aria-hidden="true" />
                  Descargar
                </button>
                <button
                  type="button"
                  onClick={copiarArchivo}
                  className="inline-flex min-h-[34px] shrink-0 items-center gap-1.5 rounded-md border border-[#2B2B33] px-3 py-1 text-xs text-[#00BFFF] hover:border-[#00BFFF]"
                >
                  {copiado ? (
                    <>
                      <Check className="h-3.5 w-3.5" aria-hidden="true" />
                      Copiado
                    </>
                  ) : (
                    <>
                      <Copy className="h-3.5 w-3.5" aria-hidden="true" />
                      Copiar
                    </>
                  )}
                </button>
              </div>
            </div>
            <pre className="font-mono-data min-h-0 flex-1 overflow-auto p-4 text-xs leading-relaxed text-[#E6E3DC]">
              <code>{archivoActivo.content}</code>
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
}

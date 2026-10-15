import { Vibrant } from 'node-vibrant/browser'
import type { Palette, Swatch } from '@vibrant/color'

const SURFACE = [255, 255, 255]
const MIX = 0.16

function mixSurface(rgb: [number, number, number]) {
  const channel = (index: number) => Math.round(SURFACE[index] * (1 - MIX) + rgb[index] * MIX)
  return `rgb(${channel(0)}, ${channel(1)}, ${channel(2)})`
}

function dominant(palette: Palette): Swatch | null {
  return Object.values(palette)
    .filter((swatch): swatch is Swatch => swatch != null)
    .sort((a, b) => b.population - a.population)[0] ?? null
}

export async function highlightFromIcon(iconUrl: string): Promise<string | null> {
  const palette = await Vibrant.from(`/api/favicon?url=${encodeURIComponent(iconUrl)}`).getPalette()
  const swatch = palette.Vibrant || palette.DarkVibrant || palette.Muted || dominant(palette)
  return swatch ? mixSurface(swatch.rgb) : null
}

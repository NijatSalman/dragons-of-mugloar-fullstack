/** Colour scale for a success chance: green when safe, amber when uncertain, ember when dangerous. */
export type ChanceTone = 'success' | 'warning' | 'error'

export function chanceTone(successChance: number): ChanceTone {
  if (successChance >= 0.8) return 'success'
  if (successChance >= 0.5) return 'warning'
  return 'error'
}

export function percent(successChance: number): string {
  return `${Math.round(successChance * 100)}%`
}

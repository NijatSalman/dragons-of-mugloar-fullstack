/** Shapes of the backend's JSON responses. Field names match the Java response records one to one. */

export type GameOrigin = 'MANUAL' | 'AUTOPLAY'

export interface Game {
  gameId: string
  lives: number
  gold: number
  level: number
  score: number
  highScore: number
  turn: number
  over: boolean
  origin: GameOrigin
}

/** One row of the leaderboard. */
export interface GameSummary {
  gameId: string
  origin: GameOrigin
  score: number
  turn: number
  lives: number
  gold: number
  level: number
  over: boolean
}

export interface Ad {
  adId: string
  message: string
  reward: number
  expiresIn: number
  probability: string
  successChance: number
  expectedValue: number
  recommended: boolean
}

export interface SolveResult {
  success: boolean
  lives: number
  gold: number
  score: number
  highScore: number
  turn: number
  message: string
}

export interface ShopItem {
  itemId: string
  name: string
  cost: number
}

export interface PurchaseResult {
  success: boolean
  gold: number
  lives: number
  level: number
  turn: number
}

export interface Reputation {
  people: number
  state: number
  underworld: number
}

export type AutoplayGameStatus = 'RUNNING' | 'FINISHED' | 'FAILED'

export interface AutoplayGameProgress {
  gameId?: string
  status: AutoplayGameStatus
  score: number
  turn: number
  lives: number
  gold: number
  level: number
  error?: string
}

export interface ScoreSummary {
  min: number
  avg: number
  max: number
}

export interface AutoplaySession {
  sessionId: string
  status: 'RUNNING' | 'FINISHED'
  startedAt: string
  requested: number
  finished: number
  games: AutoplayGameProgress[]
  summary?: ScoreSummary
}

/** Error body the backend sends (RFC 9457 ProblemDetail plus our traceId). */
export interface ProblemDetail {
  status: number
  title: string
  detail?: string
  traceId?: string
}

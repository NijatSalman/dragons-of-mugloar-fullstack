import type { ApiError } from '../api/http'
import type { Ad, AutoplaySession, Game, PurchaseResult, ShopItem, SolveResult } from '../api/types'

/** Everything the UI shows. Immutable: every action produces a new object. */
export interface State {
  game?: Game
  ads: Ad[]
  shop: ShopItem[]
  session?: AutoplaySession
  busy: boolean
  error?: ApiError
  notice?: string
}

export const initialState: State = { ads: [], shop: [], busy: false }

/** What can happen. Each action says what occurred; the reducer decides what it means for the state. */
export type Action =
  | { type: 'REQUEST_STARTED' }
  | { type: 'REQUEST_FAILED'; error: ApiError }
  | { type: 'ERROR_DISMISSED' }
  | { type: 'GAME_STARTED'; game: Game }
  | { type: 'GAME_LOADED'; game: Game }
  | { type: 'ADS_LOADED'; ads: Ad[] }
  | { type: 'AD_SOLVED'; result: SolveResult }
  | { type: 'SHOP_LOADED'; shop: ShopItem[] }
  | { type: 'ITEM_BOUGHT'; itemId: string; result: PurchaseResult }
  | { type: 'SESSION_UPDATED'; session: AutoplaySession }
  | { type: 'GAME_RESET' }

export function gameReducer(state: State, action: Action): State {
  switch (action.type) {
    case 'REQUEST_STARTED':
      return { ...state, busy: true, error: undefined }
    case 'REQUEST_FAILED':
      return { ...state, busy: false, error: action.error }
    case 'ERROR_DISMISSED':
      return { ...state, error: undefined }
    case 'GAME_STARTED':
      return { ...initialState, game: action.game, notice: 'A new game has started. Good luck!' }
    case 'GAME_LOADED':
      return { ...state, game: action.game, busy: false }
    case 'ADS_LOADED':
      return { ...state, ads: action.ads, busy: false }
    case 'AD_SOLVED':
      return { ...state, game: afterSolve(state.game, action.result), notice: action.result.message }
    case 'SHOP_LOADED':
      return { ...state, shop: action.shop, busy: false }
    case 'ITEM_BOUGHT':
      return {
        ...state,
        game: afterPurchase(state.game, action.result),
        notice: action.result.success ? `Bought ${action.itemId}.` : `Could not buy ${action.itemId}.`,
      }
    case 'SESSION_UPDATED':
      return { ...state, session: action.session, busy: false }
    case 'GAME_RESET':
      return { ...initialState, session: state.session }
  }
}

/** Mirrors Game.afterSolve on the backend: lives, gold, score and turn change, the level does not. */
function afterSolve(game: Game | undefined, result: SolveResult): Game | undefined {
  if (!game) return game
  return {
    ...game,
    lives: result.lives,
    gold: result.gold,
    score: result.score,
    highScore: result.highScore,
    turn: result.turn,
    over: result.lives <= 0,
  }
}

/** Mirrors Game.afterPurchase on the backend: gold, lives, level and turn change, the score does not. */
function afterPurchase(game: Game | undefined, result: PurchaseResult): Game | undefined {
  if (!game) return game
  return { ...game, gold: result.gold, lives: result.lives, level: result.level, turn: result.turn }
}

import type { ApiError } from '../api/http'
import type { Ad, AutoplaySession, Game, PurchaseResult, ShopItem, SolveResult } from '../api/types'

/** A short message about the last turn, shown as a toast. */
export interface Notice {
  message: string
  tone: 'success' | 'warning' | 'info'
}

/** Everything the UI shows. Immutable: every action produces a new object. */
export interface State {
  game?: Game
  ads: Ad[]
  shop: ShopItem[]
  session?: AutoplaySession
  busy: boolean
  error?: ApiError
  notice?: Notice
}

export const initialState: State = { ads: [], shop: [], busy: false }

/** What can happen. Each action says what occurred; the reducer decides what it means for the state. */
export type Action =
  | { type: 'REQUEST_STARTED' }
  | { type: 'REQUEST_FAILED'; error: ApiError }
  | { type: 'ERROR_DISMISSED' }
  | { type: 'NOTICE_DISMISSED' }
  | { type: 'GAME_STARTED'; game: Game }
  | { type: 'GAME_LOADED'; game: Game }
  | { type: 'ADS_LOADED'; ads: Ad[] }
  | { type: 'AD_SOLVED'; result: SolveResult }
  | { type: 'SHOP_LOADED'; shop: ShopItem[] }
  | { type: 'ITEM_BOUGHT'; itemId: string; result: PurchaseResult }
  | { type: 'SESSION_STARTED'; session: AutoplaySession }
  | { type: 'SESSION_UPDATED'; session: AutoplaySession }
  | { type: 'GAME_RESET' }
  | { type: 'GAME_EXPIRED' }

export function gameReducer(state: State, action: Action): State {
  switch (action.type) {
    case 'REQUEST_STARTED':
      return { ...state, busy: true, error: undefined }
    case 'REQUEST_FAILED':
      return { ...state, busy: false, error: action.error }
    case 'ERROR_DISMISSED':
      return { ...state, error: undefined }
    case 'NOTICE_DISMISSED':
      return { ...state, notice: undefined }
    case 'GAME_STARTED':
      return { ...initialState, game: action.game, notice: { message: 'A new game has started. Good luck!', tone: 'info' } }
    case 'GAME_LOADED':
      return { ...state, game: action.game, busy: false }
    case 'ADS_LOADED':
      return { ...state, ads: action.ads, busy: false }
    case 'AD_SOLVED':
      return {
        ...state,
        busy: false,
        game: afterSolve(state.game, action.result),
        notice: { message: action.result.message, tone: action.result.success ? 'success' : 'warning' },
      }
    case 'SHOP_LOADED':
      return { ...state, shop: action.shop, busy: false }
    case 'ITEM_BOUGHT':
      return {
        ...state,
        busy: false,
        game: afterPurchase(state.game, action.result),
        notice: action.result.success
          ? { message: `Bought ${action.itemId}.`, tone: 'success' }
          : { message: `Could not buy ${action.itemId}: not enough gold.`, tone: 'warning' },
      }
    case 'SESSION_STARTED':
      return { ...state, session: action.session, busy: false }
    case 'SESSION_UPDATED': // a background poll: never touches busy, so the board keeps working
      return { ...state, session: action.session }
    case 'GAME_RESET':
      return { ...initialState, session: state.session }
    case 'GAME_EXPIRED':
      return { ...initialState, session: state.session, notice: { message: 'Your previous game has expired on the game server.', tone: 'info' } }
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

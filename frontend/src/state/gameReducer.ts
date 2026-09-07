import type { ApiError } from '../api/http'
import type { Ad, AutoplaySession, Game, GameSummary, PurchaseResult, ShopItem, SolveResult } from '../api/types'

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
  leaderboard: GameSummary[]
  busy: boolean
  /** True while a remembered game is being loaded after a page reload. */
  restoring: boolean
  error?: ApiError
  notice?: Notice
}

export const initialState: State = { ads: [], shop: [], leaderboard: [], busy: false, restoring: false }

/** What can happen. Each action says what occurred; the reducer decides what it means for the state. */
export type Action =
  | { type: 'REQUEST_STARTED' }
  | { type: 'RESTORE_STARTED' }
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
  | { type: 'SESSION_FORGOTTEN' }
  | { type: 'LEADERBOARD_LOADED'; games: GameSummary[] }
  | { type: 'GAME_RESET' }
  | { type: 'GAME_EXPIRED' }

export function gameReducer(state: State, action: Action): State {
  switch (action.type) {
    case 'REQUEST_STARTED':
      return { ...state, busy: true, error: undefined }
    case 'RESTORE_STARTED':
      return { ...state, restoring: true }
    case 'REQUEST_FAILED':
      return { ...state, busy: false, restoring: false, error: action.error }
    case 'ERROR_DISMISSED':
      return { ...state, error: undefined }
    case 'NOTICE_DISMISSED':
      return { ...state, notice: undefined }
    case 'GAME_STARTED':
      return { ...initialState, session: state.session, leaderboard: state.leaderboard, game: action.game, notice: { message: 'A new game has started. Good luck!', tone: 'info' } }
    case 'GAME_LOADED':
      return { ...state, game: action.game, busy: false, restoring: false }
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
    case 'SESSION_FORGOTTEN':
      return { ...state, session: undefined }
    case 'LEADERBOARD_LOADED':
      return { ...state, leaderboard: action.games, busy: false }
    case 'GAME_RESET':
      return { ...initialState, session: state.session, leaderboard: state.leaderboard }
    case 'GAME_EXPIRED':
      return { ...initialState, session: state.session, leaderboard: state.leaderboard, notice: { message: 'Your previous game has expired.', tone: 'info' } }
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

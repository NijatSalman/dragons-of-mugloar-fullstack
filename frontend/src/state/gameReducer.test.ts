import { ApiError } from '../api/http'
import type { Ad, AutoplaySession, Game, ShopItem } from '../api/types'
import { gameReducer, initialState, type State } from './gameReducer'

const game: Game = { gameId: 'ggLmesXI', lives: 3, gold: 120, level: 1, score: 300, highScore: 300, turn: 15, over: false, origin: 'MANUAL' }
const ad: Ad = {
  adId: 'DSAUBsXa',
  message: 'Help Majid Desprez to transport a magic beer mug to steppe in Falldean',
  reward: 21,
  expiresIn: 7,
  probability: 'Quite likely',
  successChance: 0.8,
  expectedValue: 16.8,
  recommended: true,
}
const potion: ShopItem = { itemId: 'hpot', name: 'Healing potion', cost: 50 }
const playing: State = { ...initialState, game, ads: [ad], shop: [potion] }

describe('gameReducer', () => {
  it('requestStartedSetsBusyAndClearsTheError', () => {
    const state = gameReducer({ ...initialState, error: new ApiError(500, 'x', 'y') }, { type: 'REQUEST_STARTED' })

    expect(state.busy).toBe(true)
    expect(state.error).toBeUndefined()
  })

  it('requestFailedStoresTheErrorAndStopsBeingBusy', () => {
    const error = new ApiError(404, 'Not Found', 'Game not found: gameId=nope1234', '4bf92f3577b34da6a3ce929d0e0e4736')

    const state = gameReducer({ ...initialState, busy: true }, { type: 'REQUEST_FAILED', error })

    expect(state).toMatchObject({ busy: false, error })
  })

  it('gameStartedReplacesTheGameButKeepsTheAutoplaySession', () => {
    const fresh: Game = { ...game, gameId: '0NVG7E0r', gold: 0, score: 0, turn: 0 }

    const session: AutoplaySession = {
      sessionId: '84e1bfd2-3f61-467d-8fe5-90b9bc358e39',
      status: 'RUNNING',
      startedAt: '2026-09-06T14:00:00Z',
      requested: 3,
      finished: 0,
      games: [],
    }

    const state = gameReducer({ ...playing, session }, { type: 'GAME_STARTED', game: fresh })

    expect(state.game).toEqual(fresh)
    expect(state.ads).toEqual([])
    expect(state.shop).toEqual([])
    expect(state.session).toEqual(session)
  })

  it('adsLoadedReplacesTheBoard', () => {
    const state = gameReducer(playing, { type: 'ADS_LOADED', ads: [] })

    expect(state.ads).toEqual([])
    expect(state.busy).toBe(false)
  })

  it('adSolvedUpdatesLivesGoldScoreAndTurnButNotTheLevel', () => {
    const state = gameReducer(playing, {
      type: 'AD_SOLVED',
      result: { success: true, lives: 3, gold: 141, score: 321, highScore: 321, turn: 16, message: 'You successfully solved the mission!' },
    })

    expect(state.game).toMatchObject({ lives: 3, gold: 141, score: 321, turn: 16, level: 1, over: false })
    expect(state.notice).toEqual({ message: 'You successfully solved the mission!', tone: 'success' })
  })

  it('adSolvedWarnsWhenTheMissionFailed', () => {
    const state = gameReducer(playing, {
      type: 'AD_SOLVED',
      result: { success: false, lives: 2, gold: 120, score: 300, highScore: 300, turn: 16, message: 'You failed on the mission!' },
    })

    expect(state.notice).toEqual({ message: 'You failed on the mission!', tone: 'warning' })
  })

  it('adSolvedMarksTheGameOverWhenTheLastLifeIsLost', () => {
    const state = gameReducer(playing, {
      type: 'AD_SOLVED',
      result: { success: false, lives: 0, gold: 120, score: 300, highScore: 300, turn: 16, message: 'You failed on the mission!' },
    })

    expect(state.game?.over).toBe(true)
  })

  it('itemBoughtUpdatesGoldLivesLevelAndTurnButNotTheScore', () => {
    const state = gameReducer(playing, {
      type: 'ITEM_BOUGHT',
      itemId: 'cs',
      result: { success: true, gold: 20, lives: 3, level: 2, turn: 16 },
    })

    expect(state.game).toMatchObject({ gold: 20, level: 2, turn: 16, score: 300 })
    expect(state.notice).toEqual({ message: 'Bought cs.', tone: 'success' })
  })

  it('sessionUpdatedStoresTheSessionWithoutTouchingBusy', () => {
    const session: AutoplaySession = {
      sessionId: '84e1bfd2-3f61-467d-8fe5-90b9bc358e39',
      status: 'RUNNING',
      startedAt: '2026-09-06T14:00:00Z',
      requested: 3,
      finished: 0,
      games: [],
    }

    const state = gameReducer({ ...initialState, busy: true }, { type: 'SESSION_UPDATED', session })

    expect(state.session).toEqual(session)
    expect(state.busy).toBe(true)
  })

  it('restoreStartedIsClearedOnceTheGameIsLoaded', () => {
    const restoring = gameReducer(initialState, { type: 'RESTORE_STARTED' })
    expect(restoring.restoring).toBe(true)

    const loaded = gameReducer(restoring, { type: 'GAME_LOADED', game })
    expect(loaded.restoring).toBe(false)
  })

  it('gameExpiredForgetsTheGameAndExplainsWhy', () => {
    const state = gameReducer(playing, { type: 'GAME_EXPIRED' })

    expect(state.game).toBeUndefined()
    expect(state.notice?.message).toMatch(/expired/)
  })

  it('leaderboardLoadedStoresTheRankedGames', () => {
    const games = [{ gameId: 'jz21oOWI', origin: 'AUTOPLAY' as const, score: 5239, turn: 201, lives: 0, gold: 87, level: 3 }]

    const state = gameReducer({ ...initialState, busy: true }, { type: 'LEADERBOARD_LOADED', games })

    expect(state.leaderboard).toEqual(games)
    expect(state.busy).toBe(false)
  })

  it('gameResetForgetsTheGameButKeepsTheAutoplaySession', () => {
    const session: AutoplaySession = {
      sessionId: '84e1bfd2-3f61-467d-8fe5-90b9bc358e39',
      status: 'FINISHED',
      startedAt: '2026-09-06T14:00:00Z',
      requested: 1,
      finished: 1,
      games: [],
    }

    const state = gameReducer({ ...playing, session }, { type: 'GAME_RESET' })

    expect(state.game).toBeUndefined()
    expect(state.ads).toEqual([])
    expect(state.session).toEqual(session)
  })
})

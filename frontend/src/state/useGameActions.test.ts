import { renderHook } from '@testing-library/react'
import { gameApi } from '../api/gameApi'
import { ApiError } from '../api/http'
import type { Ad, Game } from '../api/types'
import { useGameActions } from './useGameActions'

vi.mock('../api/gameApi', () => ({
  gameApi: {
    startGame: vi.fn(),
    getGame: vi.fn(),
    getRecommendedAds: vi.fn(),
    solveAd: vi.fn(),
    getShopItems: vi.fn(),
    buyItem: vi.fn(),
    startAutoplay: vi.fn(),
    getAutoplayProgress: vi.fn(),
  },
}))

const api = vi.mocked(gameApi)
const game: Game = { gameId: 'ggLmesXI', lives: 3, gold: 0, level: 0, score: 0, highScore: 0, turn: 0, over: false }
const ads: Ad[] = [
  {
    adId: 'DSAUBsXa',
    message: 'Help Majid Desprez to transport a magic beer mug to steppe in Falldean',
    reward: 21,
    expiresIn: 7,
    probability: 'Quite likely',
    successChance: 0.8,
    expectedValue: 16.8,
    recommended: true,
  },
]

describe('useGameActions', () => {
  const dispatch = vi.fn()

  beforeEach(() => {
    dispatch.mockReset()
    api.getRecommendedAds.mockReset()
    localStorage.clear()
  })

  function actionsFor(gameId?: string) {
    return renderHook(() => useGameActions(dispatch, gameId)).result.current
  }

  it('startGameStartsRemembersAndLoadsTheBoard', async () => {
    api.startGame.mockResolvedValue(game)
    api.getRecommendedAds.mockResolvedValue(ads)

    await actionsFor().startGame()

    expect(localStorage.getItem('dragons.gameId')).toBe('ggLmesXI')
    expect(dispatch.mock.calls.map(([action]) => action.type)).toEqual(['REQUEST_STARTED', 'GAME_STARTED', 'ADS_LOADED'])
    expect(dispatch).toHaveBeenCalledWith({ type: 'ADS_LOADED', ads })
  })

  it('solveAdDispatchesTheResultThenRefreshesTheBoard', async () => {
    const result = { success: true, lives: 3, gold: 21, score: 21, highScore: 21, turn: 1, message: 'You successfully solved the mission!' }
    api.solveAd.mockResolvedValue(result)
    api.getRecommendedAds.mockResolvedValue([])

    await actionsFor('ggLmesXI').solveAd('DSAUBsXa')

    expect(api.solveAd).toHaveBeenCalledWith('ggLmesXI', 'DSAUBsXa')
    expect(dispatch).toHaveBeenCalledWith({ type: 'AD_SOLVED', result })
    expect(dispatch).toHaveBeenLastCalledWith({ type: 'ADS_LOADED', ads: [] })
  })

  it('solveAdDoesNotRefreshTheBoardWhenTheGameIsOver', async () => {
    api.solveAd.mockResolvedValue({ success: false, lives: 0, gold: 40, score: 40, highScore: 40, turn: 5, message: 'You failed on the mission!' })

    await actionsFor('ggLmesXI').solveAd('DSAUBsXa')

    expect(api.getRecommendedAds).not.toHaveBeenCalled()
    expect(dispatch).toHaveBeenLastCalledWith({ type: 'AD_SOLVED', result: expect.objectContaining({ lives: 0 }) })
  })

  it('solveAdDispatchesTheApiErrorWhenTheBackendRejects', async () => {
    const error = new ApiError(409, 'Conflict', 'Ad not available: gameId=ggLmesXI, adId=DSAUBsXa', '4bf92f3577b34da6a3ce929d0e0e4736')
    api.solveAd.mockRejectedValue(error)

    await actionsFor('ggLmesXI').solveAd('DSAUBsXa')

    expect(dispatch).toHaveBeenLastCalledWith({ type: 'REQUEST_FAILED', error })
  })

  it('solveAdFailsWithoutCallingTheBackendWhenNoGameIsRunning', async () => {
    await actionsFor(undefined).solveAd('DSAUBsXa')

    expect(api.solveAd).not.toHaveBeenCalled()
    expect(dispatch).toHaveBeenLastCalledWith({
      type: 'REQUEST_FAILED',
      error: expect.objectContaining({ detail: 'Start a game first' }),
    })
  })

  it('buyItemDispatchesThePurchaseThenRefreshesTheBoard', async () => {
    const result = { success: true, gold: 70, lives: 3, level: 1, turn: 12 }
    api.buyItem.mockResolvedValue(result)
    api.getRecommendedAds.mockResolvedValue(ads)

    await actionsFor('ggLmesXI').buyItem('cs')

    expect(dispatch).toHaveBeenCalledWith({ type: 'ITEM_BOUGHT', itemId: 'cs', result })
    expect(dispatch).toHaveBeenLastCalledWith({ type: 'ADS_LOADED', ads })
  })

  it('startAutoplayDispatchesTheNewSession', async () => {
    const session = { sessionId: '84e1bfd2-3f61-467d-8fe5-90b9bc358e39', status: 'RUNNING' as const, startedAt: '2026-09-06T14:00:00Z', requested: 3, finished: 0, games: [] }
    api.startAutoplay.mockResolvedValue(session)

    await actionsFor().startAutoplay(3)

    expect(api.startAutoplay).toHaveBeenCalledWith(3)
    expect(localStorage.getItem('dragons.sessionId')).toBe('84e1bfd2-3f61-467d-8fe5-90b9bc358e39')
    expect(dispatch.mock.calls.map(([action]) => action.type)).toEqual(['REQUEST_STARTED', 'SESSION_STARTED'])
  })

  it('refreshAutoplayUpdatesTheSessionWithoutMarkingTheUiBusy', async () => {
    const session = { sessionId: '84e1bfd2-3f61-467d-8fe5-90b9bc358e39', status: 'FINISHED' as const, startedAt: '2026-09-06T14:00:00Z', requested: 1, finished: 1, games: [] }
    api.getAutoplayProgress.mockResolvedValue(session)

    await actionsFor().refreshAutoplay(session.sessionId)

    expect(dispatch).toHaveBeenCalledTimes(1)
    expect(dispatch).toHaveBeenCalledWith({ type: 'SESSION_UPDATED', session })
  })

  it('refreshAutoplayForgetsASessionTheBackendNoLongerKnows', async () => {
    localStorage.setItem('dragons.sessionId', '84e1bfd2-3f61-467d-8fe5-90b9bc358e39')
    api.getAutoplayProgress.mockRejectedValue(new ApiError(404, 'Not Found', 'Autoplay session not found'))

    await actionsFor().refreshAutoplay('84e1bfd2-3f61-467d-8fe5-90b9bc358e39')

    expect(localStorage.getItem('dragons.sessionId')).toBeNull()
    expect(dispatch).toHaveBeenLastCalledWith({ type: 'SESSION_FORGOTTEN' })
  })

  it('resumeGameLoadsTheGameAndItsBoard', async () => {
    api.getGame.mockResolvedValue(game)
    api.getRecommendedAds.mockResolvedValue(ads)

    await actionsFor().resumeGame('ggLmesXI')

    expect(dispatch.mock.calls.map(([action]) => action.type)).toEqual(['REQUEST_STARTED', 'GAME_LOADED', 'ADS_LOADED'])
  })

  it('resumeGameForgetsAGameTheServerNoLongerKnows', async () => {
    localStorage.setItem('dragons.gameId', 'ggLmesXI')
    api.getGame.mockResolvedValue(game)
    api.getRecommendedAds.mockRejectedValue(new ApiError(404, 'Not Found', 'Game not found: gameId=ggLmesXI'))

    await actionsFor().resumeGame('ggLmesXI')

    expect(localStorage.getItem('dragons.gameId')).toBeNull()
    expect(dispatch).toHaveBeenLastCalledWith({ type: 'GAME_EXPIRED' })
  })

  it('resetGameForgetsTheRememberedGame', () => {
    localStorage.setItem('dragons.gameId', 'ggLmesXI')

    actionsFor('ggLmesXI').resetGame()

    expect(localStorage.getItem('dragons.gameId')).toBeNull()
    expect(dispatch).toHaveBeenCalledWith({ type: 'GAME_RESET' })
  })
})

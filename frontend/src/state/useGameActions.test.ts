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

  it('resetGameForgetsTheRememberedGame', () => {
    localStorage.setItem('dragons.gameId', 'ggLmesXI')

    actionsFor('ggLmesXI').resetGame()

    expect(localStorage.getItem('dragons.gameId')).toBeNull()
    expect(dispatch).toHaveBeenCalledWith({ type: 'GAME_RESET' })
  })
})

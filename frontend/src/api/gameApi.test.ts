import { gameApi } from './gameApi'

describe('gameApi', () => {
  const fetchMock = vi.fn()

  beforeEach(() => {
    fetchMock.mockResolvedValue(new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } }))
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    fetchMock.mockReset()
  })

  it.each([
    ['startGame', () => gameApi.startGame(), 'POST', '/api/v1/games'],
    ['getGame', () => gameApi.getGame('ggLmesXI'), 'GET', '/api/v1/games/ggLmesXI'],
    ['getTopGames', () => gameApi.getTopGames(), 'GET', '/api/v1/games?limit=10'],
    ['getRecommendedAds', () => gameApi.getRecommendedAds('ggLmesXI'), 'GET', '/api/v1/games/ggLmesXI/ads'],
    ['solveAd', () => gameApi.solveAd('ggLmesXI', 'DSAUBsXa'), 'POST', '/api/v1/games/ggLmesXI/ads/DSAUBsXa/solve'],
    ['investigateReputation', () => gameApi.investigateReputation('ggLmesXI'), 'POST', '/api/v1/games/ggLmesXI/reputation'],
    ['getShopItems', () => gameApi.getShopItems('ggLmesXI'), 'GET', '/api/v1/games/ggLmesXI/shop'],
    ['buyItem', () => gameApi.buyItem('ggLmesXI', 'hpot'), 'POST', '/api/v1/games/ggLmesXI/shop/hpot'],
    ['startAutoplay', () => gameApi.startAutoplay(3), 'POST', '/api/v1/autoplay/sessions?games=3'],
    [
      'getAutoplayProgress',
      () => gameApi.getAutoplayProgress('84e1bfd2-3f61-467d-8fe5-90b9bc358e39'),
      'GET',
      '/api/v1/autoplay/sessions/84e1bfd2-3f61-467d-8fe5-90b9bc358e39',
    ],
  ])('%sCallsTheRightEndpoint', async (_name, call, method, url) => {
    await call()

    expect(fetchMock).toHaveBeenCalledWith(url, expect.objectContaining({ method }))
  })
})

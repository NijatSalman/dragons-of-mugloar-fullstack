import { request } from './http'
import type { Ad, AutoplaySession, Game, PurchaseResult, Reputation, ShopItem, SolveResult } from './types'

/** One function per backend endpoint. Components call these and never build URLs themselves. */
const BASE = '/api/v1'

export const gameApi = {
  startGame: () => request<Game>('POST', `${BASE}/games`),

  getGame: (gameId: string) => request<Game>('GET', `${BASE}/games/${gameId}`),

  getRecommendedAds: (gameId: string) => request<Ad[]>('GET', `${BASE}/games/${gameId}/ads`),

  solveAd: (gameId: string, adId: string) =>
    request<SolveResult>('POST', `${BASE}/games/${gameId}/ads/${adId}/solve`),

  investigateReputation: (gameId: string) =>
    request<Reputation>('POST', `${BASE}/games/${gameId}/reputation`),

  getShopItems: (gameId: string) => request<ShopItem[]>('GET', `${BASE}/games/${gameId}/shop`),

  buyItem: (gameId: string, itemId: string) =>
    request<PurchaseResult>('POST', `${BASE}/games/${gameId}/shop/${itemId}`),

  startAutoplay: (games: number) =>
    request<AutoplaySession>('POST', `${BASE}/autoplay/sessions?games=${games}`),

  getAutoplayProgress: (sessionId: string) =>
    request<AutoplaySession>('GET', `${BASE}/autoplay/sessions/${sessionId}`),
}

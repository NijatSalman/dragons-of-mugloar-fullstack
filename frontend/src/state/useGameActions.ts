import { useMemo, type Dispatch } from 'react'
import { gameApi } from '../api/gameApi'
import { ApiError } from '../api/http'
import type { Action } from './gameReducer'

const GAME_ID_KEY = 'dragons.gameId'

/**
 * The use cases of the UI. Each one calls the backend, then tells the reducer what happened.
 * Components call these and never touch gameApi or dispatch themselves.
 */
export function useGameActions(dispatch: Dispatch<Action>, gameId?: string) {
  return useMemo(() => {
    const run = async (work: () => Promise<void>) => {
      dispatch({ type: 'REQUEST_STARTED' })
      try {
        await work()
      } catch (failure) {
        dispatch({ type: 'REQUEST_FAILED', error: toApiError(failure) })
      }
    }

    const requireGame = () => {
      if (!gameId) throw new ApiError(0, 'No game', 'Start a game first')
      return gameId
    }

    const refreshAds = async (id: string) => {
      dispatch({ type: 'ADS_LOADED', ads: await gameApi.getRecommendedAds(id) })
    }

    return {
      startGame: () =>
        run(async () => {
          const game = await gameApi.startGame()
          localStorage.setItem(GAME_ID_KEY, game.gameId)
          dispatch({ type: 'GAME_STARTED', game })
          await refreshAds(game.gameId)
        }),

      /** After a page reload. The game server forgets games after a while; then we forget them too. */
      resumeGame: async (id: string) => {
        dispatch({ type: 'REQUEST_STARTED' })
        try {
          dispatch({ type: 'GAME_LOADED', game: await gameApi.getGame(id) })
          await refreshAds(id)
        } catch (failure) {
          const error = toApiError(failure)
          if (error.status === 404 || error.status === 410) {
            localStorage.removeItem(GAME_ID_KEY)
            dispatch({ type: 'GAME_EXPIRED' })
          } else {
            dispatch({ type: 'REQUEST_FAILED', error })
          }
        }
      },

      refreshAds: () => run(() => refreshAds(requireGame())),

      solveAd: (adId: string) =>
        run(async () => {
          const id = requireGame()
          const result = await gameApi.solveAd(id, adId)
          dispatch({ type: 'AD_SOLVED', result })
          if (result.lives > 0) await refreshAds(id) // a finished game has no board any more
        }),

      loadShop: () =>
        run(async () => {
          dispatch({ type: 'SHOP_LOADED', shop: await gameApi.getShopItems(requireGame()) })
        }),

      buyItem: (itemId: string) =>
        run(async () => {
          const id = requireGame()
          dispatch({ type: 'ITEM_BOUGHT', itemId, result: await gameApi.buyItem(id, itemId) })
          await refreshAds(id)
        }),

      startAutoplay: (games: number) =>
        run(async () => {
          dispatch({ type: 'SESSION_STARTED', session: await gameApi.startAutoplay(games) })
        }),

      /** Called by the polling hook; runs in the background, so it does not mark the UI busy. */
      refreshAutoplay: async (sessionId: string) => {
        try {
          dispatch({ type: 'SESSION_UPDATED', session: await gameApi.getAutoplayProgress(sessionId) })
        } catch (failure) {
          dispatch({ type: 'REQUEST_FAILED', error: toApiError(failure) })
        }
      },

      resetGame: () => {
        localStorage.removeItem(GAME_ID_KEY)
        dispatch({ type: 'GAME_RESET' })
      },

      dismissError: () => dispatch({ type: 'ERROR_DISMISSED' }),

      dismissNotice: () => dispatch({ type: 'NOTICE_DISMISSED' }),
    }
  }, [dispatch, gameId])
}

export type GameActions = ReturnType<typeof useGameActions>

/** The game id remembered across page reloads, if any. */
export function rememberedGameId(): string | undefined {
  return localStorage.getItem(GAME_ID_KEY) ?? undefined
}

function toApiError(failure: unknown): ApiError {
  if (failure instanceof ApiError) return failure
  return new ApiError(0, 'Unexpected error', failure instanceof Error ? failure.message : String(failure))
}

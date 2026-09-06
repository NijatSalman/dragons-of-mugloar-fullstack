import { useEffect, useReducer, useRef, type ReactNode } from 'react'
import { GameContext } from './gameContext'
import { gameReducer, initialState } from './gameReducer'
import { rememberedGameId, rememberedSessionId, useGameActions } from './useGameActions'

/** Holds the one state object of the app and gives every component access to it through useGame(). */
export function GameProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(gameReducer, initialState)
  const actions = useGameActions(dispatch, state.game?.gameId)
  const resumed = useRef(false)

  // After a page reload, pick up the game being played and the autoplay session being watched. Runs once.
  useEffect(() => {
    if (resumed.current) return
    resumed.current = true
    const gameId = rememberedGameId()
    if (gameId) void actions.resumeGame(gameId)
    const sessionId = rememberedSessionId()
    if (sessionId) void actions.refreshAutoplay(sessionId)
  }, [actions])

  return <GameContext.Provider value={{ state, actions }}>{children}</GameContext.Provider>
}

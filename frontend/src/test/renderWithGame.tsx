import { render } from '@testing-library/react'
import type { ReactNode } from 'react'
import { GameContext, type GameContextValue } from '../state/gameContext'
import { initialState, type State } from '../state/gameReducer'
import type { GameActions } from '../state/useGameActions'

/** Renders a component inside a GameContext with the given state and mocked actions, without any HTTP. */
export function renderWithGame(ui: ReactNode, state: Partial<State> = {}) {
  const actions = {
    startGame: vi.fn(),
    resumeGame: vi.fn(),
    refreshAds: vi.fn(),
    solveAd: vi.fn(),
    loadShop: vi.fn(),
    buyItem: vi.fn(),
    startAutoplay: vi.fn(),
    refreshAutoplay: vi.fn(),
    resetGame: vi.fn(),
    dismissError: vi.fn(),
    dismissNotice: vi.fn(),
    loadLeaderboard: vi.fn(),
  } as unknown as GameActions
  const value: GameContextValue = { state: { ...initialState, ...state }, actions }
  return { ...render(<GameContext.Provider value={value}>{ui}</GameContext.Provider>), actions }
}

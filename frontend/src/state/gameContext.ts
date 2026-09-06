import { createContext } from 'react'
import type { State } from './gameReducer'
import type { GameActions } from './useGameActions'

export interface GameContextValue {
  state: State
  actions: GameActions
}

export const GameContext = createContext<GameContextValue | undefined>(undefined)

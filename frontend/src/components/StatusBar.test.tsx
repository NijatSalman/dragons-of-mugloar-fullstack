import { render, screen } from '@testing-library/react'
import { StatusBar } from './StatusBar'

describe('StatusBar', () => {
  it('showsLivesGoldScoreLevelTurnAndGameId', () => {
    render(<StatusBar game={{ gameId: 'ggLmesXI', lives: 2, gold: 120, level: 1, score: 1462, highScore: 1462, turn: 41, over: false }} />)

    expect(screen.getByText('2 lives')).toBeInTheDocument()
    expect(screen.getByText('120 gold')).toBeInTheDocument()
    expect(screen.getByText('1462 score')).toBeInTheDocument()
    expect(screen.getByText('level 1')).toBeInTheDocument()
    expect(screen.getByText('turn 41')).toBeInTheDocument()
    expect(screen.getByText('Game ggLmesXI')).toBeInTheDocument()
  })
})

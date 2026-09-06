import { screen } from '@testing-library/react'
import { App } from './App'
import { ApiError } from './api/http'
import { renderWithGame } from './test/renderWithGame'

describe('App', () => {
  it('showsTheStartPanelWhenNoGameIsRunning', () => {
    renderWithGame(<App />)

    expect(screen.getByRole('heading', { name: 'Dragons of Mugloar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Start new game' })).toBeInTheDocument()
  })

  it('showsTheStatusBarWhenAGameIsRunning', () => {
    renderWithGame(<App />, {
      game: { gameId: 'ggLmesXI', lives: 3, gold: 0, level: 0, score: 0, highScore: 0, turn: 0, over: false },
    })

    expect(screen.getByText('3 lives')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Start new game' })).not.toBeInTheDocument()
  })

  it('showsTheErrorBannerWithTheTraceId', () => {
    renderWithGame(<App />, {
      error: new ApiError(502, 'Bad Gateway', 'Game server is currently unavailable', '4bf92f3577b34da6a3ce929d0e0e4736'),
    })

    expect(screen.getByText(/Game server is currently unavailable/)).toBeInTheDocument()
    expect(screen.getByText(/4bf92f3577b34da6a3ce929d0e0e4736/)).toBeInTheDocument()
  })
})

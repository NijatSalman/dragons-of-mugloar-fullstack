import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { StatusBar } from './StatusBar'

describe('StatusBar', () => {
  it('statusBarShowsLivesGoldScoreLevelTurnAndGameId', () => {
    render(<StatusBar game={{ gameId: 'ggLmesXI', lives: 2, gold: 120, level: 1, score: 1462, highScore: 1462, turn: 41, over: false, origin: 'MANUAL' }} onLeave={vi.fn()} />)

    expect(screen.getByText('2 lives')).toBeInTheDocument()
    expect(screen.getByText('120 gold')).toBeInTheDocument()
    expect(screen.getByText('1462 score')).toBeInTheDocument()
    expect(screen.getByText('level 1')).toBeInTheDocument()
    expect(screen.getByText('turn 41')).toBeInTheDocument()
    expect(screen.getByText('Game ggLmesXI')).toBeInTheDocument()
  })

  it('statusBarLeaveGameButtonReportsTheWishToLeave', async () => {
    const onLeave = vi.fn()
    render(<StatusBar game={{ gameId: 'ggLmesXI', lives: 3, gold: 0, level: 0, score: 0, highScore: 0, turn: 0, over: false, origin: 'MANUAL' }} onLeave={onLeave} />)

    await userEvent.click(screen.getByRole('button', { name: 'Leave game' }))

    expect(onLeave).toHaveBeenCalledOnce()
  })
})
